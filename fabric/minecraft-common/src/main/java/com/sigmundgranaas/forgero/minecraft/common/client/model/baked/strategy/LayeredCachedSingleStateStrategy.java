package com.sigmundgranaas.forgero.minecraft.common.client.model.baked.strategy;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.sigmundgranaas.forgero.core.cache.LayeredMatchedOptionCache;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.minecraft.common.client.model.baked.BakedModelResult;

/**
 * A caching strategy for model retrieval that layers caching on top of a fallback model strategy.
 * This strategy uses a {@link LayeredMatchedOptionCache} to cache and retrieve {@link BakedModelResult}
 * instances based on the state hash code, applying custom validation for each model to ensure the cached models
 * are still valid before returning them. (Usually dependent on player or world data) If the cache does not have a valid entry, or if the entry
 * does not meet the validation criteria, this strategy falls back to another model strategy for retrieval.
 * <p>
 * This strategy is particularly useful in environments where model retrieval is costly and
 * model states do not change frequently, but validation of model relevance is still necessary.
 *
 * @implNote The caching mechanism depends on the state's hash code as a key for retrieving
 * and storing models, and a time-to-live parameter to ensure models do not become stale.
 */
public class LayeredCachedSingleStateStrategy implements ModelStrategy {
	private ModelStrategy fallBack;
	private final LayeredMatchedOptionCache<Integer, Optional<BakedModelResult>> modelCache;

	public LayeredCachedSingleStateStrategy() {
		this.fallBack = (state, ctx) -> Optional.empty();
		this.modelCache = new LayeredMatchedOptionCache<>(Duration.ofMinutes(1), 20);
	}

	/**
	 * Retrieves a model based on the provided state and context. If the model is available in the cache
	 * and passes the validation, it is returned directly from the cache. Otherwise, the fallback
	 * strategy is invoked to provide the model.
	 *
	 * @param state   The state whose model is being retrieved.
	 * @param context The context in which the model is requested, used for validation and fallback retrieval.
	 * @return An {@link Optional<BakedModelResult>} containing the model if available and valid; otherwise,
	 * it may return an empty {@link Optional} or a model retrieved through the fallback strategy.
	 */
	@Override
	public Optional<BakedModelResult> getModel(State state, MatchContext context) {
		int stateHash = state.hashCode();
		Supplier<Optional<BakedModelResult>> callback = () -> fallBack.getModel(state, context);
		Predicate<Optional<BakedModelResult>> predicate = model -> model.isPresent() && model.get().result().isValid(state, context);
		return modelCache.get(stateHash, callback, predicate);
	}

	/**
	 * Retrieves the model corresponding to a previous state from the cache, if available.
	 * This can be used to access models for past states in cases where we would rather reuse the previous model instead of
	 * blocking while creating a new one, or returning an empty model while the new one is being baked.
	 * <p>
	 * This method can be used as a fallback for something like the {@link AsyncModelStrategy}
	 *
	 * @param key The hash code of the state whose previous model is requested.
	 * @return An {@link Optional<BakedModelResult>} containing the previous model if available; otherwise, empty.
	 */
	public Optional<BakedModelResult> getPreviousModel(Integer key) {
		var opt = modelCache.getPrevious(key);
		return opt.orElseGet(Optional::empty);
	}


	/**
	 * Sets the fallback strategy used when the cache does not contain a valid model.
	 *
	 * @param strategy A {@link ModelStrategy} to be used as fallback when needed.
	 * @return The current instance of {@link LayeredCachedSingleStateStrategy}, allowing for method chaining.
	 */
	@Override
	public ModelStrategy then(ModelStrategy strategy) {
		this.fallBack = strategy;
		return this;
	}
}
