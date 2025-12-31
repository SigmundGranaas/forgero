package com.sigmundgranaas.forgero.data.loading.impl;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.tags.engine.TagGraph;
import com.sigmundgranaas.forgero.core.condition.api.DynamicCondition;
import com.sigmundgranaas.forgero.core.condition.api.StaticCondition;
import com.sigmundgranaas.forgero.core.condition.predicate.InSlotTypeCondition;
import com.sigmundgranaas.forgero.core.condition.predicate.TagMatchCondition;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.loading.impl.codec.AttributeCodecs;
import com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants;
import com.sigmundgranaas.forgero.core.condition.api.ConditionCodec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.sigmundgranaas.forgero.testutils.TestIdentifiers.id;
import static org.junit.jupiter.api.Assertions.*;

class AttributeDataCodecTest {

	private Codec<AttributeData> attributeDataCodec;

	@BeforeEach
	void setUp() {
		Map<String, Codec<? extends StaticCondition>> staticCodecs = new HashMap<>();
		Map<OpenIdentifier, Set<OpenIdentifier>> tagMap = new HashMap<>();
		tagMap.put(id("pickaxe"), new HashSet<>());
		staticCodecs.put("forgero:self_has_tag", TagMatchCondition.codec(() -> new TagGraph(tagMap)));
		staticCodecs.put("forgero:in_slot_type", InSlotTypeCondition.CODEC);
		Map<String, Codec<? extends DynamicCondition>> dynamicCodecs = new HashMap<>();
		ConditionCodec conditionCodec = new ConditionCodec(staticCodecs, dynamicCodecs);
		this.attributeDataCodec = AttributeCodecs.create(conditionCodec);
	}

	private <T> T parseSuccess(Codec<T> codec, String json) {
		DataResult<T> result = codec.parse(JsonOps.INSTANCE, JsonParser.parseString(json));
		assertTrue(result.result().isPresent(), "Parsing should succeed. Error: " + result.error().map(DataResult.PartialResult::message).orElse("No error message"));
		return result.result().get();
	}

	private OpenIdentifier id(String id) {
		return CodecConstants.IDENTIFIER_FACTORY.of(id);
	}

	@Test
	void testParseFullAttribute() {
		String json = """
				{
				  "id": "forgero:diamond-scoped-mining-speed",
				  "type": "forgero:mining_speed",
				  "condition": { "type": "forgero:self_has_tag", "tag": "forgero:gem" },
				  "computation": { "add": 8 }
				}
				""";

		AttributeData data = parseSuccess(attributeDataCodec, json);

		assertEquals(Optional.of(id("forgero:diamond-scoped-mining-speed")), data.id());
		assertEquals(id("forgero:mining_speed"), data.type());
		assertEquals(8f, data.computation().value());
		assertEquals(AttributeCodecs.ADDITION_OPERATOR, data.computation().operator());
		assertEquals(AttributeCodecs.BASE_ORDER, data.computation().order());

		assertTrue(data.condition().isPresent());
		var condition = data.condition().get();
		assertEquals(1, condition.staticConditions().size());
		assertTrue(condition.dynamicConditions().isEmpty());
		var predicate = condition.staticConditions().get(0);
		assertInstanceOf(TagMatchCondition.class, predicate);
		assertEquals(id("forgero:self_has_tag"), predicate.type());
		assertEquals(id("forgero:gem"), ((TagMatchCondition) predicate).tag());
	}

	@Test
	void testParseMinimalAttribute() {
		String json = """
				{
				  "id": "forgero:diamond-durability",
				  "type": "forgero:durability",
				  "computation": 1561
				}
				""";

		AttributeData data = parseSuccess(attributeDataCodec, json);

		assertEquals(Optional.of(id("forgero:diamond-durability")), data.id());
		assertEquals(id("forgero:durability"), data.type());

		assertTrue(data.condition().isEmpty());
		assertEquals(1561f, data.computation().value());
		assertEquals(AttributeCodecs.ADDITION_OPERATOR, data.computation().operator());
		assertEquals(AttributeCodecs.BASE_ORDER, data.computation().order());
	}

	/**
	 * Tests the wrapper format: {"static": [...], "dynamic": [...]}
	 * This is the format used in actual material JSON files like iron.json.
	 *
	 * <p>This test verifies the fix for the bug where conditions using the
	 * wrapper format were silently dropped, causing attributes to be unconditional.</p>
	 */
	@Test
	void testParseAttributeWithWrapperFormatCondition() {
		String json = """
				{
				  "id": "forgero:iron-armor",
				  "type": "forgero:armor",
				  "computation": 2.0,
				  "condition": {
				    "static": [
				      { "type": "forgero:in_slot_type", "slot_type": "forgero:armor_material" }
				    ]
				  }
				}
				""";

		AttributeData data = parseSuccess(attributeDataCodec, json);

		assertEquals(Optional.of(id("forgero:iron-armor")), data.id());
		assertEquals(id("forgero:armor"), data.type());
		assertEquals(2.0f, data.computation().value());

		// CRITICAL: The condition must be present - this was the bug
		assertTrue(data.condition().isPresent(),
				"Condition should be present when using wrapper format { \"static\": [...] }");

		var condition = data.condition().get();
		assertEquals(1, condition.staticConditions().size(),
				"Should have exactly one static condition");
		assertTrue(condition.dynamicConditions().isEmpty(),
				"Should have no dynamic conditions");

		var predicate = condition.staticConditions().get(0);
		assertInstanceOf(InSlotTypeCondition.class, predicate,
				"Static condition should be InSlotTypeCondition");

		InSlotTypeCondition slotCondition = (InSlotTypeCondition) predicate;
		assertEquals(id("forgero:armor_material"), slotCondition.slotType(),
				"Slot type should be armor_material");
	}

	/**
	 * Tests parsing multiple static conditions in wrapper format.
	 * This matches the real iron.json pattern with multiple conditions.
	 */
	@Test
	void testParseAttributeWithMultipleStaticConditionsInWrapperFormat() {
		String json = """
				{
				  "id": "forgero:iron-armor-base",
				  "type": "forgero:armor",
				  "computation": 2.0,
				  "condition": {
				    "static": [
				      { "type": "forgero:in_slot_type", "slot_type": "forgero:armor_material" },
				      { "type": "forgero:self_has_tag", "tag": "forgero:pickaxe" }
				    ]
				  }
				}
				""";

		AttributeData data = parseSuccess(attributeDataCodec, json);

		assertTrue(data.condition().isPresent());
		var condition = data.condition().get();
		assertEquals(2, condition.staticConditions().size(),
				"Should parse all static conditions from wrapper format");
	}
}
