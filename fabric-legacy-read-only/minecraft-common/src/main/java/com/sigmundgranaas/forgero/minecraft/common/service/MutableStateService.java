package com.sigmundgranaas.forgero.minecraft.common.service;

import java.util.Collection;
import java.util.Optional;

import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.state.StateProvider;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

class MutableStateService implements StateService {
	private StateService stateService;

	public MutableStateService(StateService stateService) {
		this.stateService = stateService;
	}

	public void setStateService(StateService stateService) {
		this.stateService = stateService;
	}

	@Override
	public Optional<State> find(Item item) {
		return stateService.find(item);
	}

	@Override
	public Collection<StateProvider> all() {
		return stateService.all();
	}

	@Override
	public Optional<State> find(Identifier id) {
		return stateService.find(id);
	}

	@Override
	public Optional<State> convert(ItemStack stack) {
		return stateService.convert(stack);
	}

	@Override
	public Optional<ItemStack> convert(State state) {
		return stateService.convert(state);
	}

	@Override
	public ItemStack update(State state, ItemStack stack) {
		return stateService.update(state, stack);
	}

	@Override
	public boolean isInitialized() {
		return stateService.isInitialized();
	}

	@Override
	public StateMapper getMapper() {
		return stateService.getMapper();
	}

	@Override
	public StateService uncached() {
		return this.stateService.uncached();
	}

	@Override
	public Optional<State> find(String id) {
		return stateService.find(id);
	}
}
