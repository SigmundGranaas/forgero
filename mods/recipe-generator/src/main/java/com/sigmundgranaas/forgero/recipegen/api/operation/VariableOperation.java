package com.sigmundgranaas.forgero.recipegen.api.operation;

/**
 * A function that transforms a variable value into a string for template substitution.
 *
 * <p>Operations are invoked when a template contains a placeholder with an operation suffix,
 * such as {@code ${material.id}} where "id" is the operation name.</p>
 *
 * <h2>Example Usage</h2>
 * <pre>{@code
 * VariableOperation itemIdOp = value -> {
 *     if (value instanceof Item item) {
 *         return Registries.ITEM.getId(item).toString();
 *     }
 *     return value.toString();
 * };
 * }</pre>
 */
@FunctionalInterface
public interface VariableOperation {

	/**
	 * Applies this operation to the given value.
	 *
	 * @param value The variable value
	 * @return The string representation
	 */
	String apply(Object value);

	/**
	 * Checks if this operation can handle the given value type.
	 *
	 * @param value The value to check
	 * @return true if this operation can handle the value
	 */
	default boolean matches(Object value) {
		return true;
	}

	/**
	 * Returns the priority of this operation.
	 * Higher priority operations are tried first when multiple match.
	 *
	 * @return The priority (default: 0)
	 */
	default int priority() {
		return 0;
	}
}
