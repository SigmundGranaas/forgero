package com.sigmundgranaas.forgero.minecraft.common.service;

import java.util.Collection;
import java.util.Optional;

import javax.annotation.Nullable;

import com.sigmundgranaas.forgero.core.registry.StateFinder;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.state.StateProvider;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

/**
 * Service for finding states from items and vice versa.
 * <p>
 * Will be initialized by Forgero.
 * <p>
 * Can convert between states and items.
 */
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

	Collection<StateProvider> all();

	Optional<State> find(Identifier id);

	Optional<State> convert(ItemStack stack);

	Optional<ItemStack> convert(@Nullable State state);

	ItemStack update(State state, ItemStack stack);

	boolean isInitialized();

	StateMapper getMapper();

	StateService uncached();
}
