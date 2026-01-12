package com.sigmundgranaas.forgero.core.attribute.api;

import com.sigmundgranaas.forgero.core.attribute.impl.computation.ComputationChain;
import com.sigmundgranaas.forgero.core.condition.api.Condition;
import com.sigmundgranaas.forgero.core.property.context.DynamicContext;

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
	 * Computes the final value by applying conditional attributes to the base.
	 *
	 * <p>Fast path: If there are no conditional attributes, returns baseValue immediately.
	 * Otherwise, evaluates dynamic conditions and applies active conditionals via ComputationChain.
	 *
	 * @param context The dynamic context for evaluating conditions
	 * @return The final computed attribute value
	 */
	public float compute(DynamicContext context) {
		if (conditionalAttributes.isEmpty()) {
			return baseValue;  // Fast path: no conditions to evaluate
		}

		List<Attribute> active = conditionalAttributes.stream()
				.filter(attr -> testDynamicConditions(attr, context))
				.toList();

		if (active.isEmpty()) {
			return baseValue;
		}

		return new ComputationChain(active).compute(baseValue);
	}

	/**
	 * Tests whether an attribute's dynamic conditions pass in the given context.
	 *
	 * @param attr    The attribute to test
	 * @param context The dynamic context
	 * @return true if all dynamic conditions pass (or if there are none)
	 */
	private boolean testDynamicConditions(Attribute attr, DynamicContext context) {
		return attr.condition()
				.map(Condition::dynamicConditions)
				.map(conditions -> conditions.stream().allMatch(cond -> cond.test(context)))
				.orElse(true);
	}

	/**
	 * Returns true if this precomputed attribute has no conditional attributes.
	 * In this case, {@link #compute(DynamicContext)} will always return {@link #baseValue}.
	 */
	public boolean isFullyPrecomputed() {
		return conditionalAttributes.isEmpty();
	}
}
