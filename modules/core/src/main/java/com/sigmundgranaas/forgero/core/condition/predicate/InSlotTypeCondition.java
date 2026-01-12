package com.sigmundgranaas.forgero.core.condition.predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.condition.api.StaticCondition;
import com.sigmundgranaas.forgero.core.property.context.ResolutionContext;
import com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants;

import java.util.Optional;

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
 * @param type     The condition type identifier (always "forgero:in_slot_type")
 * @param slotType The slot type to match against (e.g., "forgero:head_slot")
 * @see ResolutionContext#getSlot() for mutable slot access
 * @see ResolutionContext#getPart() for immutable part access
 */
public record InSlotTypeCondition(OpenIdentifier type, OpenIdentifier slotType) implements StaticCondition {
	public static final Codec<InSlotTypeCondition> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("type").forGetter(InSlotTypeCondition::type),
					CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("slot_type").forGetter(InSlotTypeCondition::slotType)
			).apply(instance, InSlotTypeCondition::new));

	@Override
	public boolean test(ResolutionContext context) {
		// Check if component is in a mutable slot (upgrade slot)
		Optional<Boolean> slotCheck = context.getSlot()
				.map(slot -> slot.slotType().equals(slotType));
		if (slotCheck.isPresent()) {
			return slotCheck.get();
		}

		// Check if component is in an immutable structure part
		return context.getPart()
				.map(part -> part.partType().equals(slotType))
				.orElse(false);
	}
}
