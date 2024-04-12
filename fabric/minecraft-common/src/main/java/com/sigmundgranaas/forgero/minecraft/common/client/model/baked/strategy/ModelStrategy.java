package com.sigmundgranaas.forgero.minecraft.common.client.model.baked.strategy;

import java.util.Optional;

import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.minecraft.common.client.model.baked.BakedModelResult;

@FunctionalInterface
public interface ModelStrategy {
	Optional<BakedModelResult> getModel(State state, MatchContext context);

	default ModelStrategy then(ModelStrategy strategy) {
		return new FallbackStrategy(this, strategy);
	}
}
