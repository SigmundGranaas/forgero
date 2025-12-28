package com.sigmundgranaas.forgero.recipegen.unit;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.recipegen.api.variable.VariableConverter;
import com.sigmundgranaas.forgero.recipegen.impl.CartesianProductCombinator;
import com.sigmundgranaas.forgero.recipegen.impl.variable.VariableConverterRegistryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CartesianProductCombinator functionality.
 */
class CartesianProductCombinatorTest {

	private VariableConverterRegistryImpl registry;
	private CartesianProductCombinator combinator;

	@BeforeEach
	void setUp() {
		registry = new VariableConverterRegistryImpl();
		combinator = new CartesianProductCombinator(registry);

		// Register a simple string list converter
		registry.register("test:string_list", new StringListTestConverter());
	}

	// ============================================================
	// Basic Combination Tests
	// ============================================================

	@Test
	void testSingleVariableSingleValue() {
		JsonObject variables = new JsonObject();
		JsonArray values = new JsonArray();
		values.add("iron");
		variables.add("material", values);

		Collection<Map<String, Object>> combinations = combinator.generateCombinations(variables);

		assertEquals(1, combinations.size());
		Map<String, Object> first = combinations.iterator().next();
		assertEquals("iron", first.get("material"));
	}

	@Test
	void testSingleVariableMultipleValues() {
		JsonObject variables = new JsonObject();
		JsonArray values = new JsonArray();
		values.add("iron");
		values.add("gold");
		values.add("diamond");
		variables.add("material", values);

		Collection<Map<String, Object>> combinations = combinator.generateCombinations(variables);

		assertEquals(3, combinations.size());

		Set<String> materials = new HashSet<>();
		for (Map<String, Object> combo : combinations) {
			materials.add((String) combo.get("material"));
		}

		assertTrue(materials.contains("iron"));
		assertTrue(materials.contains("gold"));
		assertTrue(materials.contains("diamond"));
	}

	@Test
	void testTwoVariablesCartesianProduct() {
		JsonObject variables = new JsonObject();

		JsonArray materials = new JsonArray();
		materials.add("iron");
		materials.add("diamond");
		variables.add("material", materials);

		JsonArray variants = new JsonArray();
		variants.add("standard");
		variants.add("refined");
		variables.add("variant", variants);

		Collection<Map<String, Object>> combinations = combinator.generateCombinations(variables);

		// 2 materials x 2 variants = 4 combinations
		assertEquals(4, combinations.size());

		Set<String> combos = new HashSet<>();
		for (Map<String, Object> combo : combinations) {
			combos.add(combo.get("material") + "-" + combo.get("variant"));
		}

		assertTrue(combos.contains("iron-standard"));
		assertTrue(combos.contains("iron-refined"));
		assertTrue(combos.contains("diamond-standard"));
		assertTrue(combos.contains("diamond-refined"));
	}

	@Test
	void testThreeVariablesCartesianProduct() {
		JsonObject variables = new JsonObject();

		JsonArray materials = new JsonArray();
		materials.add("iron");
		materials.add("diamond");
		variables.add("material", materials);

		JsonArray variants = new JsonArray();
		variants.add("standard");
		variants.add("refined");
		variables.add("variant", variants);

		JsonArray types = new JsonArray();
		types.add("sword");
		types.add("pickaxe");
		types.add("axe");
		variables.add("type", types);

		Collection<Map<String, Object>> combinations = combinator.generateCombinations(variables);

		// 2 materials x 2 variants x 3 types = 12 combinations
		assertEquals(12, combinations.size());
	}

	// ============================================================
	// Edge Cases
	// ============================================================

	@Test
	void testEmptyVariables() {
		JsonObject variables = new JsonObject();

		Collection<Map<String, Object>> combinations = combinator.generateCombinations(variables);

		// Empty variables should return single empty map
		assertEquals(1, combinations.size());
		assertTrue(combinations.iterator().next().isEmpty());
	}

