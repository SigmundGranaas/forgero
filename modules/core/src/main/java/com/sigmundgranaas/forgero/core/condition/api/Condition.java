package com.sigmundgranaas.forgero.core.condition.api;

import java.util.Collections;
import java.util.List;

/**
 * A unified container for all conditional logic related to a property.
 *
 * <p>This record separates conditions into two categories based on when they are evaluated:
 * <ul>
 *   <li><strong>Static conditions</strong> - Evaluated once during the bake phase against
 *       component structure. Results are cached.</li>
 *   <li><strong>Dynamic conditions</strong> - Evaluated on every query against runtime
 *       game state. Cannot be cached.</li>
 * </ul>
 *
 * <p>For a property to be active, <strong>ALL</strong> conditions must pass (AND semantics).
 * Static conditions are evaluated first; if any fail, the property is excluded from the
 * baked result entirely. Dynamic conditions are only evaluated for properties that passed
 * static filtering.
 *
 * <h2>Condition Evaluation Flow</h2>
 * <pre>
 * Property with Condition
 *         ↓
 * ┌───────────────────────────────────────────────────┐
 * │ BAKE PHASE                                        │
 * │   For each staticCondition:                       │
 * │     if (!condition.test(resolutionContext))       │
 * │       → Property EXCLUDED from baked result       │
 * │   All static passed → Property INCLUDED in baked  │
 * └───────────────────────────────────────────────────┘
 *         ↓ (property in baked result)
 * ┌───────────────────────────────────────────────────┐
 * │ APPLY PHASE (per-query)                           │
 * │   For each dynamicCondition:                      │
 * │     if (!condition.test(dynamicContext))          │
 * │       → Property INACTIVE for this query          │
 * │   All dynamic passed → Property ACTIVE            │
 * └───────────────────────────────────────────────────┘
 * </pre>
 *
 * <h2>JSON Structure</h2>
 * <pre>{@code
 * {
 *   "condition": {
 *     "static": [
 *       { "type": "forgero:in_slot_type", "slot_type": "forgero:head_slot" },
 *       { "type": "forgero:self_has_tag", "tag": "forgero:metal" }
 *     ],
 *     "dynamic": [
 *       { "type": "forgero:is_sneaking" },
 *       { "type": "forgero:target_has_tag", "tag": "minecraft:undead" }
 *     ]
 *   }
 * }
 * }</pre>
 *
 * <h2>Common Patterns</h2>
 *
 * <h3>Slot-Specific Properties</h3>
 * <p>Apply an attribute only when in a specific slot:</p>
 * <pre>{@code
 * {
 *   "condition": {
 *     "static": [{ "type": "forgero:in_slot_type", "slot_type": "forgero:gem_slot" }]
 *   }
 * }
 * }</pre>
 *
 * <h3>Root-Only Properties</h3>
 * <p>Apply only to the final assembled item, not individual parts:</p>
 * <pre>{@code
 * {
 *   "condition": {
 *     "static": [{ "type": "forgero:is_root" }]
 *   }
 * }
 * }</pre>
 *
 * <h3>Situational Bonuses</h3>
 * <p>Bonus damage when sneaking:</p>
 * <pre>{@code
 * {
 *   "condition": {
 *     "dynamic": [{ "type": "forgero:is_sneaking" }]
 *   }
 * }
 * }</pre>
 *
 * <h3>Combined Static + Dynamic</h3>
 * <p>Gem in gem slot provides bonus vs undead:</p>
 * <pre>{@code
 * {
 *   "condition": {
 *     "static": [{ "type": "forgero:in_slot_type", "slot_type": "forgero:gem_slot" }],
 *     "dynamic": [{ "type": "forgero:target_has_tag", "tag": "minecraft:undead" }]
 *   }
 * }
 * }</pre>
 *
 * @param staticConditions  Conditions evaluated during bake phase (structure-based)
 * @param dynamicConditions Conditions evaluated during apply phase (runtime-based)
 * @see StaticCondition for available static condition types
 * @see DynamicCondition for available dynamic condition types
 */
