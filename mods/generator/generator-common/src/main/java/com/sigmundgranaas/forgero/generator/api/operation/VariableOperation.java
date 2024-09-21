package com.sigmundgranaas.forgero.generator.api.operation;

import java.util.Optional;
import java.util.function.Function;

/**
 * A VariableOperation is a function that takes any object and returns a string.
 * VariableOperations are often used alongside OperationFactory and RankableClassBasedOperation to create operations for specific classes.
 * <p>
 * Operations are usually tailored to specific classes, but can be used for any class.
 * The goal of the operations to dynamically generate replacement strings in dynamically generated JsonObjects.
 */
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
