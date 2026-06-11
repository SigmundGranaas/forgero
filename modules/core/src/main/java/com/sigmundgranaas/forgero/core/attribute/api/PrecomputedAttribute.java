package com.sigmundgranaas.forgero.core.attribute.api;


import java.util.List;

/**
 * Pre-computed base value from unconditional attributes, plus remaining
 * conditional attributes that require dynamic evaluation.
 *
 * <p>This structure enables O(1) queries for attributes without dynamic conditions
 * (the common case), while still supporting conditional attributes when needed.
 *
 * <h3>Example:</h3>
 * <pre>
 * // During bake phase, an iron pickaxe might produce:
 * PrecomputedAttribute(
 *     baseValue: 240.0f,           // durability from iron material (pre-computed)
 *     conditionalAttributes: [     // bonus when sneaking
 *         SimpleAttribute(durability, +50, condition=is_sneaking)
 *     ]
 * )
 *
 * // During apply phase:
 * precomputed.compute(context)
 *     → if sneaking: 290.0f (240 + 50)
 *     → if not sneaking: 240.0f (fast path)
 * </pre>
 *
 * @param baseValue             Pre-computed value from all unconditional attributes
 * @param conditionalAttributes Attributes with dynamic conditions requiring runtime evaluation
 */
public record PrecomputedAttribute(
		float baseValue,
		List<Attribute> conditionalAttributes
) {
	/**
	 * Zero value with no conditional attributes.
	 * Used when querying an attribute type that doesn't exist.
	 */
	public static final PrecomputedAttribute ZERO = new PrecomputedAttribute(0f, List.of());

	/**
	 * Defensive copy constructor to ensure immutability.
	 */
	public PrecomputedAttribute {
		conditionalAttributes = List.copyOf(conditionalAttributes);
	}

	/**
	 * @return The compiled base attribute value. Attributes carrying dynamic conditions do
	 * not contribute here: they are exposed via {@link #conditionalAttributes()} as data and
	 * applied by the game layer at context-bearing events (see {@code DynamicAttributes} in
	 * the mc common module — e.g. a dagger's "+damage while sneaking"). Runtime state never
	 * enters core; it only reads this compiled artifact.
	 */
	public float value() {
		return baseValue;
	}

	/**
	 * Returns true if this precomputed attribute has no conditional attributes.
	 */
	public boolean isFullyPrecomputed() {
		return conditionalAttributes.isEmpty();
	}
}
