package com.sigmundgranaas.forgero.fabric.client.model.baked.strategy;

import java.util.Optional;

import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.minecraft.common.client.model.BakedModelResult;
import org.jetbrains.annotations.Nullable;

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
