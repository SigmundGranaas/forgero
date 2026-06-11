package com.sigmundgranaas.forgero.core.attribute.impl;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.attribute.api.AttributeScope;
import com.sigmundgranaas.forgero.core.attribute.api.SimpleAttribute;
import com.sigmundgranaas.forgero.core.attribute.composition.PartCompositeScopeHandler;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.ComponentTreeVisitor;
import com.sigmundgranaas.forgero.core.component.api.ComponentTreeWalker;
import com.sigmundgranaas.forgero.core.component.api.CustomizableComponent;
import com.sigmundgranaas.forgero.core.component.api.StructuredComponent;
import com.sigmundgranaas.forgero.core.component.api.slot.ComponentUpgradeSlot;
import com.sigmundgranaas.forgero.core.condition.api.Condition;
import com.sigmundgranaas.forgero.core.property.compilation.ResolutionContext;

import java.util.*;
import java.util.stream.Collectors;

import static com.sigmundgranaas.forgero.core.attribute.api.Attribute.KEY;

/**
 * Collects attributes from upgrade slots, applying slot scope filtering.
 *
 * <p>This collector handles the complex logic of gathering attributes from
 * components installed in upgrade slots. It applies proper scope filtering
 * and handles structured upgrades (like guards with shape + material) by
 * composing their attributes first.</p>
 *
 * <h2>Collection Rules</h2>
 * <ul>
 *   <li>Only collects when inside an upgrade slot</li>
 *   <li>Applies slot scope filtering using {@link AttributeScope#matchesSlotScope}</li>
 *   <li>For structured upgrades, composes part-composite attributes first</li>
 *   <li>Resolves attributes (removes scope) after collection</li>
 *   <li>Avoids double-counting attributes that were composed</li>
 * </ul>
 *
 * <h2>Structured Upgrade Handling</h2>
 * <p>When an upgrade is a structured component (e.g., a guard with shape + material),
 * its part-composite attributes are first composed using the intersection algorithm.
 * The composed result is then collected along with any non-composite attributes.</p>
 *
 * <h2>Example Usage</h2>
 * <pre>{@code
 * UpgradeAttributeCollector collector = new UpgradeAttributeCollector();
 * ComponentTreeWalker.walk(component, collector);
 * List<Attribute> upgradeAttrs = collector.getResult();
 * }</pre>
 */
public class UpgradeAttributeCollector implements ComponentTreeVisitor<List<Attribute>> {

	private final List<Attribute> result = new ArrayList<>();
	private final PartCompositeScopeHandler partCompositeHandler = PartCompositeScopeHandler.INSTANCE;
	private final Deque<ComponentUpgradeSlot> slotStack = new ArrayDeque<>();
	private Component currentRoot = null;

	@Override
	public boolean visit(Component component, Component root, int depth) {
		this.currentRoot = root;

		// If not in an upgrade slot, just continue traversing to find upgrade slots
		if (slotStack.isEmpty()) {
			return true;
		}

		// We're inside an upgrade slot - process the upgrade component
		ComponentUpgradeSlot currentSlot = slotStack.peek();
		processUpgradeComponent(component, root, currentSlot);

		// Don't traverse deeper from here - we handle nested upgrades separately
		return false;
	}

	@Override
	public void enterStructureSlot(OpenIdentifier slotId, Component component) {
		// No special handling for structure slots during upgrade collection
	}

	@Override
	public void exitStructureSlot(OpenIdentifier slotId) {
		// No special handling for structure slots during upgrade collection
	}

	@Override
	public void enterUpgradeSlot(ComponentUpgradeSlot slot) {
		slotStack.push(slot);
	}

	@Override
	public void exitUpgradeSlot(ComponentUpgradeSlot slot) {
		slotStack.pop();
	}

	/**
	 * Returns all collected upgrade attributes.
	 *
	 * <p>Attributes are already resolved (scope removed) and ready for
	 * inclusion in the final attribute list.</p>
	 *
	 * @return List of resolved attributes from all upgrade slots
	 */
	@Override
	public List<Attribute> getResult() {
		return result;
	}

	/**
	 * Returns the number of collected attributes.
	 *
	 * @return Count of collected attributes
	 */
	public int getCount() {
		return result.size();
	}

	/**
	 * Checks if any attributes were collected.
	 *
	 * @return true if at least one attribute was collected
	 */
	public boolean hasAttributes() {
		return !result.isEmpty();
	}

