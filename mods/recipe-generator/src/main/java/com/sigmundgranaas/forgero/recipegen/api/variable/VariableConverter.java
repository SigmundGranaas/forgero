package com.sigmundgranaas.forgero.recipegen.api.variable;

import com.google.gson.JsonElement;

import java.util.Collection;

/**
 * Converts a JSON variable definition into a collection of values.
 *
 * <p>Variable converters are matched against JSON elements based on their
 * {@link #matches(JsonElement)} method. The highest-priority matching
 * converter is used.</p>
 *
 * <h2>Example Implementation</h2>
 * <pre>{@code
 * public class StringListConverter implements VariableConverter<String> {
 *     @Override
 *     public boolean matches(JsonElement element) {
 *         return element.isJsonArray() &&
 *                element.getAsJsonArray().asList().stream()
 *                    .allMatch(e -> e.isJsonPrimitive() && e.getAsJsonPrimitive().isString());
 *     }
 *
 *     @Override
 *     public Collection<String> convert(JsonElement element) {
 *         return element.getAsJsonArray().asList().stream()
 *             .map(JsonElement::getAsString)
 *             .collect(Collectors.toList());
 *     }
 * }
 * }</pre>
 *
 * @param <T> The type of values produced by this converter
 */
public interface VariableConverter<T> {

	/**
	 * Checks if this converter can handle the given JSON element.
	 *
	 * @param element The JSON element to check
	 * @return true if this converter can handle the element
	 */
	boolean matches(JsonElement element);

	/**
	 * Converts the JSON element to a collection of values.
	 *
	 * @param element The JSON element to convert
	 * @return Collection of converted values
	 */
	Collection<T> convert(JsonElement element);

	/**
	 * Returns the priority of this converter.
	 * Higher priority converters are tried first.
	 *
	 * @return The priority (default: 0)
	 */
	default int priority() {
		return 0;
	}
}
