package com.sigmundgranaas.forgero.minecraft.common.client.model.baked.strategy;

import java.util.Optional;

import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.minecraft.common.client.model.baked.BakedModelResult;

/**
 * Implements a fallback mechanism for model retrieval strategies. If the primary strategy fails to
 * retrieve a model, this strategy attempts to retrieve a model using a fallback strategy.
 * This class ensures that there is a secondary option for model retrieval if the primary method fails.
 */
public class FallbackStrategy implements ModelStrategy {
	private final ModelStrategy base;
	private final ModelStrategy fallback;

	public FallbackStrategy(ModelStrategy base, ModelStrategy fallback) {
		this.base = base;
		this.fallback = fallback;
	}

	/**
	 * Retrieves a model by first attempting the primary strategy and then, if necessary, the fallback strategy.
	 *
	 * @param state   The state for which the model is being retrieved.
	 * @param context The context of the model request, possibly containing additional criteria or information.
	 * @return An Optional containing the model if either strategy is successful; otherwise, an empty Optional.
	 */
	@Override
	public Optional<BakedModelResult> getModel(State state, MatchContext context) {
		return base.getModel(state, context).or(() -> fallback.getModel(state, context));
	}
}
