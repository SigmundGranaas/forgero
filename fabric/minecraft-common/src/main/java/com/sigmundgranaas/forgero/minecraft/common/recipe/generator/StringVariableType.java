package com.sigmundgranaas.forgero.minecraft.common.recipe.generator;

import java.util.List;

public class StringVariableType implements VariableType<String> {
	private final List<String> values;

	public StringVariableType(List<String> values) {
		this.values = values;
	}

	@Override
	public List<String> values() {
		return values;
	}
}
