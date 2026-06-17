package com.sigmundgranaas.forgero.core.attribute.kernel;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.attribute.api.AttributeScope;
import com.sigmundgranaas.forgero.core.attribute.api.BakedAttributes;
import com.sigmundgranaas.forgero.core.attribute.api.PrecomputedAttribute;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.CustomizableComponent;
import com.sigmundgranaas.forgero.core.component.api.Slot;
import com.sigmundgranaas.forgero.core.component.api.StructuredComponent;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentPart;
import com.sigmundgranaas.forgero.core.condition.api.Condition;
import com.sigmundgranaas.forgero.core.property.compilation.ResolutionContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The stat kernel: one recursive seal-fold over the component tree (ADR-003).
 *
 * <p>Generic by construction — it knows no stat names, no scope strings, no content patterns.
 * Everything it computes is driven by {@link StatContribution} data:
 * <ul>
 *   <li><b>Compose layer:</b> a node's own contributions plus its structure children's sealed
 *       exports. Plain ADDs sum; <i>offers</i> (gated ADDs) join the base only when a MULTIPLY
 *       of the same type from a different source accepts them; multipliers scale the base.</li>
 *   <li><b>Seal:</b> the folded values become the node's stats. Parents receive results.</li>
 *   <li><b>Rise:</b> unaccepted offers and unmet multipliers rise with the export and try
 *       again at the parent fold. Unconsumed at the terminal ⇒ inert (reported, not silent).</li>
 *   <li><b>Modify layer:</b> filled upgrade slots' sealed exports add to / scale the sealed
 *       base.</li>
 *   <li><b>Dynamic conditions:</b> never evaluated here. Contributions carrying them are
 *       lifted, unfolded, to the terminal's conditional list for the game layer.</li>
 * </ul>
 */
public final class StatFold {

	private StatFold() {
	}

	/** A contribution travelling through a fold, tagged with the source it entered from. */
	private record Sourced(StatContribution c, Object source) {
	}

	/** The result of sealing a node: sealed values exported as ADDs, plus risers. */
	private record Export(List<Sourced> contributions, Map<OpenIdentifier, List<Attribute>> dynamicByType) {
	}

	/**
	 * Folds a component tree into compiled attributes. Output shape is identical to the
	 * current engine's, so it is parity-comparable and drop-in.
	 */
	public static BakedAttributes fold(Component root) {
		Export export = seal(root, root, false);

		Map<OpenIdentifier, Float> values = new LinkedHashMap<>();
		Map<OpenIdentifier, List<StatContribution>> leftovers = new LinkedHashMap<>();
		for (Sourced s : export.contributions()) {
			if (s.c().operation() == StatContribution.Operation.ADD && !s.c().gated()) {
				values.merge(s.c().type(), s.c().value(), Float::sum);
			} else {
				leftovers.computeIfAbsent(s.c().type(), k -> new ArrayList<>()).add(s.c());
			}
		}

		Map<OpenIdentifier, PrecomputedAttribute> byType = new LinkedHashMap<>();
		for (var e : values.entrySet()) {
			byType.put(e.getKey(), new PrecomputedAttribute(e.getValue(),
					export.dynamicByType().getOrDefault(e.getKey(), List.of())));
		}
		// dynamic-only types still surface (base 0 + conditional data)
		for (var e : export.dynamicByType().entrySet()) {
			byType.computeIfAbsent(e.getKey(), k -> new PrecomputedAttribute(0f, e.getValue()));
		}
		return new BakedAttributes(byType);
	}

