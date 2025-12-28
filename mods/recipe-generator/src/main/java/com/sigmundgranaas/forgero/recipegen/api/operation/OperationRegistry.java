package com.sigmundgranaas.forgero.recipegen.api.operation;

import java.util.Optional;
import java.util.function.Function;

/**
 * Registry for variable operations.
 *
 * <p>Operations transform variable values into strings during template processing.
 * They are organized into groups (typically by the variable's type ID) and
 * operation names.</p>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * OperationRegistry registry = RecipeGenApi.getInstance().operations();
 *
 * // Register an operation for Minecraft items
 * registry.register("minecraft:item", "id",
 *     OperationFactory.forClass(Item.class, item -> Registries.ITEM.getId(item).toString()));
 *
 * // The operation can then be used in templates:
 * // "${material.id}" will call the "id" operation on the "material" variable
 * }</pre>
 */
public interface OperationRegistry {

	/**
	 * Registers an operation for a specific group and operation name.
	 *
	 * @param group     The group ID (typically the variable type)
	 * @param operation The operation name (e.g., "id", "path", "namespace")
	 * @param handler   The operation handler
	 * @return A reference to the registered operation
	 */
	OperationReference register(String group, String operation, VariableOperation handler);

	/**
	 * Convenience method to register using a class-based operation.
	 *
	 * @param group     The group ID
	 * @param operation The operation name
	 * @param type      The expected value type
	 * @param handler   The transformation function
	 * @param <T>       The value type
	 * @return A reference to the registered operation
	 */
	default <T> OperationReference register(String group, String operation,
	                                        Class<T> type, Function<T, String> handler) {
		return register(group, operation, OperationFactory.forClass(type, handler));
	}

	/**
	 * Registers a global fallback operation that applies to all groups.
	 *
	 * @param operation The operation name
	 * @param handler   The operation handler
	 * @return A reference to the registered operation
	 */
	OperationReference registerGlobal(String operation, VariableOperation handler);

	/**
	 * Gets an operation by group and name.
	 *
	 * @param group     The group ID
	 * @param operation The operation name
	 * @return The operation, if registered
	 */
	Optional<VariableOperation> get(String group, String operation);

	/**
	 * Applies an operation to convert a value to a string.
	 *
	 * @param group     The group ID
	 * @param operation The operation name
	 * @param value     The value to convert
	 * @return The converted string
	 * @throws IllegalArgumentException if no matching operation is found
	 */
	String apply(String group, String operation, Object value);

	/**
	 * Applies an operation, searching all groups for a matching operation.
	 * Falls back to toString() if no operation is found.
	 *
	 * @param operation The operation name
	 * @param value     The value to convert
	 * @return The converted string
	 */
	String apply(String operation, Object value);

	/**
	 * Reference to a registered operation.
	 */
	interface OperationReference {
		String group();

		String operation();

		VariableOperation handler();
	}
}
