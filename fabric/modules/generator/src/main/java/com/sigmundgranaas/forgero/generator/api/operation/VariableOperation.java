package com.sigmundgranaas.forgero.generator.api.operation;

import java.util.Optional;
import java.util.function.Function;

@FunctionalInterface
public interface VariableOperation {
	static <T> String convert(Object variable, Class<T> type, Function<T, String> operation) {
		return convert(variable, type)
				.map(operation)
				.orElseThrow(() -> new IllegalArgumentException("Variable provider does not supply a " + type.getName()));
	}

	static <T> Optional<T> convert(Object variable, Class<T> type) {
		if (variable.getClass().isAssignableFrom(type)) {
			return Optional.of(type.cast(variable));
		}
		return Optional.empty();
	}

	static <T> VariableOperation operation(Class<T> type, Function<T, String> operation) {
		return variableProvider -> convert(variableProvider, type, operation);
	}

	String apply(Object variable);
}
