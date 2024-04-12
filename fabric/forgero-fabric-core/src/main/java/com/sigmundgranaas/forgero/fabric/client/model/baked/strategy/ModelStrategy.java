package com.sigmundgranaas.forgero.fabric.client.model.baked.strategy;

import java.util.Optional;

import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.minecraft.common.client.model.BakedModelResult;

@FunctionalInterface
public interface ModelStrategy {
	Optional<BakedModelResult> getModel(State state, MatchContext context);

	default ModelStrategy then(ModelStrategy strategy) {
		return new FallbackStrategy(this, strategy);
	}
}