	/**
	 * Seals one node: compose (structure) → modify (upgrades) → export sealed values + risers.
	 *
	 * @param slotForOwnFilter transitional shim: when this node sits in an upgrade slot, the
	 *                         slot may filter the node's OWN contributions (legacy slot scope).
	 */
	private static Export seal(Component node, Component globalRoot, boolean inUpgradeSlot) {
		ResolutionContext ctx = new ResolutionContext(node, globalRoot);
		List<Sourced> pool = new ArrayList<>();
		Map<OpenIdentifier, List<Attribute>> dynamic = new HashMap<>();

		// own contributions (source = the node itself)
		for (Attribute attribute : node.properties(Attribute.KEY)) {
			if (!staticPass(attribute.condition(), ctx)) {
				continue;
			}
			if (hasDynamic(attribute.condition())) {
				dynamic.computeIfAbsent(attribute.type(), k -> new ArrayList<>()).add(attribute);
				continue;
			}
			AttributeShim.toContribution(attribute).ifPresent(c -> {
				// local = applies only to the directly-queried component (the fold root).
				if (c.local() && node != globalRoot) {
					return;
				}
				// upgrade-only = applies only when this node occupies an upgrade slot.
				if (c.upgradeOnly() && !inUpgradeSlot) {
					return;
				}
				pool.add(new Sourced(c, node));
			});
		}

		// compose layer: structure children seal first; their exports enter re-sourced
		if (node instanceof StructuredComponent structured) {
			for (ComponentPart part : structured.structure().parts().values()) {
				Export child = seal(part.content(), globalRoot, false);
				for (Sourced s : child.contributions()) {
					pool.add(new Sourced(s.c(), part.id()));
				}
				mergeDynamic(dynamic, child.dynamicByType());
			}
		}

		FoldResult composed = foldPool(pool);

		// modify layer: filled slots' exports apply to the sealed base. Generic over any Slot
		// kind that opts into traversal and contributes a Component (gems, runes, a potion modeled
		// as a Component, …) — not just ComponentUpgradeSlot. Kinds that hold non-Component state
		// (StatusModifierSlot, ArrowSlot) return empty componentContent() and are skipped here,
		// contributing through their own machinery instead.
		List<Sourced> upgradePool = new ArrayList<>();
		if (node instanceof CustomizableComponent customizable) {
			for (Slot slot : customizable.upgrades().slots().asList()) {
				if (!slot.includeInTraversal()) {
					continue;
				}
				Optional<Component> content = slot.componentContent();
				if (content.isEmpty()) {
					continue;
				}
				Export upgrade = seal(content.get(), globalRoot, true);
				for (Sourced s : upgrade.contributions()) {
					upgradePool.add(new Sourced(s.c(), slot.id()));
				}
				mergeDynamic(dynamic, upgrade.dynamicByType());
			}
		}
		FoldResult modified = foldPool(merge(composed, upgradePool));

		// export: sealed values as plain ADDs + risers (unaccepted offers / unmet multipliers)
		List<Sourced> export = new ArrayList<>();
		for (var e : modified.values().entrySet()) {
			export.add(new Sourced(StatContribution.add(e.getKey(), e.getValue()), node));
		}
		export.addAll(modified.risers());
		return new Export(export, dynamic);
	}

	private record FoldResult(Map<OpenIdentifier, Float> values, List<Sourced> risers) {
	}

	/**
	 * The fold over one pool: per type, base = plain ADDs + accepted offers; multipliers scale
	 * the base. Offers without a different-source multiplier and multipliers without a base
	 * become risers.
	 */
	private static FoldResult foldPool(List<Sourced> pool) {
		Map<OpenIdentifier, Float> values = new LinkedHashMap<>();
		List<Sourced> risers = new ArrayList<>();

		Map<OpenIdentifier, List<Sourced>> byType = new LinkedHashMap<>();
		for (Sourced s : pool) {
			byType.computeIfAbsent(s.c().type(), k -> new ArrayList<>()).add(s);
		}

		for (var entry : byType.entrySet()) {
			OpenIdentifier type = entry.getKey();
			List<Sourced> adds = entry.getValue().stream().filter(s -> s.c().operation() == StatContribution.Operation.ADD).toList();
			List<Sourced> muls = entry.getValue().stream().filter(s -> s.c().operation() == StatContribution.Operation.MULTIPLY).toList();

			float base = 0f;
			boolean hasBase = false;
			for (Sourced add : adds) {
				if (!add.c().gated()) {
					base += add.c().value();
					hasBase = true;
				} else if (acceptedBy(add, muls)) {
					base += add.c().value();
					hasBase = true;
				} else {
					risers.add(add);
				}
			}

			if (hasBase) {
				float value = base;
				for (Sourced mul : muls) {
					value *= mul.c().value();
				}
				values.put(type, value);
			} else {
				risers.addAll(muls);
			}
		}
		return new FoldResult(values, risers);
	}

	/** An offer is accepted when a multiplier of the same type entered from a different source. */
	private static boolean acceptedBy(Sourced offer, List<Sourced> muls) {
		return muls.stream().anyMatch(m -> m.source() != offer.source());
	}

	private static List<Sourced> merge(FoldResult sealed, List<Sourced> upgradePool) {
		List<Sourced> pool = new ArrayList<>();
		for (var e : sealed.values().entrySet()) {
			pool.add(new Sourced(StatContribution.add(e.getKey(), e.getValue()), StatFold.class));
		}
		pool.addAll(sealed.risers());
		pool.addAll(upgradePool);
		return pool;
	}

	private static void mergeDynamic(Map<OpenIdentifier, List<Attribute>> into, Map<OpenIdentifier, List<Attribute>> from) {
		from.forEach((k, v) -> into.computeIfAbsent(k, x -> new ArrayList<>()).addAll(v));
	}

	private static boolean staticPass(Optional<Condition> condition, ResolutionContext ctx) {
		return condition.map(Condition::staticConditions).map(ctx::test).orElse(true);
	}

	private static boolean hasDynamic(Optional<Condition> condition) {
		return condition.map(c -> !c.dynamicConditions().isEmpty()).orElse(false);
	}
}
