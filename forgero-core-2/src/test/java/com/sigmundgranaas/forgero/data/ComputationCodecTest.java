package com.sigmundgranaas.forgero.data;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.data.v3.codec.AttributeCodecs;
import com.sigmundgranaas.forgero.data.v3.dto.attribute.ComputationData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ComputationCodecTest {

	@Test
	void testParseNumberShortcut() {
		String json = "100";
		var result = AttributeCodecs.COMPUTATION_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json));
		assertTrue(result.result().isPresent(), "Parsing should succeed");

		ComputationData data = result.result().get();
		assertEquals(100f, data.value());
		assertEquals(AttributeCodecs.ADDITION_OPERATOR, data.operator());
		assertEquals(AttributeCodecs.BASE_ORDER, data.order());
	}

	@Test
	void testParseAddShortcut() {
		String json = """
				{ "add": 1.2 }
				""";
		var result = AttributeCodecs.COMPUTATION_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json));
		assertTrue(result.result().isPresent(), "Parsing should succeed");

		ComputationData data = result.result().get();
		assertEquals(1.2f, data.value());
		assertEquals(AttributeCodecs.ADDITION_OPERATOR, data.operator());
		assertEquals(AttributeCodecs.BASE_ORDER, data.order());
	}

	@Test
	void testParseMultiplyShortcut() {
		String json = """
				{ "multiply": 1.5 }
				""";
		var result = AttributeCodecs.COMPUTATION_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json));
		assertTrue(result.result().isPresent(), "Parsing should succeed");

		ComputationData data = result.result().get();
		assertEquals(1.5f, data.value());
		assertEquals(AttributeCodecs.MULTIPLICATION_OPERATOR, data.operator());
		assertEquals(AttributeCodecs.BASE_ORDER, data.order());
	}

	@Test
	void testParseFullObject() {
		String json = """
				{
				  "value": 50,
				  "operator": "forgero:multiplication",
				  "order": "forgero:final"
				}
				""";
		var result = AttributeCodecs.COMPUTATION_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json));
		assertTrue(result.result().isPresent(), "Parsing should succeed");

		ComputationData data = result.result().get();
		assertEquals(50f, data.value());
		assertEquals("forgero:multiplication", data.operator());
		assertEquals("forgero:final", data.order());
	}

	@Test
	void testParseObjectWithDefaults() {
		String json = """
				{ "value": 75 }
				""";
		var result = AttributeCodecs.COMPUTATION_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json));
		assertTrue(result.result().isPresent(), "Parsing should succeed");

		ComputationData data = result.result().get();
		assertEquals(75f, data.value());
		assertEquals(AttributeCodecs.ADDITION_OPERATOR, data.operator(), "Operator should default to addition");
		assertEquals(AttributeCodecs.BASE_ORDER, data.order(), "Order should default to base");
	}

	@Test
	void testParseObjectWithPartialDefaults() {
		String json = """
				{
				  "value": 50,
				  "order": "forgero:base"
				}
				""";
		var result = AttributeCodecs.COMPUTATION_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json));
		assertTrue(result.result().isPresent(), "Parsing should succeed");

		ComputationData data = result.result().get();
		assertEquals(50f, data.value());
		assertEquals(AttributeCodecs.ADDITION_OPERATOR, data.operator(), "Operator should default to addition");
		assertEquals(AttributeCodecs.BASE_ORDER, data.order());
	}
}
