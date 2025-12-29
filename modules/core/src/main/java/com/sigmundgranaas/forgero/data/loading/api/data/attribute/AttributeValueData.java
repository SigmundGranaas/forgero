package com.sigmundgranaas.forgero.data.loading.api.data.attribute;

import java.util.Optional;

/**
 * Represents either a simple numeric value or a full computation override
 * for an attribute within a batch.
 *
 * <p>This allows individual attributes to override the batch's default computation:</p>
 * <pre>
 * {
 *   "values": {
 *     "forgero:attack_damage": 4.0,  // Uses batch default computation
 *     "forgero:rarity": {             // Overrides with custom computation
 *       "value": 5,
 *       "operator": "add",
 *       "order": "end"
 *     }
 *   }
 * }
 * </pre>
 *
 * @param value              The numeric value
 * @param computationOverride Optional full computation that overrides batch defaults
 */
public record AttributeValueData(
		float value,
		Optional<ComputationData> computationOverride
) {
	/**
	 * Creates a simple value with no computation override.
	 */
	public static AttributeValueData simple(float value) {
		return new AttributeValueData(value, Optional.empty());
	}

	/**
	 * Creates a value with a full computation override.
	 */
	public static AttributeValueData withComputation(ComputationData computation) {
		return new AttributeValueData(computation.value(), Optional.of(computation));
	}
}
