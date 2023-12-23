package com.sigmundgranaas.forgero.generator.api.operation;

import com.sigmundgranaas.forgero.core.registry.RankableConverter;

import java.util.function.Function;

/**
 * A basic factory for creating RankableClassBasedOperation objects with the same class target.
 *
 * @param <T>
 */
public class OperationFactory<T> {
	private final Class<T> operationClass;

	public OperationFactory(Class<T> operationClass) {
		this.operationClass = operationClass;
	}

	public RankableConverter<Object, String> build(Function<T, String> operation) {
		//noinspection unchecked,Convert2Diamond
		return (RankableConverter<Object, String>) new RankableClassBasedOperation<T>(operationClass, operation);
	}
}
