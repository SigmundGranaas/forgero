package com.sigmundgranaas.forgero.generator.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class VariableToMapTransformer {
	private final Function<JsonElement, Collection<?>> converter;

	public VariableToMapTransformer(Function<JsonElement, Collection<?>> converter) {
		this.converter = converter;
	}

	public Collection<Map<String, Object>> transformStateMap(JsonObject stateMapJson) {
		Map<String, Collection<?>> variables = new HashMap<>();

		for (String key : stateMapJson.keySet()) {
			Collection<?> entry = converter.apply(stateMapJson.get(key));
			variables.put(key, entry);
		}
		return generateCombinations(variables);
	}

	private List<Map<String, Object>> generateCombinations(Map<String, Collection<?>> stateOptions) {
		List<Map<String, Object>> combinations = new ArrayList<>();
		generateCombinationsRecursive(combinations, new HashMap<>(), new ArrayList<>(stateOptions.keySet()), 0, stateOptions);
		return combinations;
	}

	private void generateCombinationsRecursive(List<Map<String, Object>> combinations, Map<String, Object> current, List<String> keys, int depth, Map<String, Collection<?>> stateOptions) {
		if (depth == keys.size()) {
			combinations.add(new HashMap<>(current));
			return;
		}

		String key = keys.get(depth);
		for (Object state : stateOptions.get(key)) {
			current.put(key, state);
			generateCombinationsRecursive(combinations, current, keys, depth + 1, stateOptions);
		}
	}
}
