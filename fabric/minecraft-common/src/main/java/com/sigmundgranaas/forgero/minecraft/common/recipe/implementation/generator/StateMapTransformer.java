package com.sigmundgranaas.forgero.minecraft.common.recipe.implementation.generator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.core.type.TypeTree;

public class StateMapTransformer {
	private final TypeTree typeTree;

	public StateMapTransformer(TypeTree typeTree) {
		this.typeTree = typeTree;
	}

	public List<Map<String, State>> transformStateMap(JsonObject stateMapJson) {
		Map<String, List<State>> stateOptions = new HashMap<>();

		for (String key : stateMapJson.keySet()) {
			JsonObject entry = stateMapJson.getAsJsonObject(key);
			String type = entry.get("type").getAsString();
			List<State> states = typeTree.find(Type.of(type))
					.map(node -> node.getResources(State.class))
					.orElseGet(() -> ImmutableList.<State>builder().build());
			stateOptions.put(key, states);
		}

		return generateCombinations(stateOptions);
	}

	private List<Map<String, State>> generateCombinations(Map<String, List<State>> stateOptions) {
		List<Map<String, State>> combinations = new ArrayList<>();
		generateCombinationsRecursive(combinations, new HashMap<>(), new ArrayList<>(stateOptions.keySet()), 0, stateOptions);
		return combinations;
	}

	private void generateCombinationsRecursive(List<Map<String, State>> combinations, Map<String, State> current, List<String> keys, int depth, Map<String, List<State>> stateOptions) {
		if (depth == keys.size()) {
			combinations.add(new HashMap<>(current));
			return;
		}

		String key = keys.get(depth);
		for (State state : stateOptions.get(key)) {
			current.put(key, state);
			generateCombinationsRecursive(combinations, current, keys, depth + 1, stateOptions);
		}
	}
}
