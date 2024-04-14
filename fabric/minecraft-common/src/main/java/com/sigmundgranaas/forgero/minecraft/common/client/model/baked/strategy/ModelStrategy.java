package com.sigmundgranaas.forgero.minecraft.common.client.model.baked.strategy;

import java.util.Optional;

import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.minecraft.common.client.model.baked.BakedModelResult;

/**
 * Defines the contract for a strategy to retrieve models based on state and context.
 * This functional interface requires implementing a method to fetch a model optionally,
 * allowing for flexible model retrieval logic that might not always result in a model being available.
 * <p>
 * It also includes a default method to chain model retrieval strategies, enabling a fallback mechanism.
 */
@FunctionalInterface
public interface ModelStrategy {
	/**
	 * Attempts to retrieve a model based on the provided state and context.
	 *
	 * @param state   The state for which a model needs to be retrieved.
	 * @param context The context in which the model is requested, providing additional information necessary for model retrieval.
	 * @return An Optional containing the model if retrieval is successful; otherwise, an empty Optional.
	 */
	Optional<BakedModelResult> getModel(State state, MatchContext context);

	/**
	 * Combines this strategy with another strategy, creating a fallback mechanism.
	 * If this strategy does not result in a model, the provided strategy is used.
	 *
	 * @param strategy The fallback strategy to use if this strategy does not succeed.
	 * @return A new ModelStrategy that first tries this strategy, then the provided fallback strategy.
	 */
	default ModelStrategy then(ModelStrategy strategy) {
		return new FallbackStrategy(this, strategy);
	}
}
