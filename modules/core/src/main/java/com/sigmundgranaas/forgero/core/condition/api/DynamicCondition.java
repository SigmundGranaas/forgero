package com.sigmundgranaas.forgero.core.condition.api;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.property.context.DynamicContext;

/**
 * A condition evaluated during the dynamic "apply" phase of property resolution.
 *
 * <p>Dynamic conditions test properties against <em>runtime game state</em>: player status,
 * target entity properties, world conditions, or any other information that can change
 * between queries. Unlike {@link StaticCondition}s, dynamic conditions are evaluated
 * on every query and cannot be cached.
 *
 * <h2>When Dynamic Conditions Are Evaluated</h2>
 * <pre>
 * Component Tree
 *       ↓
 * ComponentTraversal.traverse()
 *       ↓
 * DataTypeEngine.bake() ◄── Static conditions evaluated here (cached)
 *       ↓
 * Cached intermediate result (e.g., BakedAttributes)
 *       ↓
 * DataTypeEngine.apply() ◄── Dynamic conditions evaluated HERE (per-query)
 *       ↓
 * Final result
 * </pre>
 *
 * <h2>Performance Considerations</h2>
 * <p>Dynamic conditions are evaluated on every property query, so they should be:
 * <ul>
 *   <li>Lightweight - avoid expensive computations</li>
 *   <li>Side-effect free - must not modify game state</li>
 *   <li>Deterministic for the same context - consistent results</li>
 * </ul>
 *
 * <p>The two-phase resolution architecture optimizes for this by:
 * <ol>
 *   <li>Pre-computing base values from unconditional attributes during bake</li>
 *   <li>Only evaluating dynamic conditions for conditional attributes</li>
 *   <li>Short-circuiting when no dynamic conditions exist (fast path)</li>
 * </ol>
 *
 * <h2>Available Context Information</h2>
 * The {@link DynamicContext} is a type-safe heterogeneous container. Common keys include:
 * <ul>
 *   <li>{@code TARGET_TAGS} - Tags on the target entity (for OnHit effects)</li>
 *   <li>{@code IS_SNEAKING} - Whether the player is sneaking</li>
 *   <li>{@code IS_RAINING} - Current weather conditions</li>
 *   <li>{@code TIME_OF_DAY} - Current game time</li>
 * </ul>
 *
 * <p>Context keys are extensible - mods can define custom keys for their conditions.
 *
 * <h2>Example JSON Usage</h2>
 * <pre>{@code
 * {
 *   "attributes": [{
 *     "type": "forgero:attack_damage",
 *     "value": 3.0,
 *     "operator": "addition",
 *     "condition": {
 *       "dynamic": [
 *         { "type": "forgero:is_sneaking" }
 *       ]
 *     }
 *   }]
 * }
 * }</pre>
 *
 * <p>This attribute adds +3 attack damage, but only when the player is sneaking.
 *
 * <h2>Combining Static and Dynamic Conditions</h2>
 * <p>A property can have both static and dynamic conditions. The static conditions
 * are evaluated first during baking. If they fail, the property is excluded entirely.
 * If they pass, the property is included in the baked result, and its dynamic
 * conditions are evaluated on each query.
 *
 * <pre>{@code
 * {
 *   "condition": {
 *     "static": [{ "type": "forgero:in_slot_type", "slot_type": "forgero:gem_slot" }],
 *     "dynamic": [{ "type": "forgero:target_has_tag", "tag": "minecraft:undead" }]
 *   }
 * }
 * }</pre>
 *
 * <p>This condition requires the component to be in a gem slot (static, checked once)
 * AND the target to be undead (dynamic, checked per-hit).
 *
 * @see StaticCondition for structure-based conditions evaluated during baking
 * @see Condition for the unified condition container
 * @see DynamicContext for available runtime context
 */
public interface DynamicCondition {
	/**
	 * Tests this condition against the current runtime context.
	 *
	 * <p>This method is called on every property query for properties that
	 * have dynamic conditions. It should be lightweight and side-effect free.
	 *
	 * @param context The dynamic context containing runtime game state
	 * @return {@code true} if the condition passes and the property should be active,
	 *         {@code false} if the property should be skipped for this query
	 */
	boolean test(DynamicContext context);

	/**
	 * Returns the type identifier for this condition.
	 *
	 * <p>This identifier is used for codec dispatch during JSON deserialization.
	 * It should match the "type" field in JSON condition definitions.
	 *
	 * @return The condition type identifier (e.g., "forgero:is_sneaking", "forgero:target_has_tag")
	 */
	OpenIdentifier type();
}
