package com.sigmundgranaas.forgero.fabric.client.model.baked.strategy;

import java.util.Optional;
import java.util.function.Supplier;

import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.minecraft.common.client.model.BakedModelResult;
import org.jetbrains.annotations.Nullable;

public class LazyDefaultModelStrategy implements ModelStrategy {
	@Nullable
	private BakedModelResult result;

	private final int code;

	private final Supplier<BakedModelResult> model;

	public LazyDefaultModelStrategy(int code, Supplier<BakedModelResult> model) {
		this.code = code;
		this.model = model;
	}

	@Override
	public Optional<BakedModelResult> getModel(State state, MatchContext context) {
		if (code == state.hashCode() && result != null) {
			return Optional.of(result);
		} else if (code == state.hashCode() && result == null) {
			this.result = model.get();
			return Optional.of(result);
		}

		return Optional.empty();
	}
}
