package com.sigmundgranaas.forgero.minecraft.common.client.model.baked.strategy;

import java.util.Optional;

import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.minecraft.common.client.model.baked.BakedModelResult;
import org.jetbrains.annotations.Nullable;

public class AsyncDefaultedModelStrategy implements ModelStrategy {

	@Nullable
	private ModelStrategy defaultModel;

	private final ModelStrategy strategy;

	public AsyncDefaultedModelStrategy(ModelStrategy strategy) {
		this.strategy = strategy;
	}

	@Override
	public Optional<BakedModelResult> getModel(State state, MatchContext context) {
		if (defaultModel != null) {
			return defaultModel.getModel(state, context);
		} else {
			Optional<BakedModelResult> async = strategy.getModel(state, context);
			async.ifPresent(result -> this.defaultModel = new DefaultModelStrategy(result, state.hashCode()));
			return Optional.ofNullable(defaultModel).flatMap(defaultModel -> defaultModel.getModel(state, context));
		}
	}
}
