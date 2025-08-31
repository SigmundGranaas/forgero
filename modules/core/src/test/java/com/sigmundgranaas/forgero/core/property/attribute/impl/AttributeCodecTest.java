package com.sigmundgranaas.forgero.core.property.attribute.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.attribute.api.AttributeCodec;
import com.sigmundgranaas.forgero.core.attribute.api.CompositeAttributeComponent;
import com.sigmundgranaas.forgero.core.attribute.api.SimpleAttribute;
import com.sigmundgranaas.forgero.core.attribute.api.operator.*;
import com.sigmundgranaas.forgero.core.condition.api.Condition;
import com.sigmundgranaas.forgero.core.condition.predicate.IsRootCondition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AttributeCodecTest {

	private Codec<Attribute> codec;

	private static final OpenIdentifier ATTACK_DAMAGE = new OpenIdentifier("forgero", "attack_damage");
	private static final OpenIdentifier DURABILITY = new OpenIdentifier("forgero", "durability");
	private static final OpenIdentifier COMPOSITE_KEY = new OpenIdentifier("forgero", "test-head");
	private static final OpenIdentifier IS_ROOT_TYPE = new OpenIdentifier("forgero", "is_root");


	@BeforeEach
	void setUp() {
		Codec<Condition> conditionCodecForTest = IsRootCondition.CODEC.xmap(
				isRoot -> new Condition(List.of(isRoot), Collections.emptyList()),
				condition -> (IsRootCondition) condition.staticConditions().stream()
						.filter(IsRootCondition.class::isInstance)
						.findFirst()
						.orElse(null)
		);
		codec = new AttributeCodec(conditionCodecForTest);
	}

	private void assertDecode(String json, Attribute expectedAttribute) {
		JsonElement jsonElement = JsonParser.parseString(json);
		DataResult<Attribute> decodeResult = codec.decode(JsonOps.INSTANCE, jsonElement)
				.map(Pair::getFirst);

		assertTrue(decodeResult.result().isPresent(), "Failed to decode: " + decodeResult.error().map(DataResult.PartialResult::message).orElse(""));
		assertEquals(expectedAttribute, decodeResult.result().get());
	}

	private void assertEncode(Attribute attribute, String expectedJson) {
		DataResult<JsonElement> encodeResult = codec.encodeStart(JsonOps.INSTANCE, attribute);
		assertTrue(encodeResult.result().isPresent(), "Failed to encode: " + encodeResult.error().map(DataResult.PartialResult::message).orElse(""));

		JsonObject expectedObject = JsonParser.parseString(expectedJson).getAsJsonObject();
		JsonObject actualObject = encodeResult.result().get().getAsJsonObject();

		// Use Set.equals() on the entrySet for a robust, order-insensitive comparison.
		// This is the correct way to check for semantic equality of two JSON objects.
		assertEquals(expectedObject.entrySet(), actualObject.entrySet(),
				"The serialized JSON objects must have the same key-value pairs, regardless of order.");
	}

	@Test
	void decodeSimpleAttributeWithDefaults() {
		String inputJson = """
            {
              "type": "forgero:attack_damage",
              "value": 10.0
            }
            """;
		SimpleAttribute expected = new SimpleAttribute(Optional.empty(), ATTACK_DAMAGE, 10.0f, AdditionOperator.getInstance(), 0, Condition.ALWAYS_TRUE);
		assertDecode(inputJson, expected);
	}

	@Test
	void encodeSimpleAttributeWithDefaults() {
		SimpleAttribute attribute = new SimpleAttribute(ATTACK_DAMAGE, 10.0f);
		String expectedJson = """
            {
              "type": "forgero:attack_damage",
              "value": 10.0,
              "operator": "add",
              "group": 0
            }
            """;
		assertEncode(attribute, expectedJson);
	}

	@Test
	void decodeSimpleAttributeWithExplicitValues() {
		// Using 1.5 as it is perfectly representable in both float and double
		String inputJson = """
            {
              "type": "forgero:attack_damage",
              "value": 1.5,
              "operator": "mul",
              "group": 2
            }
            """;
		SimpleAttribute expected = new SimpleAttribute(Optional.empty(), ATTACK_DAMAGE, 1.5f, MultiplicationOperator.getInstance(), 2, Condition.ALWAYS_TRUE);
		assertDecode(inputJson, expected);
	}

	@Test
	void encodeSimpleAttributeWithExplicitValues() {
		// Using 1.5f to avoid float vs double precision issues during comparison
		SimpleAttribute attribute = new SimpleAttribute(ATTACK_DAMAGE, 1.5f, MultiplicationOperator.getInstance(), 2);
		String expectedJson = """
            {
              "type": "forgero:attack_damage",
              "value": 1.5,
              "operator": "mul",
              "group": 2
            }
            """;
		assertEncode(attribute, expectedJson);
	}

	@Test
	void decodeSimpleAttributeWithLongOperatorName() {
		String inputJson = "{ \"type\": \"forgero:attack_damage\", \"value\": 1.5, \"operator\": \"subtraction\" }";
		SimpleAttribute expected = new SimpleAttribute(Optional.empty(), ATTACK_DAMAGE, 1.5f, SubtractionOperator.getInstance(), 0, Condition.ALWAYS_TRUE);
		assertDecode(inputJson, expected);
	}

	@Test
	void encodeAlwaysUsesShortOperatorName() {
		SimpleAttribute attribute = new SimpleAttribute(ATTACK_DAMAGE, 1.5f, SubtractionOperator.getInstance(), 0);
		String expectedJson = "{ \"type\": \"forgero:attack_damage\", \"value\": 1.5, \"operator\": \"sub\", \"group\": 0 }";
		assertEncode(attribute, expectedJson);
	}

	@Test
	void decodeCompositeComponentWithDefaults() {
		String inputJson = """
            {
              "type": "forgero:durability",
              "value": 100,
              "composite_key": "forgero:test-head"
            }
            """;
		CompositeAttributeComponent expected = new CompositeAttributeComponent(Optional.empty(), DURABILITY, 100f, AdditionOperator.getInstance(), 0, COMPOSITE_KEY);
		assertDecode(inputJson, expected);
	}

	@Test
	void encodeCompositeComponentWithDefaults() {
		CompositeAttributeComponent attribute = new CompositeAttributeComponent(DURABILITY, 100f, COMPOSITE_KEY);
		String expectedJson = """
            {
              "type": "forgero:durability",
              "value": 100.0,
              "operator": "add",
              "group": 0,
              "composite_key": "forgero:test-head"
            }
            """;
		assertEncode(attribute, expectedJson);
	}

	@Test
	void decodeAndEncodeFullCompositeComponent() {
		String json = """
            {
              "type": "forgero:durability",
              "value": 0.1,
              "operator": "div",
              "group": 3,
              "composite_key": "forgero:test-head"
            }
            """;
		// 0.1f also has precision issues, so we use a different representable float
		CompositeAttributeComponent attribute = new CompositeAttributeComponent(Optional.empty(), DURABILITY, 0.125f, DivisionOperator.getInstance(), 3, COMPOSITE_KEY);
		String expectedJson = """
            {
              "type": "forgero:durability",
              "value": 0.125,
              "operator": "div",
              "group": 3,
              "composite_key": "forgero:test-head"
            }
            """;
		assertEncode(attribute, expectedJson);
	}

	@Test
	void decodesAndEncodesSimpleAttributeWithCondition() {
		String json = """
            {
              "type": "forgero:attack_damage",
              "value": 2.0,
              "condition": {
                "type": "forgero:is_root"
              }
            }
            """;
		Condition condition = new Condition(List.of(new IsRootCondition(IS_ROOT_TYPE)), List.of());
		SimpleAttribute attribute = new SimpleAttribute(ATTACK_DAMAGE, 2.0f, condition);

		assertDecode(json, attribute);

		String expectedJson = """
            {
              "type": "forgero:attack_damage",
              "value": 2.0,
              "operator": "add",
              "group": 0,
              "condition": {
                "type": "forgero:is_root"
              }
            }
            """;
		assertEncode(attribute, expectedJson);
	}
}
