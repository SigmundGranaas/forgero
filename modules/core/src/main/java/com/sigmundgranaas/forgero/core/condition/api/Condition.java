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
}
