package com.sigmundgranaas.forgero.data.loading.impl;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.condition.api.DynamicCondition;
import com.sigmundgranaas.forgero.core.condition.api.StaticCondition;
import com.sigmundgranaas.forgero.data.loading.api.data.CastData;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.loading.impl.codec.AttributeCodecs;
import com.sigmundgranaas.forgero.data.loading.impl.codec.CastCodecs;
import com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants;
import com.sigmundgranaas.forgero.core.condition.api.ConditionCodec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CastDataCodecTest {

	private Codec<CastData> castDataCodec;

	@BeforeEach
	void setUp() {
		Map<String, Codec<? extends StaticCondition>> staticCodecs = new HashMap<>();
		Map<String, Codec<? extends DynamicCondition>> dynamicCodecs = new HashMap<>();
		ConditionCodec conditionCodec = new ConditionCodec(staticCodecs, dynamicCodecs);
		Codec<List<AttributeData>> attributeListCodec = Codec.list(AttributeCodecs.create(conditionCodec));
		this.castDataCodec = CastCodecs.create(attributeListCodec);
	}

	private <T> T parseSuccess(Codec<T> codec, String json) {
		DataResult<T> result = codec.decode(JsonOps.INSTANCE, JsonParser.parseString(json)).map(pair -> pair.getFirst());
		assertTrue(result.result().isPresent(), "Expected successful parse but got error: " + result.error().map(DataResult.PartialResult::message).orElse(""));
		return result.result().get();
	}

	private <T> void parseFailure(Codec<T> codec, String json, String expectedErrorSubstring) {
		DataResult<T> result = codec.parse(JsonOps.INSTANCE, JsonParser.parseString(json));
		assertTrue(result.error().isPresent(), "Expected parse error but succeeded");
		String errorMessage = result.error().get().message();
		assertTrue(errorMessage.contains(expectedErrorSubstring),
				"Error message '" + errorMessage + "' does not contain '" + expectedErrorSubstring + "'");
	}

	private OpenIdentifier id(String id) {
		return CodecConstants.OPEN_IDENTIFIER_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString("\"" + id + "\""))
				.result()
				.orElseThrow();
	}

	@Test
	void testParseMinimalCast() {
		String json = """
				{
				  "type": "forgero:cast",
				  "name": "pickaxe_head_cast"
				}
				""";

		CastData data = parseSuccess(castDataCodec, json);
		assertEquals(id("forgero:cast"), data.type());
		assertEquals("pickaxe_head_cast", data.name());
		assertNull(data.include());
		assertNull(data.tags());
		assertNull(data.localTags());
		assertNull(data.host());
		assertNull(data.attributes());
		assertNull(data.localAttributes());
		assertNull(data.properties());
	}

	@Test
	void testParseCastWithInclude() {
		String json = """
				{
				  "type": "forgero:cast",
				  "name": "pickaxe_head_cast",
				  "include": ["forgero:pickaxe_head"]
				}
				""";

		CastData data = parseSuccess(castDataCodec, json);
		assertNotNull(data.include());
		assertEquals(1, data.include().size());
		assertEquals(id("forgero:pickaxe_head"), data.include().get(0));
	}

	@Test
	void testParseCastWithTags() {
		String json = """
				{
				  "type": "forgero:cast",
				  "name": "pickaxe_head_cast",
				  "tags": ["forgero:pickaxe_head_shape", "forgero:tool_part"]
				}
				""";

		CastData data = parseSuccess(castDataCodec, json);
		assertNotNull(data.tags());
		assertEquals(2, data.tags().size());
		assertTrue(data.tags().contains(id("forgero:pickaxe_head_shape")));
		assertTrue(data.tags().contains(id("forgero:tool_part")));
	}

	@Test
	void testParseCastWithLocalTags() {
		String json = """
				{
				  "type": "forgero:cast",
				  "name": "pickaxe_head_cast",
				  "tags": ["forgero:pickaxe_head_shape"],
				  "local_tags": ["forgero:cast", "forgero:molten"]
				}
				""";

		CastData data = parseSuccess(castDataCodec, json);
		assertNotNull(data.tags());
		assertEquals(1, data.tags().size());
		assertNotNull(data.localTags());
		assertEquals(2, data.localTags().size());
		assertTrue(data.localTags().contains(id("forgero:cast")));
		assertTrue(data.localTags().contains(id("forgero:molten")));
	}

	@Test
	void testParseCastWithAttributes() {
		String json = """
				{
				  "type": "forgero:cast",
				  "name": "pickaxe_head_cast",
				  "include": ["forgero:pickaxe_head"],
				  "attributes": [
					{
					  "id": "forgero:cast-durability-bonus",
					  "type": "forgero:durability",
					  "computation": { "multiply": 1.2 }
					}
				  ]
				}
				""";

		CastData data = parseSuccess(castDataCodec, json);
		assertNotNull(data.attributes());
		assertEquals(1, data.attributes().size());
		assertEquals(id("forgero:durability"), data.attributes().get(0).type());
	}

	@Test
	void testParseCastWithLocalAttributes() {
		String json = """
				{
				  "type": "forgero:cast",
				  "name": "pickaxe_head_cast",
				  "include": ["forgero:pickaxe_head"],
				  "local_attributes": [
					{
					  "id": "forgero:cast-bonus-1",
					  "type": "forgero:durability",
					  "computation": { "multiply": 1.2 }
					},
					{
					  "id": "forgero:cast-bonus-2",
					  "type": "forgero:mining_speed",
					  "computation": { "multiply": 1.1 }
					}
				  ],
				  "local_tags": ["forgero:cast"]
				}
				""";

		CastData data = parseSuccess(castDataCodec, json);
		assertNotNull(data.localAttributes());
		assertEquals(2, data.localAttributes().size());
		assertNotNull(data.localTags());
		assertEquals(1, data.localTags().size());
	}

	@Test
	void testParseCastWithBothAttributesAndLocalAttributes() {
		String json = """
				{
				  "type": "forgero:cast",
				  "name": "pickaxe_head_cast",
				  "include": ["forgero:pickaxe_head"],
				  "attributes": [
					{
					  "id": "forgero:inherited-attr",
					  "type": "forgero:durability",
					  "computation": { "multiply": 1.0 }
					}
				  ],
				  "local_attributes": [
					{
					  "id": "forgero:local-attr",
					  "type": "forgero:mining_speed",
					  "computation": { "multiply": 1.1 }
					}
				  ]
				}
				""";

		CastData data = parseSuccess(castDataCodec, json);
		assertNotNull(data.attributes());
		assertEquals(1, data.attributes().size());
		assertNotNull(data.localAttributes());
		assertEquals(1, data.localAttributes().size());
	}

	@Test
	void testParseCastWithHost() {
		String json = """
				{
				  "type": "forgero:cast",
				  "name": "pickaxe_head_cast",
				  "host": {
					"create": {
					  "id": "forgero:pickaxe_head_cast",
					  "class_name": "forgero:cast_item"
					}
				  }
				}
				""";

		CastData data = parseSuccess(castDataCodec, json);
		assertNotNull(data.host());
		assertNotNull(data.host().create());
		assertEquals("forgero:cast_item", data.host().create().itemClass());
	}

	@Test
	void testParseCastWithProperties() {
		String json = """
				{
				  "type": "forgero:cast",
				  "name": "pickaxe_head_cast",
				  "properties": {
					"custom_property": "custom_value",
					"numeric_property": 42
				  }
				}
				""";

		CastData data = parseSuccess(castDataCodec, json);
		assertNotNull(data.properties());
		assertEquals(2, data.properties().size());
		assertTrue(data.properties().containsKey("custom_property"));
		assertTrue(data.properties().containsKey("numeric_property"));
	}

	@Test
	void testParseCastCompleteFull() {
		String json = """
				{
				  "type": "forgero:cast",
				  "name": "mastercrafted_pickaxe_head_cast",
				  "include": ["forgero:pickaxe_head"],
				  "tags": ["forgero:pickaxe_head_shape", "forgero:tool_part"],
				  "local_tags": ["forgero:cast", "forgero:mastercrafted"],
				  "attributes": [
					{
					  "id": "forgero:base-durability",
					  "type": "forgero:durability",
					  "computation": { "multiply": 1.0 }
					}
				  ],
				  "local_attributes": [
					{
					  "id": "forgero:cast-durability-bonus",
					  "type": "forgero:durability",
					  "computation": { "multiply": 1.3 }
					},
					{
					  "id": "forgero:cast-mining-bonus",
					  "type": "forgero:mining_speed",
					  "computation": { "multiply": 1.15 }
					}
				  ],
				  "host": {
					"create": {
					  "id": "forgero:mastercrafted_pickaxe_head_cast",
					  "class_name": "forgero:cast_item"
					}
				  },
				  "properties": {
					"rarity": "rare",
					"durability_cost": 2
				  }
				}
				""";

		CastData data = parseSuccess(castDataCodec, json);
		assertEquals("mastercrafted_pickaxe_head_cast", data.name());
		assertNotNull(data.include());
		assertEquals(1, data.include().size());
		assertNotNull(data.tags());
		assertEquals(2, data.tags().size());
		assertNotNull(data.localTags());
		assertEquals(2, data.localTags().size());
		assertNotNull(data.attributes());
		assertEquals(1, data.attributes().size());
		assertNotNull(data.localAttributes());
		assertEquals(2, data.localAttributes().size());
		assertNotNull(data.host());
		assertNotNull(data.host().create());
		assertEquals("forgero:cast_item", data.host().create().itemClass());
		assertNotNull(data.properties());
		assertEquals(2, data.properties().size());
	}

	@Test
	void testParseCastMissingRequiredFields() {
		String json = """
				{
				  "type": "forgero:cast"
				}
				"""; // Missing 'name'
		parseFailure(castDataCodec, json, "No key name");
	}

	@Test
	void testParseCastMissingType() {
		String json = """
				{
				  "name": "pickaxe_head_cast"
				}
				"""; // Missing 'type'
		parseFailure(castDataCodec, json, "No key type");
	}

	@Test
	void testImplementsResourceTypeData() {
		String json = """
				{
				  "type": "forgero:cast",
				  "name": "test_cast",
				  "include": ["forgero:base_shape"]
				}
				""";

		CastData data = parseSuccess(castDataCodec, json);

		// Test shapeName() derivation from ResourceTypeData interface
		assertEquals("base_shape", data.shapeName());

		// Test other ResourceTypeData methods
		assertEquals(id("forgero:cast"), data.type());
		assertEquals("test_cast", data.name());
		assertNotNull(data.include());
		assertEquals(1, data.include().size());
	}

	@Test
	void testShapeNameDerivationWithoutInclude() {
		String json = """
				{
				  "type": "forgero:cast",
				  "name": "standalone_cast"
				}
				""";

		CastData data = parseSuccess(castDataCodec, json);

		// When no include, shapeName() should return the name
		assertEquals("standalone_cast", data.shapeName());
	}
}
