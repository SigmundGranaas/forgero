package com.sigmundgranaas.forgero.data.loading.impl;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.condition.api.DynamicCondition;
import com.sigmundgranaas.forgero.core.condition.api.StaticCondition;
import com.sigmundgranaas.forgero.data.loading.api.data.SchematicData;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.loading.impl.codec.AttributeCodecs;
import com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants;
import com.sigmundgranaas.forgero.data.loading.impl.codec.SchematicCodecs;
import com.sigmundgranaas.forgero.core.condition.api.ConditionCodec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SchematicDataCodecTest {

	private Codec<SchematicData> schematicDataCodec;

	@BeforeEach
	void setUp() {
		Map<String, Codec<? extends StaticCondition>> staticCodecs = new HashMap<>();
		Map<String, Codec<? extends DynamicCondition>> dynamicCodecs = new HashMap<>();
		ConditionCodec conditionCodec = new ConditionCodec(staticCodecs, dynamicCodecs);
		Codec<List<AttributeData>> attributeListCodec = Codec.list(AttributeCodecs.create(conditionCodec));
		this.schematicDataCodec = SchematicCodecs.create(attributeListCodec);
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
	void testParseFullSchematic() {
		String json = """
				{
				  "type": "forgero:schematic",
				  "name": "mandrill-head-schematic",
				  "include": ["forgero:schematics/base_schematic"],
				  "tags": ["forgero:common_schematic"],
				  "target": "forgero:mandrill-pickaxe-head",
				  "crafting_material": "minecraft:paper",
				  "properties": {
				    "forgero:rarity": "LEGENDARY",
				    "forgero:craft_experience": 100
				  }
				}
				""";

		SchematicData data = parseSuccess(schematicDataCodec, json);
		assertEquals(id("forgero:schematic"), data.type());
		assertEquals("mandrill-head-schematic", data.name());
		assertNotNull(data.include());
		assertTrue(data.include().contains(id("forgero:schematics/base_schematic")));
		assertNotNull(data.tags());
		assertTrue(data.tags().contains(id("forgero:common_schematic")));
		assertEquals(id("forgero:mandrill-pickaxe-head"), data.target());

		assertNotNull(data.properties());
		assertEquals(2, data.properties().size());
		assertTrue(data.properties().containsKey("forgero:rarity"));
		assertEquals("LEGENDARY", data.properties().get("forgero:rarity").getAsString());
		assertTrue(data.properties().containsKey("forgero:craft_experience"));
		assertEquals(100, data.properties().get("forgero:craft_experience").getAsInt());
	}

	@Test
	void testParseMinimalSchematic() {
		String json = """
				{
				  "type": "forgero:schematic",
				  "name": "simple-handle-schematic",
				  "target": "forgero:wooden-handle",
				  "crafting_material": "minecraft:stick"
				}
				""";

		SchematicData data = parseSuccess(schematicDataCodec, json);
		assertEquals(id("forgero:schematic"), data.type());
		assertEquals("simple-handle-schematic", data.name());
		assertNull(data.include());
		assertNull(data.tags());
		assertEquals(id("forgero:wooden-handle"), data.target());
		assertNull(data.properties());
	}

	@Test
	void testParseSchematicMissingRequiredFields() {
		String json = """
				{
				  "type": "forgero:schematic"
				}
				"""; // Missing 'name'
		parseFailure(schematicDataCodec, json, "No key name");
	}

	@Test
	void testParseSchematicWithAttributes() {
		String json = """
				{
				  "type": "forgero:schematic",
				  "name": "pickaxe_head",
				  "include": ["forgero:shapes/pickaxe_head"],
				  "attributes": [
					{
					  "id": "forgero:schematic-durability-bonus",
					  "type": "forgero:durability",
					  "computation": { "multiply": 1.1 }
					}
				  ],
				  "local_tags": ["forgero:schematic", "forgero:crafted"]
				}
				""";

		SchematicData data = parseSuccess(schematicDataCodec, json);
		assertEquals("pickaxe_head", data.name());
		assertNotNull(data.attributes());
		assertEquals(1, data.attributes().size());
		assertNotNull(data.localTags());
		assertEquals(2, data.localTags().size());
		assertTrue(data.localTags().contains(id("forgero:schematic")));
		assertTrue(data.localTags().contains(id("forgero:crafted")));
	}

	@Test
	void testParseSchematicBackwardCompatibility() {
		String json = """
				{
				  "type": "forgero:schematic",
				  "name": "old_schematic",
				  "target": "forgero:pickaxe_head"
				}
				""";

		SchematicData data = parseSuccess(schematicDataCodec, json);
		assertEquals(id("forgero:pickaxe_head"), data.target());
		assertNull(data.attributes());
		assertNull(data.localTags());
	}
}
