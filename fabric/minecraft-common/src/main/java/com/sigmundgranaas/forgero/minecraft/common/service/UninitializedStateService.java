package com.sigmundgranaas.forgero.minecraft.common.service;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.state.StateProvider;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class UninitializedStateService implements StateService {
	@Override
	public Optional<State> find(Item item) {
		Forgero.LOGGER.warn("Forgero is not initialized yet. Please wait for the mod to finish loading.");
		return Optional.empty();
	}

	@Override
	public Collection<StateProvider> all() {
		return Collections.emptyList();
	}

	@Override
	public Optional<State> find(Identifier id) {
		Forgero.LOGGER.warn("Forgero is not initialized yet. Please wait for the mod to finish loading.");
		return Optional.empty();
	}

	@Override
	public Optional<State> convert(ItemStack stack) {
		Forgero.LOGGER.warn("Forgero is not initialized yet. Please wait for the mod to finish loading.");
		return Optional.empty();
	}

	@Override
	public Optional<ItemStack> convert(State state) {
		return Optional.empty();
	}

	@Override
	public boolean isInitialized() {
		return false;
	}

	@Override
	public StateMapper getMapper() {
		return new StateMapper();
	}

	@Override
	public StateService uncached() {
		return this;
	}

	@Override
	public Optional<State> find(String id) {
		Forgero.LOGGER.warn("Forgero is not initialized yet. Please wait for the mod to finish loading.");
		return Optional.empty();
	}
}
