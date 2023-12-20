package com.sigmundgranaas.forgero.generator.api.operation;

import java.util.function.Function;

import com.sigmundgranaas.forgero.core.registry.RankableConverter;


public class RankableClassBasedOperation<T> implements RankableConverter<T, String>, VariableOperation {
	private final Class<T> clazz;
	private final Function<T, String> operation;

	public RankableClassBasedOperation(Class<T> clazz, Function<T, String> operation) {
		this.clazz = clazz;
		this.operation = operation;
	}

	@Override
	public String apply(Object variable) {
		return VariableOperation.convert(variable, clazz, this::convert);
	}

	@Override
	public String convert(T entry) {
		return operation.apply(entry);
	}

	@Override
	public boolean matches(T entry) {
		return clazz.isAssignableFrom(entry.getClass());
	}
}
