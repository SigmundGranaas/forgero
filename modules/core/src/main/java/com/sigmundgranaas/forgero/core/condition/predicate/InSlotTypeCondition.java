package com.sigmundgranaas.forgero.core.condition.predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.tags.api.TagResolver;
import com.sigmundgranaas.forgero.core.component.api.Slot;
import com.sigmundgranaas.forgero.core.component.api.slot.ComponentUpgradeSlot;
import com.sigmundgranaas.forgero.core.condition.api.StaticCondition;
import com.sigmundgranaas.forgero.core.property.compilation.ResolutionContext;
import com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

/**
 * A static condition that passes when the component is contained within a slot of a specific type.
 *
 * <p>This condition checks where in the component hierarchy the current component is placed.
 * It supports both:
 * <ul>
 *   <li><strong>Mutable slots</strong> (upgrade slots) - Optional slots that can be filled/emptied</li>
 *   <li><strong>Immutable parts</strong> (structure parts) - Required slots that are always filled</li>
 * </ul>
 *
 * <h2>Use Cases</h2>
 * <ul>
 *   <li><strong>Slot-specific bonuses:</strong> A gem provides different effects based on
 *       which slot it's placed in (head slot vs handle slot)</li>
 *   <li><strong>Role-based attributes:</strong> Same material provides different stats when
 *       used as a blade vs a handle</li>
 *   <li><strong>Upgrade restrictions:</strong> Certain effects only activate when installed
 *       in upgrade slots, not when used as primary materials</li>
 * </ul>
 *
 * <h2>Common Slot Types</h2>
 * <table>
 *   <tr><th>Slot Type</th><th>Description</th></tr>
 *   <tr><td>{@code forgero:head_slot}</td><td>Tool/weapon head (pickaxe head, sword blade)</td></tr>
 *   <tr><td>{@code forgero:handle_slot}</td><td>Handle/grip</td></tr>
 *   <tr><td>{@code forgero:binding_slot}</td><td>Binding between head and handle</td></tr>
 *   <tr><td>{@code forgero:gem_slot}</td><td>Gem upgrade slot</td></tr>
 *   <tr><td>{@code forgero:schematic_slot}</td><td>Schematic defining part shape</td></tr>
 *   <tr><td>{@code forgero:material_slot}</td><td>Primary material</td></tr>
 * </table>
 *
 * <h2>Example: Material Role-Based Attributes</h2>
 * <pre>{@code
 * // Iron material definition with slot-specific attributes:
 * {
 *   "attributes": [
 *     {
 *       "type": "forgero:attack_damage",
 *       "value": 2.0,
 *       "context": "forgero:part-composite",
 *       "condition": {
 *         "static": [{ "type": "forgero:in_slot_type", "slot_type": "forgero:head_slot" }]
 *       }
 *     },
 *     {
 *       "type": "forgero:durability",
 *       "value": 1.2,
 *       "operator": "multiplication",
 *       "context": "forgero:part-composite",
 *       "condition": {
 *         "static": [{ "type": "forgero:in_slot_type", "slot_type": "forgero:handle_slot" }]
 *       }
 *     }
 *   ]
 * }
 * }</pre>
 *
 * <h2>Component Tree Example</h2>
 * <pre>
 * iron-pickaxe
 *   ├─ [head_slot] iron-pickaxe_head    ← in_slot_type:head_slot = true
 *   │   ├─ [schematic] pickaxe_head_schematic
 *   │   └─ [material] iron              ← in_slot_type:material_slot = true
 *   ├─ [handle_slot] oak-handle         ← in_slot_type:handle_slot = true
 *   └─ [gem_slot] diamond_gem           ← in_slot_type:gem_slot = true
 * </pre>
 *
 * <h2>Hierarchical matching</h2>
 * A slot answers to several identities: its type (its install identity, e.g.
 * {@code materials/roles/upgrade_material} — what an "in any upgrade slot" condition asks for) and
 * its identity tags (e.g. its context {@code contexts/offensive}). The condition matches if any of
 * those identities <em>is</em> the requested {@code slot_type}, resolved through the tag graph — so
 * {@code in_slot_type: forgero:contexts} matches a slot tagged {@code forgero:contexts/offensive}
 * when the graph makes the latter a descendant of the former. When no resolver is available (e.g.
 * a directly-constructed condition in a unit test) it falls back to exact equality.
 *
 * @see ResolutionContext#getSlot() for mutable slot access
 * @see ResolutionContext#getPart() for immutable part access
 */
public final class InSlotTypeCondition implements StaticCondition {

	private final OpenIdentifier type;
	private final OpenIdentifier slotType;
	private final transient Supplier<TagResolver> resolverSupplier;

	public InSlotTypeCondition(OpenIdentifier type, OpenIdentifier slotType, Supplier<TagResolver> resolverSupplier) {
		this.type = type;
		this.slotType = slotType;
		this.resolverSupplier = resolverSupplier;
	}

	/** Constructs a condition without a tag resolver — matching falls back to exact equality. */
	public InSlotTypeCondition(OpenIdentifier type, OpenIdentifier slotType) {
		this(type, slotType, () -> null);
	}

	/**
	 * Resolver-injecting codec. {@code slot_type} is a tag-like classifier, so its full path is
	 * preserved (matching the path-preserving slot type/tags); see CodecConstants for the contract.
	 */
	public static Codec<InSlotTypeCondition> codec(Supplier<TagResolver> resolverSupplier) {
		return RecordCodecBuilder.create(instance ->
				instance.group(
						CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("type").forGetter(InSlotTypeCondition::type),
						CodecConstants.FULL_PATH_IDENTIFIER_CODEC.fieldOf("slot_type").forGetter(InSlotTypeCondition::slotType)
				).apply(instance, (type, slotType) -> new InSlotTypeCondition(type, slotType, resolverSupplier)));
	}

	/** Exact-match codec for callers without a resolver. */
	public static final Codec<InSlotTypeCondition> CODEC = codec(() -> null);

	@Override
	public OpenIdentifier type() {
		return type;
	}

	public OpenIdentifier slotType() {
		return slotType;
	}

	/**
	 * Does any of the slot's identities satisfy the requested {@code slot_type}? With a resolver
	 * this is a tag-graph query ({@code hasTag} subsumes exact equality and adds descendant
	 * matching); without one it is plain equality.
	 */
	private boolean matches(Set<OpenIdentifier> identities) {
		TagResolver resolver = resolverSupplier.get();
		if (resolver != null) {
			return resolver.hasTag(() -> identities, slotType);
		}
		return identities.contains(slotType);
	}

	@Override
	public boolean test(ResolutionContext context) {
		Optional<Slot> slotOpt = context.getSlot();
		if (slotOpt.isPresent()) {
			Slot slot = slotOpt.get();
			Set<OpenIdentifier> identities = new HashSet<>();
			identities.add(slot.slotType());
			if (slot instanceof ComponentUpgradeSlot upgradeSlot) {
				identities.addAll(upgradeSlot.tags());
			}
			return matches(identities);
		}

		// Immutable structure part: match the part's type.
		return context.getPart()
				.map(part -> matches(Set.of(part.partType())))
				.orElse(false);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof InSlotTypeCondition that)) return false;
		return Objects.equals(type, that.type) && Objects.equals(slotType, that.slotType);
	}

	@Override
	public int hashCode() {
		return Objects.hash(type, slotType);
	}
}
