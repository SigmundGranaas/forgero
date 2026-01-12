package com.sigmundgranaas.forgero.core.condition.predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.condition.api.StaticCondition;
import com.sigmundgranaas.forgero.core.property.context.ResolutionContext;
import com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants;

/**
 * A static condition that passes when the component has a sibling with a specific ID.
 *
 * <p>"Siblings" are components that share the same direct parent. This enables
 * synergy effects where components enhance each other when used together.
 *
 * <h2>Use Cases</h2>
 * <ul>
 *   <li><strong>Material synergies:</strong> Diamond blade gains bonus when paired with
 *       a gold handle</li>
 *   <li><strong>Set bonuses:</strong> Matching materials in different slots provide bonuses</li>
 *   <li><strong>Cross-slot effects:</strong> Head affects handle properties when both present</li>
 * </ul>
 *
 * <h2>Sibling Relationships</h2>
 * <pre>
 * iron-pickaxe (parent)
 *   ├─ iron-pickaxe_head    ← siblings with oak-handle and diamond_gem
 *   ├─ oak-handle           ← siblings with iron-pickaxe_head and diamond_gem
 *   └─ diamond_gem          ← siblings with iron-pickaxe_head and oak-handle
 *
 * iron-pickaxe_head (parent)
 *   ├─ pickaxe_schematic    ← siblings with iron
 *   └─ iron                 ← siblings with pickaxe_schematic
 * </pre>
 *
 * <h2>Example: Diamond + Gold Synergy</h2>
 * <pre>{@code
 * // Diamond blade gets bonus attack damage when paired with gold handle:
 * {
 *   "attributes": [{
 *     "type": "forgero:attack_damage",
 *     "value": 2.0,
 *     "condition": {
 *       "static": [
 *         { "type": "forgero:self_has_tag", "tag": "forgero:diamond" },
 *         { "type": "forgero:has_sibling", "sibling_id": "forgero:gold-handle" }
 *       ]
 *     }
 *   }]
 * }
 * }</pre>
 *
 * <p>Note: Both conditions must pass (AND logic) - the component must be diamond
 * AND have a gold handle sibling.
 *
 * @param type      The condition type identifier (always "forgero:has_sibling")
 * @param siblingId The exact ID of the required sibling component
 * @see ResolutionContext#getSiblings() for the underlying sibling lookup
 * @see SlotContainsCondition for checking slots anywhere in the tree
 */
public record HasSiblingCondition(OpenIdentifier type, OpenIdentifier siblingId) implements StaticCondition {
	public static final Codec<HasSiblingCondition> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("type").forGetter(HasSiblingCondition::type),
					CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("sibling_id").forGetter(HasSiblingCondition::siblingId)
			).apply(instance, HasSiblingCondition::new));

	@Override
	public boolean test(ResolutionContext context) {
		return context.getSiblings().stream().anyMatch(sibling -> sibling.id().equals(siblingId));
	}
}
