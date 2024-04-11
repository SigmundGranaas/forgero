package com.sigmundgranaas.forgero.fabric.client.model.baked.strategy;

import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.minecraft.common.client.model.BakedModelResult;
import org.jetbrains.annotations.Nullable;

public class SingleCachedStateStrategy implements ModelStrategy {
	@Nullable
	private BakedModelResult result;

	int hashCode = 0;

	private final ModelStrategy fallback;

	public SingleCachedStateStrategy(ModelStrategy fallback) {
		this.fallback = fallback;
	}

	@Override
	public BakedModelResult getModel(State state, MatchContext context) {
		int hash = state.hashCode();
		if (result == null || hash != hashCode || !result.result().isValid(state, context)) {
			this.hashCode = hash;
			this.result = fallback.getModel(state, context);
		}
		return result;
	}
}
