package com.sigmundgranaas.forgero.core.condition.predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.condition.api.StaticCondition;
import com.sigmundgranaas.forgero.core.property.compilation.ResolutionContext;
import com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants;

/**
 * A static condition that passes when a slot of a specific type contains a component with a specific tag.
 *
 * <p>This condition searches the entire component tree from the root to find a slot of the
 * specified type, then checks if the component in that slot has the required tag. This enables
 * powerful cross-component conditional logic where one component's properties depend on what's
 * installed elsewhere in the equipment.
 *
 * <h2>Use Cases</h2>
 * <ul>
 *   <li><strong>Cross-slot synergies:</strong> Handle properties change based on what blade
 *       material is used</li>
 *   <li><strong>Gem interactions:</strong> Blade gets bonus when a fire gem is in the gem slot</li>
 *   <li><strong>Material requirements:</strong> Effect only activates if head uses metal material</li>
 * </ul>
 *
 * <h2>Search Scope</h2>
 * <p>The condition searches from the <strong>root</strong> of the resolution tree, not from
 * the current component. This means it can find slots anywhere in the equipment hierarchy.
 *
 * <pre>
 * iron-pickaxe (ROOT - search starts here)
 *   ├─ [head_slot] iron-pickaxe_head    ← findable via slot_type:head_slot
 *   │   └─ [material] iron              ← findable via slot_type:material
 *   ├─ [handle_slot] oak-handle         ← findable via slot_type:handle_slot
 *   └─ [gem_slot] fire_gem              ← findable via slot_type:gem_slot
 * </pre>
 *
 * <h2>Example: Blade Bonus from Fire Gem</h2>
 * <pre>{@code
 * // Blade gets +3 fire damage when a fire gem is installed:
 * {
 *   "on_hit": [{
 *     "selector": { "type": "forgero:single_target" },
 *     "effects": [{ "type": "forgero:fire", "duration": 60 }],
 *     "condition": {
 *       "static": [
 *         { "type": "forgero:slot_contains", "slot_type": "forgero:gem_slot", "tag": "forgero:fire_gem" }
 *       ]
 *     }
 *   }]
 * }
 * }</pre>
 *
 * <h2>Example: Metal Head Requirement</h2>
 * <pre>{@code
 * // Handle durability bonus only applies if head uses metal:
 * {
 *   "attributes": [{
 *     "type": "forgero:durability",
 *     "value": 1.2,
 *     "operator": "multiplication",
 *     "condition": {
 *       "static": [
 *         { "type": "forgero:in_slot_type", "slot_type": "forgero:handle_slot" },
 *         { "type": "forgero:slot_contains", "slot_type": "forgero:head_slot", "tag": "forgero:metal" }
 *       ]
 *     }
 *   }]
 * }
 * }</pre>
 *
 * @param type     The condition type identifier (always "forgero:slot_contains")
 * @param slotType The type of slot to search for (e.g., "forgero:gem_slot")
 * @param tag      The tag the slot's content must have
 * @see ResolutionContext#findInRoot(OpenIdentifier) for the underlying search
 * @see InSlotTypeCondition for checking which slot the current component is in
 */
public record SlotContainsCondition(OpenIdentifier type, OpenIdentifier slotType, OpenIdentifier tag) implements StaticCondition {
	public static final Codec<SlotContainsCondition> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("type").forGetter(SlotContainsCondition::type),
					// slot_type and tag are path-preserving classifiers (e.g. forgero:head_slot,
					// forgero:materials/types/metal); canonicalizing would break the exact match.
					CodecConstants.TAG_IDENTIFIER_CODEC.fieldOf("slot_type").forGetter(SlotContainsCondition::slotType),
					CodecConstants.TAG_IDENTIFIER_CODEC.fieldOf("tag").forGetter(SlotContainsCondition::tag)
			).apply(instance, SlotContainsCondition::new));

	@Override
	public boolean test(ResolutionContext context) {
		return context.findInRoot(slotType)
				.map(comp -> comp.getTags().contains(tag))
				.orElse(false);
	}
}
