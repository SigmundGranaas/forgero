package com.sigmundgranaas.forgero.minecraft.common.conversion;

import com.sigmundgranaas.forgero.minecraft.common.item.StateItem;
import com.sigmundgranaas.forgero.core.state.State;

import net.minecraft.item.Item;

import java.util.Optional;
import java.util.function.Function;

public class ItemToStateConverter implements Converter<Item, Optional<State>> {
	private final Function<Item, Optional<State>> stateFinder;

	public ItemToStateConverter(Function<Item, Optional<State>> stateFinder) {
		this.stateFinder = stateFinder;
	}

	@Override
	public Optional<State> convert(Item item) {
		if (item instanceof StateItem stateItem) {
			return Optional.of(stateItem.defaultState());
		} else {
			return stateFinder.apply(item);
		}
	}
}
