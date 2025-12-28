package com.sigmundgranaas.forgero.recipegen.api.operation;

import java.util.function.Function;

/**
 * Factory for creating type-safe variable operations.
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * // Create an operation that only handles Item instances
 * VariableOperation itemIdOp = OperationFactory.forClass(
 *     Item.class,
 *     item -> Registries.ITEM.getId(item).toString()
 * );
 *
 * // Create a simple toString operation
 * VariableOperation toStringOp = OperationFactory.toString();
 * }</pre>
 */
public final class OperationFactory {

	private OperationFactory() {
	}

	/**
	 * Creates an operation that handles a specific class type.
	 *
	 * @param type      The class this operation handles
	 * @param operation The transformation function
	 * @param <T>       The value type
	 * @return A new VariableOperation
	 */
	public static <T> VariableOperation forClass(Class<T> type, Function<T, String> operation) {
		return new ClassBasedOperation<>(type, operation);
	}

	/**
	 * Creates an operation that handles any value using toString().
	 *
	 * @return A new VariableOperation using toString()
	 */
	public static VariableOperation asString() {
		return Object::toString;
	}

	/**
	 * Creates an operation with a specific priority.
	 *
	 * @param priority  The priority value
	 * @param operation The base operation
	 * @return A new VariableOperation with the specified priority
	 */
	public static VariableOperation withPriority(int priority, VariableOperation operation) {
		return new VariableOperation() {
			@Override
			public String apply(Object value) {
				return operation.apply(value);
			}

			@Override
			public boolean matches(Object value) {
				return operation.matches(value);
			}

			@Override
			public int priority() {
				return priority;
			}
		};
	}

	private static class ClassBasedOperation<T> implements VariableOperation {
		private final Class<T> type;
		private final Function<T, String> operation;

		ClassBasedOperation(Class<T> type, Function<T, String> operation) {
			this.type = type;
			this.operation = operation;
		}

		@Override
		@SuppressWarnings("unchecked")
		public String apply(Object value) {
			if (type.isInstance(value)) {
				return operation.apply((T) value);
			}
			return value.toString();
		}

		@Override
		public boolean matches(Object value) {
			return type.isInstance(value);
		}
	}
}
