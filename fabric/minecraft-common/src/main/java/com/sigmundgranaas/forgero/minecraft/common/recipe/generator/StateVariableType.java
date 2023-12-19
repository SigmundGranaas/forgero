package com.sigmundgranaas.forgero.minecraft.common.recipe.generator;

import java.util.List;

import com.sigmundgranaas.forgero.core.state.State;

public class StateVariableType implements VariableType<State> {
	private final List<State> values;

	public StateVariableType(List<State> values) {
		this.values = values;
	}

	@Override
	public List<State> values() {
		return values;
	}
}
