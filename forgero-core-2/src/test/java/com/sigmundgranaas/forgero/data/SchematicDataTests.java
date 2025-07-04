package com.sigmundgranaas.forgero.data;

import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.data.v3.codec.SchematicCodecs;
import com.sigmundgranaas.forgero.data.v3.codec.CodecConstants; // Added import
import com.sigmundgranaas.forgero.data.v3.dto.SchematicData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SchematicDataCodecTest {

	@Test
	void testParseFullSchematic() {
		String json = """
				{
				  "type": "forgero:schematic",
				  "name": "mandrill-head-schematic",
				  "include": ["forgero:schematics/base_schematic"],
				  "tags": ["forgero:common_schematic"],
				  "target": "forgero:mandrill-pickaxe-head",
				  "crafting_material": "minecraft:paper"
				}
				""";

		DataResult<SchematicData> result = SchematicCodecs.SCHEMATIC_DATA_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json));
		assertTrue(result.result().isPresent(), "Parsing full schematic data should succeed. " + result.error().map(DataResult.PartialResult::message).orElse(""));

		SchematicData data = result.result().get();
		assertEquals(CodecConstants.IDENTIFIER_FACTORY.of("forgero:schematic"), data.type()); // Changed to OpenIdentifier
		assertEquals("mandrill-head-schematic", data.name());
		assertNotNull(data.include());
		assertEquals(1, data.include().size());
		assertTrue(data.include().contains(CodecConstants.IDENTIFIER_FACTORY.of("forgero:schematics/base_schematic"))); // Changed to OpenIdentifier
		assertNotNull(data.tags());
		assertEquals(1, data.tags().size());
		assertTrue(data.tags().contains(CodecConstants.IDENTIFIER_FACTORY.of("forgero:common_schematic"))); // Changed to OpenIdentifier
		assertEquals(CodecConstants.IDENTIFIER_FACTORY.of("forgero:mandrill-pickaxe-head"), data.target()); // Changed to OpenIdentifier
		assertEquals("minecraft:paper", data.craftingMaterial());
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

		DataResult<SchematicData> result = SchematicCodecs.SCHEMATIC_DATA_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json));
		assertTrue(result.result().isPresent(), "Parsing minimal schematic data should succeed. " + result.error().map(DataResult.PartialResult::message).orElse(""));

		SchematicData data = result.result().get();
		assertEquals(CodecConstants.IDENTIFIER_FACTORY.of("forgero:schematic"), data.type()); // Changed to OpenIdentifier
		assertEquals("simple-handle-schematic", data.name());
		assertNull(data.include());
		assertNull(data.tags());
		assertEquals(CodecConstants.IDENTIFIER_FACTORY.of("forgero:wooden-handle"), data.target()); // Changed to OpenIdentifier
		assertEquals("minecraft:stick", data.craftingMaterial());
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

		DataResult<SchematicData> result = SchematicCodecs.SCHEMATIC_DATA_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json));
		assertTrue(result.result().isEmpty(), "Parsing schematic data missing 'target' should fail.");
		assertTrue(result.error().isPresent());
		assertTrue(result.error().get().message().contains("No key target"), result.error().get().message());
	}
}
