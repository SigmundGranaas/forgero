package com.sigmundgranaas.forgero.minecraft.common.service;

import java.util.Optional;

import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.state.State;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class UninitializedStateService extends StateService {
	@Override
	public Optional<State> find(Item item) {
		Forgero.LOGGER.warn("Forgero is not initialized yet. Please wait for the mod to finish loading.");
		return Optional.empty();
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
	public boolean isInitialized() {
		return false;
	}

	@Override
	public Optional<State> find(String id) {
		Forgero.LOGGER.warn("Forgero is not initialized yet. Please wait for the mod to finish loading.");
		return Optional.empty();
	}
}
