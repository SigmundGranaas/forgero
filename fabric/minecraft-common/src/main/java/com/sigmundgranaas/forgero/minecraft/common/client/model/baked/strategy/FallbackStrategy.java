package com.sigmundgranaas.forgero.minecraft.common.client.model.baked.strategy;

import java.util.Optional;

import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.minecraft.common.client.model.baked.BakedModelResult;

public class FallbackStrategy implements ModelStrategy {
	private final ModelStrategy base;
	private final ModelStrategy fallback;

	public FallbackStrategy(ModelStrategy base, ModelStrategy fallback) {
		this.base = base;
		this.fallback = fallback;
	}

	@Override
	public Optional<BakedModelResult> getModel(State state, MatchContext context) {
		return base.getModel(state, context).or(() -> fallback.getModel(state, context));
	}
}
