package com.sigmundgranaas.forgero.client.model.baked.strategy;

import java.util.Optional;

import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.client.model.baked.BakedModelResult;
import org.jetbrains.annotations.Nullable;

/**
 * A caching implementation of ModelStrategy that caches a single model result.
 * It leverages a simple cache to remember the last successful model fetched based on a state's hash code.
 * If the state changes or if the cached model is no longer valid, the strategy falls back to a secondary strategy.
 * <p>
 * This strategy is useful when model retrieval is costly and the state changes infrequently,
 * or when the validity of the model depends on the context that may occasionally change. Usually only a single model will correspond
 */
public class SingleCachedStateStrategy implements ModelStrategy {
	@Nullable
	private BakedModelResult result;

	int hashCode = 0;

	private ModelStrategy fallback;

	public SingleCachedStateStrategy() {
		this.fallback = (s, c) -> Optional.empty();
	}

	@Override
	public Optional<BakedModelResult> getModel(State state, MatchContext context) {
		int hash = state.hashCode();
		if (result == null || hash != hashCode || !result.result().isValid(state, context)) {
			this.hashCode = hash;
			Optional<BakedModelResult> model = fallback.getModel(state, context);
			if (model.isPresent()) {
				this.result = model.get();
			} else {
				return Optional.empty();
			}
		}
		return Optional.of(result);
	}

	@Override
	public ModelStrategy then(ModelStrategy strategy) {
		this.fallback = strategy;
		return this;
	}
}
