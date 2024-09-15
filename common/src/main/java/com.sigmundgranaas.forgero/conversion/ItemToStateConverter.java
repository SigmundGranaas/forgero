package com.sigmundgranaas.forgero.conversion;

import java.util.Optional;

import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.item.StateItem;
import com.sigmundgranaas.forgero.service.StateService;

import net.minecraft.item.Item;
import net.minecraft.registry.Registry;


public class ItemToStateConverter implements Converter<Item, Optional<State>> {
	private final Registry<Item> registry;
	private final StateService stateFinder;

	public ItemToStateConverter(Registry<Item> registry, StateService stateFinder) {
		this.registry = registry;
		this.stateFinder = stateFinder;
	}

	@Override
	public Optional<State> convert(Item item) {
		if (item instanceof StateItem stateItem) {
			return Optional.of(stateItem.defaultState());
		} else {
			return stateFinder.find(registry.getId(item));
		}
	}
}