public record Condition(
		List<StaticCondition> staticConditions,
		List<DynamicCondition> dynamicConditions
) {
	/**
	 * A condition that always passes - no static or dynamic requirements.
	 *
	 * <p>Properties with this condition (or no condition) are always active,
	 * enabling the fast-path optimization where base values are pre-computed
	 * during baking.
	 */
	public static final Condition ALWAYS_TRUE = new Condition(Collections.emptyList(), Collections.emptyList());

	/**
	 * Defensive copy constructor to ensure immutability.
	 */
	public Condition {
		staticConditions = List.copyOf(staticConditions);
		dynamicConditions = List.copyOf(dynamicConditions);
	}

	/**
	 * Returns true if this condition has no static conditions.
	 *
	 * <p>Properties with no static conditions are always included in the
	 * baked result (they may still have dynamic conditions).
	 *
	 * @return true if staticConditions is empty
	 */
	public boolean hasNoStaticConditions() {
		return staticConditions.isEmpty();
	}

	/**
	 * Returns true if this condition has no dynamic conditions.
	 *
	 * <p>Properties with no dynamic conditions can have their values
	 * fully pre-computed during baking (fast path).
	 *
	 * @return true if dynamicConditions is empty
	 */
	public boolean hasNoDynamicConditions() {
		return dynamicConditions.isEmpty();
	}

	/**
	 * Returns true if this condition always passes (no conditions at all).
	 *
	 * @return true if both static and dynamic condition lists are empty
	 */
	public boolean isAlwaysTrue() {
		return staticConditions.isEmpty() && dynamicConditions.isEmpty();
	}

	// ========================================================================
	// FACTORY METHODS
	// ========================================================================

	/**
	 * Creates a condition with only static conditions.
	 *
	 * <p>All provided conditions must pass for the condition to be satisfied.</p>
	 *
	 * @param conditions The static conditions (all must pass)
	 * @return A new Condition with only static conditions
	 */
	public static Condition ofStatic(StaticCondition... conditions) {
		return new Condition(List.of(conditions), List.of());
	}

	/**
	 * Creates a condition with only dynamic conditions.
	 *
	 * <p>All provided conditions must pass for the condition to be satisfied.</p>
	 *
	 * @param conditions The dynamic conditions (all must pass)
	 * @return A new Condition with only dynamic conditions
	 */
	public static Condition ofDynamic(DynamicCondition... conditions) {
		return new Condition(List.of(), List.of(conditions));
	}

	/**
	 * Creates a condition that combines multiple conditions (all must pass).
	 *
	 * <p>This merges all static and dynamic conditions from the provided conditions.</p>
	 *
	 * @param conditions The conditions to combine (AND semantics)
	 * @return A new Condition combining all input conditions
	 */
	public static Condition all(Condition... conditions) {
		if (conditions.length == 0) {
			return ALWAYS_TRUE;
		}
		if (conditions.length == 1) {
			return conditions[0];
		}

		java.util.List<StaticCondition> staticList = new java.util.ArrayList<>();
		java.util.List<DynamicCondition> dynamicList = new java.util.ArrayList<>();

		for (Condition c : conditions) {
			staticList.addAll(c.staticConditions());
			dynamicList.addAll(c.dynamicConditions());
		}

		return new Condition(staticList, dynamicList);
	}

	/**
	 * Creates a condition with the provided static conditions (all must pass).
	 *
	 * @param conditions The static conditions (all must pass)
	 * @return A new Condition with static conditions only
	 */
	public static Condition all(StaticCondition... conditions) {
		return ofStatic(conditions);
	}

	/**
	 * Creates a condition with the provided dynamic conditions (all must pass).
	 *
	 * @param conditions The dynamic conditions (all must pass)
	 * @return A new Condition with dynamic conditions only
	 */
	public static Condition all(DynamicCondition... conditions) {
		return ofDynamic(conditions);
	}

	/**
	 * Creates a condition that passes if ANY of the provided conditions pass.
	 *
	 * <p><strong>Important:</strong> This method is designed for combining conditions
	 * that have dynamic components. The OR evaluation happens at runtime.</p>
	 *
	 * <p><strong>How it works:</strong></p>
	 * <ul>
	 *   <li>All static conditions from inner Conditions are extracted and merged into
	 *       the outer Condition's static list (evaluated with AND semantics during baking)</li>
	 *   <li>Dynamic conditions are wrapped in an OrDynamicCondition (evaluated with OR
	 *       semantics at runtime)</li>
	 *   <li>If an inner Condition has no dynamic conditions, it's considered to "always
	 *       pass at runtime" since its static conditions passed during baking</li>
	 * </ul>
	 *
	 * <p><strong>Example:</strong></p>
	 * <pre>{@code
	 * // OR between two dynamic conditions
	 * Condition.any(
	 *     Condition.ofDynamic(isSneaking),
	 *     Condition.ofDynamic(isRaining)
	 * )
	 * // Passes if player is sneaking OR it's raining
	 * }</pre>
	 *
	 * @param conditions The conditions where at least one must pass
	 * @return A new Condition with OR semantics for dynamic evaluation
	 */
	public static Condition any(Condition... conditions) {
		if (conditions.length == 0) {
			return ALWAYS_TRUE;
		}
		if (conditions.length == 1) {
			return conditions[0];
		}

		// Extract all static conditions from inner Conditions - these are AND'd together
		// during baking. This ensures that if any inner condition has static requirements,
		// they must pass before the dynamic OR evaluation.
		java.util.List<StaticCondition> mergedStatic = new java.util.ArrayList<>();
		for (Condition c : conditions) {
			mergedStatic.addAll(c.staticConditions());
		}

		// Check if any inner condition has ONLY static conditions (no dynamic)
		// If so, we need to track this for the OR evaluation
		boolean hasStaticOnlyCondition = false;
		for (Condition c : conditions) {
			if (c.dynamicConditions().isEmpty() && !c.staticConditions().isEmpty()) {
				hasStaticOnlyCondition = true;
				break;
			}
		}

		// Wrap in an OrDynamicCondition for runtime OR evaluation
		return new Condition(
				mergedStatic,
				List.of(new OrDynamicCondition(List.of(conditions), hasStaticOnlyCondition))
		);
	}

	/**
	 * Internal dynamic condition that implements OR semantics.
	 *
	 * <p>At runtime, this checks if ANY of the inner conditions' dynamic conditions pass.
	 * If an inner condition has no dynamic conditions (only static), and those static
	 * conditions passed during baking, it counts as a pass for the OR evaluation.</p>
	 */
	public record OrDynamicCondition(
			List<Condition> conditions,
			boolean hasStaticOnlyCondition
	) implements DynamicCondition {
		// Evaluation semantics (implemented by the game-side runtime evaluator):
		// passes if ANY inner Condition passes, where an inner Condition with no dynamic
		// conditions counts as a pass (its static conditions already passed during
		// compilation), and one with dynamic conditions passes if ALL of them pass.
		@Override
		public com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier type() {
			return new com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier("forgero", "condition/or");
		}
	}
}