	private void processUpgradeComponent(Component upgrade, Component root, ComponentUpgradeSlot slot) {
		Optional<OpenIdentifier> slotScope = slot.scope();
		ResolutionContext ctx = new ResolutionContext(upgrade, root);

		// For structured upgrades (like guards with shape + material), compose first
		// This transforms part-composite attributes into resolved (no-scope) attributes
		List<Attribute> composedAttrs = List.of();
		if (upgrade instanceof StructuredComponent structuredUpgrade) {
			composedAttrs = composeStructuredUpgrade(structuredUpgrade, root);
		}

		// Collect non-composite attributes from the upgrade, filtered by slot scope
		List<Attribute> rawAttrs = upgrade.properties(KEY).stream()
				.filter(attr -> AttributeScope.matchesSlotScope(attr.scope(), slotScope))
				.filter(attr -> attr.condition()
						.map(Condition::staticConditions)
						.map(ctx::test)
						.orElse(true))
				// Resolve the attribute (remove scope since it's now been processed)
				.<Attribute>map(attr -> SimpleAttribute.resolved(attr.type(), attr.value()))
				.toList();

		// Merge composed attributes (from structure composition) with raw attributes
		// Composed attrs are already resolved (no scope), raw attrs were filtered and resolved above
		Set<OpenIdentifier> composedTypes = composedAttrs.stream()
				.map(Attribute::type)
				.collect(Collectors.toSet());

		// Add composed attributes first
		result.addAll(composedAttrs);

		// Add raw attributes that weren't already covered by composition
		// This prevents double-counting attributes that were composed
		for (Attribute attr : rawAttrs) {
			if (!composedTypes.contains(attr.type())) {
				result.add(attr);
			}
		}

		// Recursively collect from nested customizable components in the upgrade
		if (upgrade instanceof CustomizableComponent nestedCustomizable) {
			collectNestedUpgrades(nestedCustomizable, root);
		}
	}

	private List<Attribute> composeStructuredUpgrade(StructuredComponent upgrade, Component root) {
		// Use the part-composite collector to gather attributes from the upgrade's structure
		PartCompositeAttributeCollector collector = new PartCompositeAttributeCollector();
		ComponentTreeWalker.walkFrom(upgrade, root, collector);

		Map<String, List<Attribute>> sources = collector.getResult();
		if (sources.size() >= 2) {
			List<Attribute> composed = partCompositeHandler.compose(sources);
			if (!composed.isEmpty()) {
				return composed;
			}
			// Fall back to additive composition if intersection fails
			return additiveComposeMultiSource(sources);
		} else if (sources.size() == 1) {
			// Single source - discard scoped attributes
			return sources.values().iterator().next().stream()
					.filter(attr -> attr.scope().isEmpty())
					.toList();
		}
		return List.of();
	}

	private List<Attribute> additiveComposeMultiSource(Map<String, List<Attribute>> sources) {
		Map<OpenIdentifier, Set<String>> typeToSources = new HashMap<>();
		Map<OpenIdentifier, Float> sums = new HashMap<>();
		Map<OpenIdentifier, Boolean> hasResolved = new HashMap<>();

		for (Map.Entry<String, List<Attribute>> entry : sources.entrySet()) {
			String sourceName = entry.getKey();
			for (Attribute attr : entry.getValue()) {
				OpenIdentifier type = attr.type();
				sums.merge(type, attr.value(), Float::sum);

				if (attr.scope().isEmpty()) {
					hasResolved.put(type, true);
				} else {
					typeToSources.computeIfAbsent(type, k -> new HashSet<>()).add(sourceName);
				}
			}
		}

		return sums.entrySet().stream()
				.filter(e -> {
					OpenIdentifier type = e.getKey();
					if (hasResolved.getOrDefault(type, false)) {
						return true;
					}
					Set<String> scopeSources = typeToSources.get(type);
					return scopeSources != null && scopeSources.size() >= 2;
				})
				.<Attribute>map(e -> SimpleAttribute.resolved(e.getKey(), e.getValue()))
				.toList();
	}

	private void collectNestedUpgrades(CustomizableComponent customizable, Component root) {
		for (ComponentUpgradeSlot nestedSlot : customizable.upgrades().allUpgradeSlots()) {
			if (!nestedSlot.isFilled()) {
				continue;
			}

			nestedSlot.content().ifPresent(nestedUpgrade -> {
				slotStack.push(nestedSlot);
				processUpgradeComponent(nestedUpgrade, root, nestedSlot);
				slotStack.pop();
			});
		}
	}
}
