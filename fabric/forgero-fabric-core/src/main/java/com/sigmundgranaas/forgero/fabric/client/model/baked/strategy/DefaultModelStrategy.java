package com.sigmundgranaas.forgero.fabric.client.model.baked.strategy;

import java.util.Optional;

import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.minecraft.common.client.model.BakedModelResult;

public class DefaultModelStrategy implements ModelStrategy {
	private final BakedModelResult result;
	private final int code;

	public DefaultModelStrategy(BakedModelResult result, int code) {
		this.result = result;
		this.code = code;
	}

	@Override
	public Optional<BakedModelResult> getModel(State state, MatchContext context) {
		if (code == state.hashCode()) {
			return Optional.of(result);
		} else {
			return Optional.empty();
		}
	}
}
