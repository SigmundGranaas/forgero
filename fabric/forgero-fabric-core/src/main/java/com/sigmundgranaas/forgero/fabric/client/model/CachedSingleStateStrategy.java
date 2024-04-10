package com.sigmundgranaas.forgero.fabric.client.model;

import java.time.Duration;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.sigmundgranaas.forgero.core.cache.LayeredMatchedOptionCache;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.minecraft.common.client.model.BakedModelResult;

public class CachedSingleStateStrategy implements ModelStrategy {
	private final ModelStrategy strategy;
	private final LayeredMatchedOptionCache<Integer, BakedModelResult> modelCache;

	public CachedSingleStateStrategy(ModelStrategy strategy) {
		this.strategy = strategy;
		this.modelCache = new LayeredMatchedOptionCache<>(Duration.ofMinutes(1), 20);
	}

	@Override
	public BakedModelResult getModel(State state, MatchContext context) {
		int stateHash = state.hashCode();
		Supplier<BakedModelResult> callback = () -> strategy.getModel(state, context);
		Predicate<BakedModelResult> predicate = model -> model.result().isValid(state, context);
		return modelCache.get(stateHash, callback, predicate);
	}
}
