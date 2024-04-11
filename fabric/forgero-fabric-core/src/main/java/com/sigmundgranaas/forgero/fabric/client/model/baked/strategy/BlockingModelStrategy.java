package com.sigmundgranaas.forgero.fabric.client.model.baked.strategy;


import static com.sigmundgranaas.forgero.minecraft.common.client.forgerotool.model.implementation.EmptyBakedModel.EMPTY;

import com.sigmundgranaas.forgero.core.model.ModelResult;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.fabric.client.model.StateModelBaker;
import com.sigmundgranaas.forgero.minecraft.common.client.model.BakedModelResult;

public class BlockingModelStrategy implements ModelStrategy {
	private final StateModelBaker baker;

	public BlockingModelStrategy(StateModelBaker baker) {
		this.baker = baker;
	}

	@Override
	public BakedModelResult getModel(State state, MatchContext context) {
		return baker.bake(state, context).orElseGet(() -> new BakedModelResult(ModelResult.EMPTY, EMPTY));
	}
}
