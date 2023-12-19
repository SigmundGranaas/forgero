package com.sigmundgranaas.forgero.minecraft.common.recipe.implementation.generator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.minecraft.common.recipe.generator.VariableType;

public class StateMapTransformer {
	private final Function<String, List<State>> stateFinder;

	public StateMapTransformer(Function<String, List<State>> stateFinder) {
		this.stateFinder = stateFinder;
	}

	public List<Map<String, Object>> transformStateMap(JsonObject stateMapJson) {
		Map<String, VariableType<?>> variables = new HashMap<>();

		for (String key : stateMapJson.keySet()) {
			JsonElement entry = stateMapJson.get(key);
			VariableType<?> variableType = VariableTypeRegistry.createVariableType(entry, stateFinder);
			variables.put(key, variableType);
		}

		return generateCombinations(variables);
	}

	private List<Map<String, Object>> generateCombinations(Map<String, VariableType<?>> stateOptions) {
		List<Map<String, Object>> combinations = new ArrayList<>();
		generateCombinationsRecursive(combinations, new HashMap<>(), new ArrayList<>(stateOptions.keySet()), 0, stateOptions);
		return combinations;
	}

	private void generateCombinationsRecursive(List<Map<String, Object>> combinations, Map<String, Object> current, List<String> keys, int depth, Map<String, VariableType<?>> stateOptions) {
		if (depth == keys.size()) {
			combinations.add(new HashMap<>(current));
			return;
		}

		String key = keys.get(depth);
		for (Object state : stateOptions.get(key).values()) {
			current.put(key, state);
			generateCombinationsRecursive(combinations, current, keys, depth + 1, stateOptions);
		}
	}
}
