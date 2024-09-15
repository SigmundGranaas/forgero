package com.sigmundgranaas.forgero.client.model.baked.strategy;

import java.util.Optional;
import java.util.function.Function;

import com.sigmundgranaas.forgero.core.model.Strategy;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.client.model.baked.BakedModelResult;

/**
 * A factory class for creating model strategies based on predefined operational modes. This class allows for
 * flexible generation of strategies that can either cache models, operate fully asynchronously, or combine both
 * behaviors depending on the provided configuration.
 * <p>
 * The factory simplifies the creation of complex model retrieval strategies by encapsulating the logic required
 * to assemble various components like caching layers and asynchronous operations.
 */
public class StrategyFactory {

	private final ModelStrategy baker;
	private final Strategy strategy;

	public StrategyFactory(ModelStrategy baker, Strategy strategy) {
		this.baker = baker;
		this.strategy = strategy;
	}

	/**
	 * Builds and returns a ModelStrategy according to the specified strategy configuration.
	 *
	 * @param state The state this strategy is intended to be used for.
	 * @return A ModelStrategy that conforms to the specified operational mode.
	 */
	public ModelStrategy build(State state) {
		return switch (strategy) {
			case FULLY_ASYNC -> fullyAsync(state);
			case PRE_BAKED -> PreBaked(state);
			case NO_ASYNC -> noAsync(state);
			case NO_CACHE -> baker;
		};
	}

	private ModelStrategy PreBaked(State state) {
		ModelStrategy defaultModel = defaultBaked(state);

		LayeredCachedSingleStateStrategy cache = layeredCache();

		ModelStrategy asyncBaker = backedAsyncStrategy(cache::getPreviousModel);

		return defaultModel.then(singleCache().then(cache.then(asyncBaker)));
	}

	private ModelStrategy noAsync(State state) {
		ModelStrategy defaultModel = defaultBaked(state);

		LayeredCachedSingleStateStrategy cache = layeredCache();

		return defaultModel.then(singleCache().then(cache.then(baker)));
	}

	private ModelStrategy defaultBaked(State state) {
		return new DefaultModelStrategy(baker.getModel(state, MatchContext.of()).orElseThrow(), state.hashCode());
	}

	private ModelStrategy fullyAsync(State state) {
		ModelStrategy lazy = lazyDefaultBaker(state);

		LayeredCachedSingleStateStrategy cache = layeredCache();

		ModelStrategy asyncBaker = backedAsyncStrategy(cache::getPreviousModel);

		return lazy.then(singleCache().then(cache.then(asyncBaker)));
	}

	private ModelStrategy lazyDefaultBaker(State state) {
		return new LazyDefaultModelStrategy(state.hashCode(), () -> asyncBaker().getModel(state, MatchContext.of()).orElseThrow());
	}

	private ModelStrategy asyncBaker() {
		return new AsyncModelStrategy(baker);
	}

	private SingleCachedStateStrategy singleCache() {
		return new SingleCachedStateStrategy();
	}

	private LayeredCachedSingleStateStrategy layeredCache() {
		return new LayeredCachedSingleStateStrategy();
	}

	private ModelStrategy backedAsyncStrategy(Function<Integer, Optional<BakedModelResult>> optionalGetter) {
		return new AsyncModelStrategy(baker, optionalGetter);
	}
}
