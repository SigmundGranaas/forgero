package com.sigmundgranaas.forgero.data;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.data.codec.AttributeCodecs;
import com.sigmundgranaas.forgero.data.dto.attribute.ComputationData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ComputationCodecTest {

	private <T> T parseSuccess(Codec<T> codec, String json) {
		DataResult<T> result = codec.parse(JsonOps.INSTANCE, JsonParser.parseString(json));
		assertTrue(result.result().isPresent(), "Parsing should succeed. Error: " + result.error().map(DataResult.PartialResult::message).orElse("No error message"));
		return result.result().get();
	}

	@Test
	void testParseNumberShortcut() {
		ComputationData data = parseSuccess(AttributeCodecs.COMPUTATION_CODEC, "100");
		assertEquals(100f, data.value());
		assertEquals(AttributeCodecs.ADDITION_OPERATOR, data.operator());
		assertEquals(AttributeCodecs.BASE_ORDER, data.order());
	}

	@Test
	void testParseAddShortcut() {
		String json = """
				{ "add": 1.2 }
				""";
		ComputationData data = parseSuccess(AttributeCodecs.COMPUTATION_CODEC, json);
		assertEquals(1.2f, data.value());
		assertEquals(AttributeCodecs.ADDITION_OPERATOR, data.operator());
		assertEquals(AttributeCodecs.BASE_ORDER, data.order());
	}

	@Test
	void testParseMultiplyShortcut() {
		String json = """
				{ "multiply": 1.5 }
				""";
		ComputationData data = parseSuccess(AttributeCodecs.COMPUTATION_CODEC, json);
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
		ComputationData data = parseSuccess(AttributeCodecs.COMPUTATION_CODEC, json);
		assertEquals(50f, data.value());
		assertEquals("forgero:multiplication", data.operator());
		assertEquals("forgero:final", data.order());
	}

	@Test
	void testParseObjectWithDefaults() {
		String json = "{ \"value\": 75 }";
		ComputationData data = parseSuccess(AttributeCodecs.COMPUTATION_CODEC, json);
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
		ComputationData data = parseSuccess(AttributeCodecs.COMPUTATION_CODEC, json);
		assertEquals(50f, data.value());
		assertEquals(AttributeCodecs.ADDITION_OPERATOR, data.operator(), "Operator should default to addition");
		assertEquals(AttributeCodecs.BASE_ORDER, data.order());
	}
}
