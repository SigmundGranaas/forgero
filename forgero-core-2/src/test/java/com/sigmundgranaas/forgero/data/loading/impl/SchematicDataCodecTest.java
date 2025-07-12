package com.sigmundgranaas.forgero.data.loading.impl;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.data.loading.impl.codec.SchematicCodecs;
import com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants;
import com.sigmundgranaas.forgero.data.loading.api.data.SchematicData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SchematicDataCodecTest {

	// region Helpers
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
	// endregion

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

		SchematicData data = parseSuccess(SchematicCodecs.SCHEMATIC_DATA_CODEC, json);
		assertEquals(id("forgero:schematic"), data.type());
		assertEquals("mandrill-head-schematic", data.name());
		assertNotNull(data.include());
		assertTrue(data.include().contains(id("forgero:schematics/base_schematic")));
		assertNotNull(data.tags());
		assertTrue(data.tags().contains(id("forgero:common_schematic")));
		assertEquals(id("forgero:mandrill-pickaxe-head"), data.target());
		assertEquals("minecraft:paper", data.craftingMaterial());

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

		SchematicData data = parseSuccess(SchematicCodecs.SCHEMATIC_DATA_CODEC, json);
		assertEquals(id("forgero:schematic"), data.type());
		assertEquals("simple-handle-schematic", data.name());
		assertNull(data.include());
		assertNull(data.tags());
		assertEquals(id("forgero:wooden-handle"), data.target());
		assertEquals("minecraft:stick", data.craftingMaterial());
		assertNull(data.properties());
	}

	@Test
	void testParseSchematicMissingRequiredFields() {
		String json = """
				{
				  "type": "forgero:schematic",
				  "name": "incomplete-schematic",
				  "crafting_material": "minecraft:paper"
				}
				"""; // Missing 'target'
		parseFailure(SchematicCodecs.SCHEMATIC_DATA_CODEC, json, "No key target");
	}
}
