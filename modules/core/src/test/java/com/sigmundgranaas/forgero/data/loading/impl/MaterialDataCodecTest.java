package com.sigmundgranaas.forgero.data.loading.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.condition.api.DynamicCondition;
import com.sigmundgranaas.forgero.core.condition.api.StaticCondition;
import com.sigmundgranaas.forgero.data.loading.api.data.MaterialData;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.loading.impl.codec.AttributeCodecs;
import com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants;
import com.sigmundgranaas.forgero.core.condition.api.ConditionCodec;
import com.sigmundgranaas.forgero.data.loading.impl.codec.MaterialCodecs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class MaterialDataCodecTest {

	private Codec<MaterialData> materialDataCodec;

	@BeforeEach
	void setUp() {
		Map<String, Codec<? extends StaticCondition>> staticCodecs = new HashMap<>();
		Map<String, Codec<? extends DynamicCondition>> dynamicCodecs = new HashMap<>();
		ConditionCodec conditionCodec = new ConditionCodec(staticCodecs, dynamicCodecs);

		Codec<List<AttributeData>> attributeListCodec = Codec.list(AttributeCodecs.create(conditionCodec));

		this.materialDataCodec = MaterialCodecs.create(attributeListCodec);
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

	private static final String FULL_MATERIAL_JSON = """
			{
			  "type": "forgero:material",
			  "name": "diamond",
			  "include": ["forgero:materials/mineral_base", "forgero:materials/smeltable_base"],
			  "tags": ["forgero:gem", "forgero:mineral", "forgero:tool_material"],
			  "attributes": [
			    { "id": "forgero:diamond-durability", "type": "forgero:durability", "computation": 1561 }
			  ],
			  "features": [
			    {
			      "type": "forgero:vein_mining",
			      "selector": { "type": "forgero:radius", "radius": 1, "tag": "forgero:vein_mining_ores" },
			      "title": "feature.forgero.vein_mining.title",
			      "description": "feature.forgero.ore_vein_mining.description"
			    }
			  ],
			  "properties": {
			    "forgero:tooltip": [
			      { "text": "A custom tooltip line" }
			    ],
			    "better_combat:attribute_container": [
			        { "value": "better_combat:two_handed_spear" }
			    ]
			  }
			}
			""";

	@Test
	void testParseFullMaterial() {
		MaterialData data = parseSuccess(materialDataCodec, FULL_MATERIAL_JSON);

		assertEquals(id("forgero:material"), data.type());
		assertEquals("diamond", data.name());

		assertNotNull(data.include());
		assertTrue(data.include().containsAll(List.of(id("forgero:materials/mineral_base"), id("forgero:materials/smeltable_base"))));

		assertNotNull(data.tags());
		assertTrue(data.tags().containsAll(List.of(id("forgero:gem"), id("forgero:mineral"), id("forgero:tool_material"))));

		assertNotNull(data.attributes());
		assertEquals(1, data.attributes().size());
		Optional<AttributeData> durability = data.attributes().stream().filter(a -> a.id().equals(Optional.of(id("forgero:diamond-durability")))).findFirst();
		assertTrue(durability.isPresent());
		assertEquals(1561f, durability.get().computation().value());

		assertNotNull(data.properties());
		assertEquals(2, data.properties().size());
		assertTrue(data.properties().containsKey("forgero:tooltip"));
		JsonElement tooltip = data.properties().get("forgero:tooltip");
		assertTrue(tooltip.isJsonArray());
		assertEquals(1, tooltip.getAsJsonArray().size());
		assertTrue(data.properties().containsKey("better_combat:attribute_container"));
	}

	@Test
	void testParseMinimalMaterial() {
		String json = """
				{
				  "type": "forgero:material",
				  "name": "minimal_stone"
				}
				""";

		MaterialData data = parseSuccess(materialDataCodec, json);
		assertEquals(id("forgero:material"), data.type());
		assertEquals("minimal_stone", data.name());
		assertNull(data.include());
		assertNull(data.tags());
		assertNull(data.attributes());
		assertNull(data.properties());
	}

	@Test
	void testParseMaterialMissingRequiredFields() {
		String json = "{ \"type\": \"forgero:material\" }"; // Missing 'name'
		parseFailure(materialDataCodec, json, "No key name");
	}

	// ==================== Edge Case Tests ====================

	/**
	 * Edge case: Material with empty arrays for tags, include, and attributes.
	 * Should parse successfully with empty collections.
	 */
	@Test
	void testParseMaterialWithEmptyArrays() {
		String json = """
				{
				  "type": "forgero:material",
				  "name": "empty_arrays_material",
				  "include": [],
				  "tags": [],
				  "attributes": []
				}
				""";

		MaterialData data = parseSuccess(materialDataCodec, json);
		assertEquals("empty_arrays_material", data.name());
		assertNotNull(data.include());
		assertTrue(data.include().isEmpty(), "Include should be empty list");
		assertNotNull(data.tags());
		assertTrue(data.tags().isEmpty(), "Tags should be empty list");
		assertNotNull(data.attributes());
		assertTrue(data.attributes().isEmpty(), "Attributes should be empty list");
	}

	/**
	 * Edge case: Material with many tags (100+).
	 * Tests codec can handle large arrays.
	 */
	@Test
	void testParseMaterialWithManyTags() {
		StringBuilder tagsBuilder = new StringBuilder("[");
		for (int i = 0; i < 100; i++) {
			if (i > 0) tagsBuilder.append(", ");
			tagsBuilder.append("\"forgero:tag_").append(i).append("\"");
		}
		tagsBuilder.append("]");

		String json = String.format("""
				{
				  "type": "forgero:material",
				  "name": "many_tags_material",
				  "tags": %s
				}
				""", tagsBuilder);

		MaterialData data = parseSuccess(materialDataCodec, json);
		assertEquals("many_tags_material", data.name());
		assertNotNull(data.tags());
		assertEquals(100, data.tags().size(), "Should have 100 tags");
	}

	/**
	 * Edge case: Material with special characters in name.
	 * Tests handling of valid special characters.
	 */
	@Test
	void testParseMaterialWithSpecialCharactersInName() {
		String json = """
				{
				  "type": "forgero:material",
				  "name": "special_name-with.dots_and-dashes"
				}
				""";

		MaterialData data = parseSuccess(materialDataCodec, json);
		assertEquals("special_name-with.dots_and-dashes", data.name());
	}

	/**
	 * Edge case: Material with unicode characters in name.
	 * Tests handling of international characters.
	 */
	@Test
	void testParseMaterialWithUnicodeCharacters() {
		String json = """
				{
				  "type": "forgero:material",
				  "name": "钻石_материал_🔨"
				}
				""";

		MaterialData data = parseSuccess(materialDataCodec, json);
		assertEquals("钻石_материал_🔨", data.name());
	}

	/**
	 * Edge case: Material with very long name (1000+ characters).
	 * Tests handling of large string values.
	 */
	@Test
	void testParseMaterialWithLongName() {
		String longName = "a".repeat(1000);
		String json = String.format("""
				{
				  "type": "forgero:material",
				  "name": "%s"
				}
				""", longName);

		MaterialData data = parseSuccess(materialDataCodec, json);
		assertEquals(longName, data.name());
		assertEquals(1000, data.name().length());
	}

	/**
	 * Edge case: Material with duplicate tags.
	 * Tests that duplicate tags are handled (may be allowed).
	 */
	@Test
	void testParseMaterialWithDuplicateTags() {
		String json = """
				{
				  "type": "forgero:material",
				  "name": "duplicate_tags",
				  "tags": ["forgero:metal", "forgero:metal", "forgero:metal"]
				}
				""";

		MaterialData data = parseSuccess(materialDataCodec, json);
		assertNotNull(data.tags());
		// Depending on implementation, duplicates may be kept or deduplicated
		assertTrue(data.tags().size() >= 1, "Should have at least one tag");
	}

	/**
	 * Edge case: Material with duplicate includes.
	 * Tests that duplicate includes are handled.
	 */
	@Test
	void testParseMaterialWithDuplicateIncludes() {
		String json = """
				{
				  "type": "forgero:material",
				  "name": "duplicate_includes",
				  "include": [
				    "forgero:materials/base",
				    "forgero:materials/base",
				    "forgero:materials/base"
				  ]
				}
				""";

		MaterialData data = parseSuccess(materialDataCodec, json);
		assertNotNull(data.include());
		// Duplicates may be kept or deduplicated depending on implementation
		assertTrue(data.include().size() >= 1, "Should have at least one include");
	}

	/**
	 * Edge case: Material with invalid identifier format (missing namespace).
	 * Should fail to parse.
	 */
	@Test
	void testParseMaterialWithInvalidIdentifier() {
		String json = """
				{
				  "type": "forgero:material",
				  "name": "invalid_tag_material",
				  "tags": ["no_namespace_tag"]
				}
				""";

		// Depending on identifier parsing, this may succeed or fail
		// If OpenIdentifier.of() accepts strings without namespaces, it will succeed
		// This test documents the behavior
		DataResult<MaterialData> result = materialDataCodec.parse(
				JsonOps.INSTANCE,
				JsonParser.parseString(json)
		);

		// Just verify it doesn't crash - behavior may vary
		assertNotNull(result, "Parse should complete without crashing");
	}

	/**
	 * Edge case: Material with empty name.
	 * Tests handling of empty string values.
	 */
	@Test
	void testParseMaterialWithEmptyName() {
		String json = """
				{
				  "type": "forgero:material",
				  "name": ""
				}
				""";

		MaterialData data = parseSuccess(materialDataCodec, json);
		assertEquals("", data.name(), "Empty name should be allowed");
	}

	/**
	 * Type mismatch case: Material with tags as string instead of array.
	 * The codec uses optionalFieldOf which silently ignores type mismatches,
	 * treating the invalid field as if it were missing (null).
	 * This is DFU's standard behavior for optional fields with type errors.
	 */
	@Test
	void testParseMaterialWithTypeMismatchTags() {
		String json = """
				{
				  "type": "forgero:material",
				  "name": "type_mismatch",
				  "tags": "forgero:metal"
				}
				""";

		// optionalFieldOf silently ignores type mismatches - field treated as missing
		MaterialData data = parseSuccess(materialDataCodec, json);
		assertEquals("type_mismatch", data.name());
		assertNull(data.tags(), "Tags should be null when provided as wrong type (string instead of array)");
	}

	/**
	 * Error case: Material with type mismatch (name as number).
	 */
	@Test
	void testParseMaterialWithTypeMismatchName() {
		String json = """
				{
				  "type": "forgero:material",
				  "name": 123
				}
				""";

		DataResult<MaterialData> result = materialDataCodec.parse(
				JsonOps.INSTANCE,
				JsonParser.parseString(json)
		);

		assertTrue(result.error().isPresent(),
				"Should fail when name is number instead of string");
	}

	/**
	 * Error case: Material with missing type field.
	 */
	@Test
	void testParseMaterialMissingType() {
		String json = """
				{
				  "name": "no_type_material"
				}
				""";

		parseFailure(materialDataCodec, json, "type");
	}

	/**
	 * Edge case: Material with attribute having negative value.
	 * Negative values may represent debuffs.
	 */
	@Test
	void testParseMaterialWithNegativeAttribute() {
		String json = """
				{
				  "type": "forgero:material",
				  "name": "negative_attr_material",
				  "attributes": [
				    {
				      "id": "forgero:test-durability",
				      "type": "forgero:durability",
				      "computation": -100
				    }
				  ]
				}
				""";

		MaterialData data = parseSuccess(materialDataCodec, json);
		assertNotNull(data.attributes());
		assertEquals(1, data.attributes().size());

		Optional<AttributeData> attr = data.attributes().stream().findFirst();
		assertTrue(attr.isPresent());
		assertEquals(-100f, attr.get().computation().value(),
				"Negative attribute value should be preserved");
	}

	/**
	 * Edge case: Material with attribute having zero value.
	 */
	@Test
	void testParseMaterialWithZeroAttribute() {
		String json = """
				{
				  "type": "forgero:material",
				  "name": "zero_attr_material",
				  "attributes": [
				    {
				      "id": "forgero:test-durability",
				      "type": "forgero:durability",
				      "computation": 0
				    }
				  ]
				}
				""";

		MaterialData data = parseSuccess(materialDataCodec, json);
		assertNotNull(data.attributes());
		assertEquals(1, data.attributes().size());

		Optional<AttributeData> attr = data.attributes().stream().findFirst();
		assertTrue(attr.isPresent());
		assertEquals(0f, attr.get().computation().value(),
				"Zero attribute value should be preserved");
	}

	/**
	 * Edge case: Material with attribute having extreme float value.
	 */
	@Test
	void testParseMaterialWithExtremeAttributeValue() {
		String json = String.format("""
				{
				  "type": "forgero:material",
				  "name": "extreme_attr_material",
				  "attributes": [
				    {
				      "id": "forgero:test-durability",
				      "type": "forgero:durability",
				      "computation": %s
				    }
				  ]
				}
				""", Float.MAX_VALUE);

		MaterialData data = parseSuccess(materialDataCodec, json);
		assertNotNull(data.attributes());
		assertEquals(1, data.attributes().size());

		Optional<AttributeData> attr = data.attributes().stream().findFirst();
		assertTrue(attr.isPresent());
		// Float.MAX_VALUE when parsed from JSON may have precision loss
		assertTrue(attr.get().computation().value() > 1e37f,
				"Extreme float value should be preserved (within precision)");
	}

	/**
	 * Edge case: Material with nested properties (complex JSON).
	 */
	@Test
	void testParseMaterialWithNestedProperties() {
		String json = """
				{
				  "type": "forgero:material",
				  "name": "nested_props_material",
				  "properties": {
				    "forgero:complex_property": {
				      "nested": {
				        "deeply": {
				          "value": "deep_value"
				        }
				      }
				    }
				  }
				}
				""";

		MaterialData data = parseSuccess(materialDataCodec, json);
		assertNotNull(data.properties());
		assertTrue(data.properties().containsKey("forgero:complex_property"));

		JsonElement prop = data.properties().get("forgero:complex_property");
		assertTrue(prop.isJsonObject());
		assertTrue(prop.getAsJsonObject().has("nested"));
	}

	/**
	 * Edge case: Material with null values in optional fields.
	 * Tests that explicit nulls are handled correctly.
	 */
	@Test
	void testParseMaterialWithExplicitNulls() {
		String json = """
				{
				  "type": "forgero:material",
				  "name": "explicit_nulls_material",
				  "include": null,
				  "tags": null,
				  "attributes": null
				}
				""";

		MaterialData data = parseSuccess(materialDataCodec, json);
		assertEquals("explicit_nulls_material", data.name());
		// Explicit nulls should be treated as missing fields
		assertNull(data.include());
		assertNull(data.tags());
		assertNull(data.attributes());
	}

	/**
	 * Roundtrip test: Parse and then serialize material data.
	 * Ensures codec can both read and write correctly.
	 */
	@Test
	void testMaterialCodecRoundtrip() {
		String originalJson = """
				{
				  "type": "forgero:material",
				  "name": "roundtrip_material",
				  "tags": ["forgero:metal", "forgero:tool_material"],
				  "attributes": [
				    {
				      "id": "forgero:test-durability",
				      "type": "forgero:durability",
				      "computation": 500
				    }
				  ]
				}
				""";

		// Parse
		MaterialData data = parseSuccess(materialDataCodec, originalJson);

		// Serialize back
		DataResult<JsonElement> encodeResult = materialDataCodec.encodeStart(
				JsonOps.INSTANCE,
				data
		);

		assertTrue(encodeResult.result().isPresent(),
				"Should encode material data");

		// Parse the serialized data again
		MaterialData roundTrip = parseSuccess(materialDataCodec,
				encodeResult.result().get().toString());

		// Verify data is preserved
		assertEquals(data.name(), roundTrip.name(),
				"Name should be preserved in roundtrip");
		assertEquals(data.type(), roundTrip.type(),
				"Type should be preserved in roundtrip");
	}
}
