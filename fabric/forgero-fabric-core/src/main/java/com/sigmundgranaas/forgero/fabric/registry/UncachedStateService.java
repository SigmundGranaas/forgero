package com.sigmundgranaas.forgero.fabric.registry;

import static com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.NbtConstants.FORGERO_IDENTIFIER;

import java.util.Collection;
import java.util.Optional;

import com.sigmundgranaas.forgero.core.state.Composite;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.state.StateProvider;
import com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.CompoundParser;
import com.sigmundgranaas.forgero.minecraft.common.service.StateMapper;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class UncachedStateService implements StateService {
	private final StateService stateService;

	public UncachedStateService(StateService fallBack) {
		this.stateService = fallBack;
	}

	@Override
	public Optional<State> find(Item item) {
		return copy(stateService.find(item));
	}

	@Override
	public Collection<StateProvider> all() {
		return stateService.all();
	}

	@Override
	public Optional<State> find(Identifier id) {
		return copy(stateService.find(id));
	}

	@Override
	public Optional<State> convert(ItemStack stack) {
		if (stack.hasNbt() && stack.getNbt().contains(FORGERO_IDENTIFIER)) {
			return CompoundParser.STATE_PARSER.parse(stack.getOrCreateNbt().getCompound(FORGERO_IDENTIFIER));
		}
		return stateService.convert(stack.copy());
	}

	@Override
	public Optional<ItemStack> convert(State state) {
		return stateService.convert(state);
	}

	@Override
	public ItemStack update(State state, ItemStack stack) {
		return stateService.update(state, stack);
	}

	private State copy(State state) {
		if (state instanceof Composite composite) {
			return composite.copy();
		}
		return state;
	}

	private Optional<State> copy(Optional<State> state) {
		return state.map(this::copy);
	}

	@Override
	public boolean isInitialized() {
		return true;
	}

	@Override
	public StateMapper getMapper() {
		return stateService.getMapper();
	}

	@Override
	public StateService uncached() {
		return this;
	}

	@Override
	public Optional<State> find(String id) {
		return copy(stateService.find(id));
	}
}
