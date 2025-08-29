package com.sigmundgranaas.forgero.data.loading.impl;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.condition.api.DynamicCondition;
import com.sigmundgranaas.forgero.core.condition.api.StaticCondition;
import com.sigmundgranaas.forgero.data.loading.api.data.StaticData;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.UpgradeSlotData;
import com.sigmundgranaas.forgero.data.loading.impl.codec.AttributeCodecs;
import com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants;
import com.sigmundgranaas.forgero.core.condition.api.ConditionCodec;
import com.sigmundgranaas.forgero.data.loading.impl.codec.PartTemplateCodecs;
import com.sigmundgranaas.forgero.data.loading.impl.codec.StaticPartCodecs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class StaticPartDataCodecTest {

	private Codec<StaticData> staticPartDataCodec;

	@BeforeEach
	void setUp() {
		Map<String, Codec<? extends StaticCondition>> staticCodecs = new HashMap<>();
		Map<String, Codec<? extends DynamicCondition>> dynamicCodecs = new HashMap<>();
		ConditionCodec conditionCodec = new ConditionCodec(staticCodecs, dynamicCodecs);

		Codec<List<AttributeData>> attributeListCodec = Codec.list(AttributeCodecs.create(conditionCodec));
		Codec<List<UpgradeSlotData>> upgradeSlotDataListCodec = Codec.list(PartTemplateCodecs.UPGRADE_SLOT_DATA_CODEC);

		this.staticPartDataCodec = StaticPartCodecs.create(attributeListCodec, upgradeSlotDataListCodec);
	}

	private <T> T parseSuccess(Codec<T> codec, String json) {
		DataResult<T> result = codec.parse(JsonOps.INSTANCE, JsonParser.parseString(json));
		assertTrue(result.result().isPresent(), "Parsing should succeed. Error: " + result.error().map(DataResult.PartialResult::message).orElse("No error message"));
		return result.result().get();
	}

	private <T> void parseFailure(Codec<T> codec, String json, String expectedError) {
		DataResult<T> result = codec.parse(JsonOps.INSTANCE, JsonParser.parseString(json));
		assertTrue(result.result().isEmpty(), "Parsing should fail for: " + json);
		assertTrue(result.error().isPresent(), "An error message should be present.");
		assertTrue(result.error().get().message().contains(expectedError), "Error message mismatch. Expected to contain '" + expectedError + "', but was: " + result.error().get().message());
	}

	private OpenIdentifier id(String id) {
		return CodecConstants.IDENTIFIER_FACTORY.of(id);
	}

	@Test
	void testParseFullStaticPart() {
		String json = """
				{
				  "type": "forgero:static_part",
				  "name": "custom_stone",
				  "include": ["forgero:materials/stone_base"],
				  "tags": ["forgero:stone", "forgero:special"],
				  "attributes": [
				    { "id": "forgero:custom-hardness", "type": "forgero:hardness", "computation": 5.0 }
				  ],
				  "features": [],
				  "properties": {
				    "forgero:weight": 2.5
				  }
				}
				""";

		StaticData data = parseSuccess(staticPartDataCodec, json);
		assertEquals(id("forgero:static_part"), data.type());
		assertEquals("custom_stone", data.name());

		assertNotNull(data.include());
		assertTrue(data.include().contains(id("forgero:materials/stone_base")));

		assertNotNull(data.tags());
		assertTrue(data.tags().containsAll(List.of(id("forgero:stone"), id("forgero:special"))));

		assertNotNull(data.attributes());
		assertEquals(1, data.attributes().size());
		AttributeData hardness = data.attributes().get(0);
		assertEquals(id("forgero:custom-hardness"), hardness.id());
		assertEquals(id("forgero:hardness"), hardness.type());
		assertEquals(5.0f, hardness.computation().value());

		assertNotNull(data.properties());
		assertEquals(1, data.properties().size());
		assertTrue(data.properties().containsKey("forgero:weight"));
		assertEquals(2.5, data.properties().get("forgero:weight").getAsDouble(), 0.001);
	}

	@Test
	void testParseMinimalStaticPart() {
		String json = """
				{
				  "type": "forgero:static_part",
				  "name": "simple_stick"
				}
				""";

		StaticData data = parseSuccess(staticPartDataCodec, json);
		assertEquals(id("forgero:static_part"), data.type());
		assertEquals("simple_stick", data.name());
		assertNull(data.include());
		assertNull(data.tags());
		assertNull(data.attributes());
		assertNull(data.properties());
	}

	@Test
	void testParseStaticPartMissingRequiredFields() {
		String json = "{ \"type\": \"forgero:static_part\" }";
		parseFailure(staticPartDataCodec, json, "No key name");
	}
}
