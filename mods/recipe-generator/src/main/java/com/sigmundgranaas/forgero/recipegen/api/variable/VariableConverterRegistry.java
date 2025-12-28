package com.sigmundgranaas.forgero.recipegen.api.variable;

import com.google.gson.JsonElement;

import java.util.Collection;
import java.util.Optional;

/**
 * Registry for variable converters.
 *
 * <p>Thread-safe registry that allows registration of custom variable converters.
 * When converting a variable, the registry selects the highest-priority matching
 * converter.</p>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * VariableConverterRegistry registry = RecipeGenApi.getInstance().variables();
 *
 * // Register a custom converter
 * registry.register("mymod:custom_type", new MyCustomConverter());
 *
 * // Convert a JSON element
 * Collection<?> values = registry.convert(jsonElement);
 * }</pre>
 */
public interface VariableConverterRegistry {

	/**
	 * Registers a variable converter with the given ID.
	 *
	 * @param id        Unique identifier (e.g., "mymod:custom_type")
	 * @param converter The converter to register
	 * @param <T>       The type of values the converter produces
	 * @return A reference to the registered converter
	 */
	<T> ConverterReference<T> register(String id, VariableConverter<T> converter);

	/**
	 * Gets a converter by its ID.
	 *
	 * @param id The converter ID
	 * @return The converter, if registered
	 */
	Optional<VariableConverter<?>> get(String id);

	/**
	 * Converts a JSON element using the appropriate registered converter.
	 * Selects the highest-priority matching converter.
	 *
	 * @param element The JSON element to convert
	 * @return Collection of converted values
	 * @throws IllegalArgumentException if no matching converter is found
	 */
	Collection<?> convert(JsonElement element);

	/**
	 * Converts a JSON element using the converter with the specified ID.
	 *
	 * @param id      The converter ID to use
	 * @param element The JSON element to convert
	 * @return Collection of converted values
	 * @throws IllegalArgumentException if the converter is not found
	 */
	Collection<?> convert(String id, JsonElement element);

	/**
	 * Reference to a registered converter.
	 *
	 * @param <T> The type of values the converter produces
	 */
	interface ConverterReference<T> {
		String id();

		VariableConverter<T> converter();
	}
}
