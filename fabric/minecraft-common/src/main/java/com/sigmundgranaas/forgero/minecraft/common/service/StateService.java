package com.sigmundgranaas.forgero.minecraft.common.service;

import java.util.Optional;

import com.sigmundgranaas.forgero.core.registry.StateFinder;
import com.sigmundgranaas.forgero.core.state.State;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public interface StateService extends StateFinder {
	StateService INSTANCE = new MutableStateService(new UninitializedStateService());

	static void initialize(StateService stateService) {
		if (INSTANCE.isInitialized()) {
			throw new IllegalStateException("StateService is already initialized.");
		}
		if (INSTANCE instanceof MutableStateService mutableStateService) {
			mutableStateService.setStateService(stateService);
		}
	}

	Optional<State> find(Item item);

	Optional<State> find(Identifier id);

	Optional<State> convert(ItemStack stack);

	boolean isInitialized();
}
