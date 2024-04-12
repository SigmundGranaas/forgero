package com.sigmundgranaas.forgero.minecraft.common.client.model.baked.strategy;

import java.util.Optional;
import java.util.function.Function;

import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.minecraft.common.client.model.baked.BakedModelResult;

public class StrategyFactory {
	public enum Strategy {
		FULLY_ASYNC,
		PRE_BAKED,
		NO_ASYNC,
		NO_CACHE
	}

	private final StateModelBaker baker;
	private final Strategy strategy;


	public StrategyFactory(StateModelBaker baker, Strategy strategy) {
		this.baker = baker;
		this.strategy = strategy;
	}

	public ModelStrategy build(State state) {
		return switch (strategy) {
			case FULLY_ASYNC -> fullyAsync(state);
			case PRE_BAKED -> PreBaked(state);
			case NO_ASYNC -> noAsync(state);
			case NO_CACHE -> baker;
		};
	}

	public ModelStrategy PreBaked(State state) {
		ModelStrategy defaultModel = defaultBaked(state);

		LayeredCachedSingleStateStrategy cache = layeredCache();

		ModelStrategy asyncBaker = backedAsyncStrategy(cache::getPreviousModel);

		return defaultModel.then(singleCache().then(cache.then(asyncBaker)));
	}

	public ModelStrategy noAsync(State state) {
		ModelStrategy defaultModel = defaultBaked(state);

		LayeredCachedSingleStateStrategy cache = layeredCache();

		return defaultModel.then(singleCache().then(cache.then(baker)));
	}

	private ModelStrategy defaultBaked(State state) {
		return new DefaultModelStrategy(baker.getModel(state, MatchContext.of()).orElseThrow(), state.hashCode());
	}

	public ModelStrategy fullyAsync(State state) {
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
