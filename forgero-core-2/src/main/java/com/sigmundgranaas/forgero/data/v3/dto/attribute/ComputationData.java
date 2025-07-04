package com.sigmundgranaas.forgero.data.v3.dto.attribute;

/**
 * A normalized, intermediate representation of an attribute's computation logic.
 * All computation shortcuts in JSON are expanded into this format.
 *
 * @param value    The numerical value of the computation.
 * @param operator The identifier for the mathematical operator (e.g., "forgero:addition").
 * @param order    The identifier for the calculation order (e.g., "forgero:base").
 */
public record ComputationData(
		float value,
		String operator,
		String order
) {
}
