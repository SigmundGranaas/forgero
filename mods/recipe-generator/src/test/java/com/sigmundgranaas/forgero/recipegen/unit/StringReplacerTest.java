package com.sigmundgranaas.forgero.recipegen.unit;

import com.sigmundgranaas.forgero.recipegen.api.operation.OperationFactory;
import com.sigmundgranaas.forgero.recipegen.impl.operation.OperationRegistryImpl;
import com.sigmundgranaas.forgero.recipegen.impl.template.StringReplacer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for StringReplacer functionality.
 */
class StringReplacerTest {

	private OperationRegistryImpl operationRegistry;
	private StringReplacer replacer;

	@BeforeEach
	void setUp() {
		operationRegistry = new OperationRegistryImpl();
		replacer = new StringReplacer(operationRegistry);

		// Register some test operations
		operationRegistry.register("test", "upper",
				OperationFactory.forClass(String.class, String::toUpperCase));
		operationRegistry.register("test", "lower",
				OperationFactory.forClass(String.class, String::toLowerCase));
		operationRegistry.register("test", "length",
				OperationFactory.forClass(String.class, s -> String.valueOf(s.length())));

		// Global operation
		operationRegistry.registerGlobal("toString", Object::toString);
	}

	// ============================================================
	// Basic Replacement Tests
	// ============================================================

	@Test
	void testSimpleVariableReplacement() {
		Map<String, Object> variables = new HashMap<>();
		variables.put("name", "iron");

		String result = replacer.applyReplacements("material: ${name}", variables);

		assertEquals("material: iron", result);
	}

	@Test
	void testMultipleVariableReplacements() {
		Map<String, Object> variables = new HashMap<>();
		variables.put("material", "diamond");
		variables.put("variant", "refined");

		String result = replacer.applyReplacements(
				"${material}-${variant}-sword",
				variables
		);

		assertEquals("diamond-refined-sword", result);
	}

	@Test
	void testNoPlaceholders() {
		Map<String, Object> variables = new HashMap<>();
		variables.put("unused", "value");

		String result = replacer.applyReplacements("no placeholders here", variables);

		assertEquals("no placeholders here", result);
	}

	@Test
	void testEmptyTemplate() {
		Map<String, Object> variables = new HashMap<>();

		String result = replacer.applyReplacements("", variables);

		assertEquals("", result);
	}

	@Test
	void testMissingVariablePreservesPlaceholder() {
		Map<String, Object> variables = new HashMap<>();
		variables.put("found", "value");

		String result = replacer.applyReplacements(
				"${found} and ${missing}",
				variables
		);

		assertEquals("value and ${missing}", result);
	}

	// ============================================================
	// Operation Tests
	// ============================================================

	@Test
	void testVariableWithOperation() {
		Map<String, Object> variables = new HashMap<>();
		variables.put("name", "diamond");

		String result = replacer.applyReplacements("${name.upper}", variables);

		assertEquals("DIAMOND", result);
	}

	@Test
	void testMultipleOperations() {
		Map<String, Object> variables = new HashMap<>();
		variables.put("material", "Iron");
		variables.put("type", "SWORD");

		String result = replacer.applyReplacements(
				"${material.lower}-${type.lower}",
				variables
		);

		assertEquals("iron-sword", result);
	}

	@Test
	void testLengthOperation() {
		Map<String, Object> variables = new HashMap<>();
		variables.put("word", "diamond");

		String result = replacer.applyReplacements("Length: ${word.length}", variables);

		assertEquals("Length: 7", result);
	}

	@Test
	void testMissingOperationFallsBackToString() {
		Map<String, Object> variables = new HashMap<>();
		variables.put("value", 42);

		// unknownOp doesn't exist, should fall back to toString()
		String result = replacer.applyReplacements("${value.unknownOp}", variables);

		assertEquals("42", result);
	}

	// ============================================================
	// Edge Cases
	// ============================================================

	@Test
	void testConsecutivePlaceholders() {
		Map<String, Object> variables = new HashMap<>();
		variables.put("a", "A");
		variables.put("b", "B");
		variables.put("c", "C");

		String result = replacer.applyReplacements("${a}${b}${c}", variables);

		assertEquals("ABC", result);
	}

	@Test
	void testPlaceholderAtStartAndEnd() {
		Map<String, Object> variables = new HashMap<>();
		variables.put("start", "BEGIN");
		variables.put("end", "END");

		String result = replacer.applyReplacements("${start} middle ${end}", variables);

		assertEquals("BEGIN middle END", result);
	}

	@Test
	void testNestedBracesNotSupported() {
		Map<String, Object> variables = new HashMap<>();
		variables.put("outer", "value");

		// Nested braces should not be specially handled - innermost match wins
		String result = replacer.applyReplacements("${outer}", variables);

		assertEquals("value", result);
	}

	@Test
	void testJsonLikeTemplate() {
		Map<String, Object> variables = new HashMap<>();
		variables.put("item", "minecraft:diamond");
		variables.put("count", 64);

		String template = """
				{
				  "result": {
				    "item": "${item}",
				    "count": ${count}
				  }
				}""";

		String result = replacer.applyReplacements(template, variables);

		assertTrue(result.contains("\"item\": \"minecraft:diamond\""));
		assertTrue(result.contains("\"count\": 64"));
	}

	@Test
	void testNumericVariables() {
		Map<String, Object> variables = new HashMap<>();
		variables.put("count", 64);
		variables.put("durability", 1561.5);

		String result = replacer.applyReplacements(
				"Count: ${count}, Durability: ${durability}",
				variables
		);

		assertEquals("Count: 64, Durability: 1561.5", result);
	}

	@Test
	void testSpecialCharactersInValues() {
		Map<String, Object> variables = new HashMap<>();
		variables.put("id", "minecraft:diamond_sword");
		variables.put("namespace", "my_mod");

		String result = replacer.applyReplacements(
				"${namespace}:${id}",
				variables
		);

		assertEquals("my_mod:minecraft:diamond_sword", result);
	}

	// ============================================================
	// Custom Object Tests
	// ============================================================

	@Test
	void testCustomObjectWithOperation() {
		// Register operation for custom type
		operationRegistry.register("custom", "name",
				OperationFactory.forClass(TestMaterial.class, TestMaterial::name));
		operationRegistry.register("custom", "id",
				OperationFactory.forClass(TestMaterial.class, m -> m.namespace + ":" + m.name));

		Map<String, Object> variables = new HashMap<>();
		variables.put("material", new TestMaterial("forgero", "iron"));

		String nameResult = replacer.applyReplacements("${material.name}", variables);
		String idResult = replacer.applyReplacements("${material.id}", variables);

		assertEquals("iron", nameResult);
		assertEquals("forgero:iron", idResult);
	}

	@Test
	void testCustomObjectFallsBackToToString() {
		Map<String, Object> variables = new HashMap<>();
		variables.put("material", new TestMaterial("forgero", "diamond"));

		// No operation specified - should use toString()
		String result = replacer.applyReplacements("Material: ${material}", variables);

		assertEquals("Material: TestMaterial[namespace=forgero, name=diamond]", result);
	}

	// ============================================================
	// Helper Classes
	// ============================================================

	private record TestMaterial(String namespace, String name) {
	}
}
