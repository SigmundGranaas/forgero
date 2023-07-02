package com.sigmundgranaas.forgero.minecraft.common.item;

import com.sigmundgranaas.forgero.core.customdata.DataContainer;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;

import net.minecraft.item.ItemStack;

public interface StateItem extends State {
	State defaultState();

	default State dynamicState(ItemStack stack) {
		return StateService.INSTANCE.convert(stack).orElse(defaultState());
	}

	default State state(ItemStack stack) {
		return StateService.INSTANCE.convert(stack).orElse(defaultState());
	}

	default String name() {
		return defaultState().name();
	}

	default String nameSpace() {
		return defaultState().nameSpace();
	}

	default Type type() {
		return defaultState().type();
	}

	default boolean test(Matchable match, MatchContext context) {
		return defaultState().test(match, context);
	}

	@Override
	default DataContainer customData() {
		return defaultState().customData();
	}
}
