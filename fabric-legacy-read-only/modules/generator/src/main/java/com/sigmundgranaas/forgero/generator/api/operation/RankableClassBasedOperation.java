package com.sigmundgranaas.forgero.generator.api.operation;

import com.sigmundgranaas.forgero.core.registry.RankableConverter;

import java.util.function.Function;


/**
 * RankableClassBasedOperation is a helper object to make it easier to create a VariableOperation for a generic input class type.
 * See {@link OperationFactory} for a more convenient way to create operations.
 * <p>
 * This class' only requirement is that the input class type is the same as the class type of the operation.
 *
 * @param <T>
 */
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