	@Test
	void testSingleEmptyArray() {
		JsonObject variables = new JsonObject();
		JsonArray empty = new JsonArray();
		variables.add("material", empty);

		Collection<Map<String, Object>> combinations = combinator.generateCombinations(variables);

		// Empty array means no combinations possible
		assertEquals(0, combinations.size());
	}

	@Test
	void testMixedEmptyAndNonEmptyArrays() {
		JsonObject variables = new JsonObject();

		JsonArray materials = new JsonArray();
		materials.add("iron");
		materials.add("diamond");
		variables.add("material", materials);

		JsonArray empty = new JsonArray();
		variables.add("empty", empty);

		Collection<Map<String, Object>> combinations = combinator.generateCombinations(variables);

		// Empty variable short-circuits - no valid combinations
		assertEquals(0, combinations.size());
	}

	@Test
	void testLargeCartesianProduct() {
		JsonObject variables = new JsonObject();

		// 5 variables x 3 values each = 3^5 = 243 combinations
		for (int v = 0; v < 5; v++) {
			JsonArray values = new JsonArray();
			values.add("a");
			values.add("b");
			values.add("c");
			variables.add("var" + v, values);
		}

		Collection<Map<String, Object>> combinations = combinator.generateCombinations(variables);

		assertEquals(243, combinations.size());

		// Verify all combinations are unique
		Set<String> uniqueCombos = new HashSet<>();
		for (Map<String, Object> combo : combinations) {
			StringBuilder sb = new StringBuilder();
			for (int v = 0; v < 5; v++) {
				sb.append(combo.get("var" + v));
			}
			uniqueCombos.add(sb.toString());
		}
		assertEquals(243, uniqueCombos.size());
	}

	// ============================================================
	// Order Preservation Tests
	// ============================================================

	@Test
	void testVariableOrderPreserved() {
		JsonObject variables = new JsonObject();

		JsonArray first = new JsonArray();
		first.add("1");
		variables.add("first", first);

		JsonArray second = new JsonArray();
		second.add("2");
		variables.add("second", second);

		JsonArray third = new JsonArray();
		third.add("3");
		variables.add("third", third);

		Collection<Map<String, Object>> combinations = combinator.generateCombinations(variables);

		assertEquals(1, combinations.size());
		Map<String, Object> combo = combinations.iterator().next();

		// All variables should be present
		assertEquals("1", combo.get("first"));
		assertEquals("2", combo.get("second"));
		assertEquals("3", combo.get("third"));
	}

	@Test
	void testValueOrderPreserved() {
		JsonObject variables = new JsonObject();

		JsonArray ordered = new JsonArray();
		ordered.add("first");
		ordered.add("second");
		ordered.add("third");
		variables.add("item", ordered);

		Collection<Map<String, Object>> combinations = combinator.generateCombinations(variables);
		List<Map<String, Object>> list = new ArrayList<>(combinations);

		assertEquals(3, list.size());
		assertEquals("first", list.get(0).get("item"));
		assertEquals("second", list.get(1).get("item"));
		assertEquals("third", list.get(2).get("item"));
	}

	// ============================================================
	// Helper Classes
	// ============================================================

	/**
	 * Simple converter that converts JSON arrays of strings to collections.
	 */
	private static class StringListTestConverter implements VariableConverter<String> {

		@Override
		public boolean matches(com.google.gson.JsonElement element) {
			if (element.isJsonArray()) {
				for (com.google.gson.JsonElement e : element.getAsJsonArray()) {
					if (!e.isJsonPrimitive() || !e.getAsJsonPrimitive().isString()) {
						return false;
					}
				}
				return true;
			}
			return false;
		}

		@Override
		public Collection<String> convert(com.google.gson.JsonElement element) {
			List<String> result = new ArrayList<>();
			for (com.google.gson.JsonElement e : element.getAsJsonArray()) {
				result.add(e.getAsString());
			}
			return result;
		}
	}
}
