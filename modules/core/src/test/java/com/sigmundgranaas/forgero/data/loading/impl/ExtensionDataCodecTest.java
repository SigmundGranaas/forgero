package com.sigmundgranaas.forgero.data.loading.impl;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.condition.api.ConditionCodec;
import com.sigmundgranaas.forgero.core.condition.api.DynamicCondition;
import com.sigmundgranaas.forgero.core.condition.api.StaticCondition;
import com.sigmundgranaas.forgero.data.loading.api.data.ExtensionData;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.loading.impl.codec.AttributeCodecs;
import com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants;
import com.sigmundgranaas.forgero.data.loading.impl.codec.ExtensionCodecs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ExtensionDataCodecTest {

	private Codec<ExtensionData> extensionDataCodec;

	@BeforeEach
	void setUp() {
		Map<String, Codec<? extends StaticCondition>> staticCodecs = new HashMap<>();
		Map<String, Codec<? extends DynamicCondition>> dynamicCodecs = new HashMap<>();
		ConditionCodec conditionCodec = new ConditionCodec(staticCodecs, dynamicCodecs);

		Codec<List<AttributeData>> attributeListCodec = Codec.list(AttributeCodecs.create(conditionCodec));

		this.extensionDataCodec = ExtensionCodecs.create(attributeListCodec);
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

	private static final String FULL_EXTENSION_JSON = """
			{
			  "type": "forgero:extension",
			  "target": "forgero:materials/iron",
			  "priority": 10,
			  "tags": ["forgero:tool_material", "forgero:armor_material"],
			  "attributes": [
			    { "id": "forgero:iron-mining-speed", "type": "forgero:mining_speed", "computation": 6.0 }
			  ],
			  "properties": {
			    "forgero:tooltip": [
			      { "text": "Iron is a versatile metal" }
			    ]
			  }
			}
			""";

	@Test
	void testParseFullExtension() {
		ExtensionData data = parseSuccess(extensionDataCodec, FULL_EXTENSION_JSON);

		assertEquals(id("forgero:extension"), data.type());
		assertEquals(id("forgero:materials/iron"), data.target());
		assertEquals(10, data.priority());

		assertNotNull(data.tags());
		assertEquals(2, data.tags().size());
		assertTrue(data.tags().containsAll(List.of(id("forgero:tool_material"), id("forgero:armor_material"))));

		assertNotNull(data.attributes());
		assertEquals(1, data.attributes().size());
		assertEquals(Optional.of(id("forgero:iron-mining-speed")), data.attributes().get(0).id());
		assertEquals(6.0f, data.attributes().get(0).computation().value());

		assertNotNull(data.properties());
		assertEquals(1, data.properties().size());
		assertTrue(data.properties().containsKey("forgero:tooltip"));
	}

	@Test
	void testParseMinimalExtension() {
		String json = """
				{
				  "type": "forgero:extension",
				  "target": "forgero:materials/gold"
				}
				""";

		ExtensionData data = parseSuccess(extensionDataCodec, json);
		assertEquals(id("forgero:extension"), data.type());
		assertEquals(id("forgero:materials/gold"), data.target());
		assertEquals(ExtensionData.DEFAULT_PRIORITY, data.priority());
		assertNull(data.tags());
		assertNull(data.attributes());
		assertNull(data.properties());
	}

	@Test
	void testParseExtensionWithTagsOnly() {
		String json = """
				{
				  "type": "forgero:extension",
				  "target": "forgero:materials/copper",
				  "tags": ["forgero:decorative", "forgero:conductive"]
				}
				""";

		ExtensionData data = parseSuccess(extensionDataCodec, json);
		assertEquals(id("forgero:materials/copper"), data.target());
		assertEquals(0, data.priority());
		assertNotNull(data.tags());
		assertEquals(2, data.tags().size());
	}

	@Test
	void testParseExtensionWithNegativePriority() {
		String json = """
				{
				  "type": "forgero:extension",
				  "target": "forgero:materials/netherite",
				  "priority": -100
				}
				""";

		ExtensionData data = parseSuccess(extensionDataCodec, json);
		assertEquals(-100, data.priority());
	}

	@Test
	void testParseMissingTarget() {
		String json = """
				{
				  "type": "forgero:extension"
				}
				""";
		parseFailure(extensionDataCodec, json, "No key target");
	}

	@Test
	void testParseMissingType() {
		String json = """
				{
				  "target": "forgero:materials/iron"
				}
				""";
		parseFailure(extensionDataCodec, json, "No key type");
	}

	@Test
	void testResourceTypeDataImplementation() {
		String json = """
				{
				  "type": "forgero:extension",
				  "target": "forgero:materials/diamond"
				}
				""";

		ExtensionData data = parseSuccess(extensionDataCodec, json);

		// Test ResourceTypeData implementation
		assertEquals("diamond", data.name());
		assertEquals(List.of(), data.include());
		assertEquals(List.of(), data.localTags());
		assertEquals(List.of(), data.localAttributes());
		assertNull(data.host());
	}
}
