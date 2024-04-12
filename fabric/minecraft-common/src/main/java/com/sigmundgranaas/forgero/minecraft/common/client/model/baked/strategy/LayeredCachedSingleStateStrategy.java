package com.sigmundgranaas.forgero.minecraft.common.client.model.baked.strategy;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.sigmundgranaas.forgero.core.cache.LayeredMatchedOptionCache;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.minecraft.common.client.model.baked.BakedModelResult;

public class LayeredCachedSingleStateStrategy implements ModelStrategy {
	private ModelStrategy fallBack;
	private final LayeredMatchedOptionCache<Integer, Optional<BakedModelResult>> modelCache;

	public LayeredCachedSingleStateStrategy() {
		this.fallBack = (state, ctx) -> Optional.empty();
		this.modelCache = new LayeredMatchedOptionCache<>(Duration.ofMinutes(1), 20);
	}

	@Override
	public Optional<BakedModelResult> getModel(State state, MatchContext context) {
		int stateHash = state.hashCode();
		Supplier<Optional<BakedModelResult>> callback = () -> fallBack.getModel(state, context);
		Predicate<Optional<BakedModelResult>> predicate = model -> model.isPresent() && model.get().result().isValid(state, context);
		return modelCache.get(stateHash, callback, predicate);
	}

	public Optional<BakedModelResult> getPreviousModel(Integer key) {
		var opt = modelCache.getPrevious(key);
		return opt.orElseGet(Optional::empty);
	}

	@Override
	public ModelStrategy then(ModelStrategy strategy) {
		this.fallBack = strategy;
		return this;
	}
}
