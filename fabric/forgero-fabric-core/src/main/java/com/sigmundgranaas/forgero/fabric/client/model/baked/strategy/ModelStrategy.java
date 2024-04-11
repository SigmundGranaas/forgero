package com.sigmundgranaas.forgero.fabric.client.model.baked.strategy;

import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.minecraft.common.client.model.BakedModelResult;

public interface ModelStrategy {
	BakedModelResult getModel(State state, MatchContext context);
}
