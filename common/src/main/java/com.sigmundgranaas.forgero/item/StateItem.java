package com.sigmundgranaas.forgero.item;

import java.util.List;

import com.sigmundgranaas.forgero.core.customdata.DataContainer;
import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;
import com.sigmundgranaas.forgero.service.StateService;
import org.jetbrains.annotations.NotNull;

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

	@Override
	default State strip() {
		return defaultState();
	}

	@Override
	default @NotNull List<Property> getRootProperties(Matchable target, MatchContext context) {
		return defaultState().getRootProperties(target, context);
	}
}