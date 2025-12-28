package com.sigmundgranaas.forgero.recipegen.impl;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.recipegen.api.variable.VariableConverterRegistry;

import java.util.*;

/**
 * Generates all combinations (cartesian product) of variable values.
 *
 * <p>Given a JSON object with variable definitions, this class converts each variable
 * to a collection of values and then generates all possible combinations.</p>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * // Input:
 * {
 *   "material": ["iron", "gold", "diamond"],
 *   "variant": ["standard", "refined"]
 * }
 *
 * // Output: 6 combinations
 * [
 *   {material: "iron", variant: "standard"},
 *   {material: "iron", variant: "refined"},
 *   {material: "gold", variant: "standard"},
 *   {material: "gold", variant: "refined"},
 *   {material: "diamond", variant: "standard"},
 *   {material: "diamond", variant: "refined"}
 * ]
 * }</pre>
 */
public class CartesianProductCombinator {

	private final VariableConverterRegistry converterRegistry;

	public CartesianProductCombinator(VariableConverterRegistry converterRegistry) {
		this.converterRegistry = converterRegistry;
	}

	/**
	 * Transforms a JSON object of variable definitions into all possible combinations.
	 *
	 * @param variables JSON object with variable definitions
	 * @return Collection of variable maps, one for each combination
	 */
	public Collection<Map<String, Object>> generateCombinations(JsonObject variables) {
		Map<String, Collection<?>> convertedVariables = new LinkedHashMap<>();

		for (String key : variables.keySet()) {
			Collection<?> values = converterRegistry.convert(variables.get(key));
			convertedVariables.put(key, values);
		}

		return cartesianProduct(convertedVariables);
	}

	private List<Map<String, Object>> cartesianProduct(Map<String, Collection<?>> variables) {
		List<Map<String, Object>> result = new ArrayList<>();
		List<String> keys = new ArrayList<>(variables.keySet());

		if (keys.isEmpty()) {
			result.add(new HashMap<>());
			return result;
		}

		// Check if any variable has no values - if so, cartesian product is empty
		for (Collection<?> values : variables.values()) {
			if (values == null || values.isEmpty()) {
				return result; // Return empty list
			}
		}

		generateCombinationsRecursive(result, new HashMap<>(), keys, 0, variables);

		return result;
	}

	private void generateCombinationsRecursive(
			List<Map<String, Object>> result,
			Map<String, Object> current,
			List<String> keys,
			int depth,
			Map<String, Collection<?>> variables) {

		if (depth == keys.size()) {
			result.add(new HashMap<>(current));
			return;
		}

		String key = keys.get(depth);
		Collection<?> values = variables.get(key);

		for (Object value : values) {
			current.put(key, value);
			generateCombinationsRecursive(result, current, keys, depth + 1, variables);
		}
	}
}
