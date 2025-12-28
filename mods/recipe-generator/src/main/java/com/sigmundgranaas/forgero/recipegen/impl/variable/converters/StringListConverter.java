package com.sigmundgranaas.forgero.recipegen.impl.variable.converters;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.sigmundgranaas.forgero.recipegen.api.variable.VariableConverter;

import java.util.Collection;
import java.util.List;
import java.util.stream.StreamSupport;

/**
 * Converts a JSON array of strings to a collection of strings.
 *
 * <h2>Example Input</h2>
 * <pre>{@code
 * ["iron", "gold", "diamond"]
 * }</pre>
 *
 * <h2>Example Output</h2>
 * <pre>{@code
 * Collection of: "iron", "gold", "diamond"
 * }</pre>
 */
public class StringListConverter implements VariableConverter<String> {

	private static final Gson GSON = new Gson();

	@Override
	public boolean matches(JsonElement element) {
		if (element.isJsonArray()) {
			return StreamSupport.stream(element.getAsJsonArray().spliterator(), false)
					.filter(JsonElement::isJsonPrimitive)
					.map(JsonElement::getAsJsonPrimitive)
					.allMatch(JsonPrimitive::isString);
		}
		return false;
	}

	@Override
	@SuppressWarnings("UnstableApiUsage")
	public Collection<String> convert(JsonElement element) {
		return GSON.fromJson(element, new TypeToken<List<String>>() {
		}.getType());
	}

	@Override
	public int priority() {
		return 0; // Default priority
	}
}
