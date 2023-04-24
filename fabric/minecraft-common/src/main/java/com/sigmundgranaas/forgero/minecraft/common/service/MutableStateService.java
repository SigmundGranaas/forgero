package com.sigmundgranaas.forgero.minecraft.common.service;

import java.util.Optional;

import com.sigmundgranaas.forgero.core.state.State;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

class MutableStateService extends StateService {
	private StateService stateService;

	public MutableStateService(StateService stateService) {
		this.stateService = stateService;
	}

	public void setStateService(StateService stateService) {
		this.stateService = stateService;
	}

	@Override
	Optional<State> find(Item item) {
		return stateService.find(item);
	}

	@Override
	Optional<State> find(Identifier id) {
		return stateService.find(id);
	}

	@Override
	Optional<State> convert(ItemStack stack) {
		return stateService.convert(stack);
	}

	@Override
	boolean isInitialized() {
		return stateService.isInitialized();
	}

	@Override
	public Optional<State> find(String id) {
		return stateService.find(id);
	}
}
