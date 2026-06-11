package com.sigmundgranaas.forgero.common.runtime;

import com.sigmundgranaas.forgero.core.condition.api.DynamicCondition;

/**
 * The game-side evaluation contract for a {@link DynamicCondition}.
 *
 * <p>Core treats dynamic conditions as opaque data attached to compiled results; this
 * interface is how the game layer evaluates them. Predicate implementations (entity,
 * block, weather, damage, ...) implement this interface and are tested by
 * {@link RuntimeConditions} at the call sites that hold live game state.
 */
public interface EvaluableCondition extends DynamicCondition {
	/**
	 * @param context Runtime context built from live game state (entities, world, ...).
	 * @return true if the condition passes for this context.
	 */
	boolean test(DynamicContext context);
}
