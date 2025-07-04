package com.sigmundgranaas.forgero.data;

import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.data.v3.codec.MaterialCodecs;
import com.sigmundgranaas.forgero.data.v3.codec.CodecConstants; // Added import
import com.sigmundgranaas.forgero.data.v3.dto.MaterialData;
import com.sigmundgranaas.forgero.data.v3.dto.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.v3.dto.feature.VeinMiningFeatureData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MaterialDataCodecTest {

	@Test
	void testParseFullMaterial() {
		String json = """
				{
				  "type": "forgero:material",
				  "name": "diamond",
				  "include": ["forgero:materials/mineral_base", "forgero:materials/smeltable_base"],
				  "tags": ["forgero:gem", "forgero:mineral", "forgero:tool_material"],
				  "attributes": [
				    {
				      "id": "forgero:diamond-durability",
				      "type": "forgero:durability",
				      "computation": 1561
				    },
				    {
				      "id": "forgero:diamond-mining_level",
				      "type": "forgero:mining_level",
				      "computation": 3
				    },
				    {
				      "id": "forgero:diamond-composite-mining-speed",
				      "type": "forgero:mining_speed",
				      "composite": "forgero:material-mining-speed",
				      "computation": { "add": 8, "order": "forgero:addition" }
				    }
				  ],
				  "features": [
				    {
				      "type": "forgero:vein_mining",
				      "selector": {
				        "type": "forgero:radius",
				        "radius": 1,
				        "tag": "forgero:vein_mining_ores"
				      },
				      "title": "feature.forgero.vein_mining.title",
				      "description": "feature.forgero.ore_vein_mining.description"
				    }
				  ]
				}
				""";

		DataResult<MaterialData> result = MaterialCodecs.MATERIAL_DATA_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json));
		assertTrue(result.result().isPresent(), "Parsing full material data should succeed. " + result.error().map(DataResult.PartialResult::message).orElse(""));

		MaterialData data = result.result().get();
		assertEquals(CodecConstants.IDENTIFIER_FACTORY.of("forgero:material"), data.type()); // Changed to OpenIdentifier
		assertEquals("diamond", data.name());

		// Test includes
		assertNotNull(data.include());
		assertEquals(2, data.include().size());
		assertTrue(data.include().contains(CodecConstants.IDENTIFIER_FACTORY.of("forgero:materials/mineral_base"))); // Changed to OpenIdentifier
		assertTrue(data.include().contains(CodecConstants.IDENTIFIER_FACTORY.of("forgero:materials/smeltable_base"))); // Changed to OpenIdentifier

		// Test tags
		assertNotNull(data.tags());
		assertEquals(3, data.tags().size());
		assertTrue(data.tags().contains(CodecConstants.IDENTIFIER_FACTORY.of("forgero:gem"))); // Changed to OpenIdentifier
		assertTrue(data.tags().contains(CodecConstants.IDENTIFIER_FACTORY.of("forgero:mineral"))); // Changed to OpenIdentifier
		assertTrue(data.tags().contains(CodecConstants.IDENTIFIER_FACTORY.of("forgero:tool_material"))); // Changed to OpenIdentifier

		// Test attributes (delegated to AttributeCodecs)
		assertNotNull(data.attributes());
		assertEquals(3, data.attributes().size());
		AttributeData durability = data.attributes().stream().filter(a -> a.id().equals(CodecConstants.IDENTIFIER_FACTORY.of("forgero:diamond-durability"))).findFirst().orElse(null); // Changed to OpenIdentifier
		assertNotNull(durability);
		assertEquals(1561f, durability.computation().value());

		// Test features (delegated to FeatureCodecs)
		assertNotNull(data.features());
		assertEquals(1, data.features().size());
		assertInstanceOf(VeinMiningFeatureData.class, data.features().get(0));
		VeinMiningFeatureData veinMining = (VeinMiningFeatureData) data.features().get(0);
		assertEquals(CodecConstants.IDENTIFIER_FACTORY.of("forgero:vein_mining"), veinMining.type()); // Changed to OpenIdentifier
		assertEquals(CodecConstants.IDENTIFIER_FACTORY.of("forgero:radius"), veinMining.selector().type()); // Changed to OpenIdentifier
		assertEquals(1, veinMining.selector().radius());
		assertEquals(CodecConstants.IDENTIFIER_FACTORY.of("forgero:vein_mining_ores"), veinMining.selector().tag()); // Changed to OpenIdentifier
	}

	@Test
	void testParseMinimalMaterial() {
		String json = """
				{
				  "type": "forgero:material",
				  "name": "minimal_stone"
				}
				""";

		DataResult<MaterialData> result = MaterialCodecs.MATERIAL_DATA_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json));
		assertTrue(result.result().isPresent(), "Parsing minimal material data should succeed.");

		MaterialData data = result.result().get();
		assertEquals(CodecConstants.IDENTIFIER_FACTORY.of("forgero:material"), data.type()); // Changed to OpenIdentifier
		assertEquals("minimal_stone", data.name());
		assertNull(data.include());
		assertNull(data.tags());
		assertNull(data.attributes());
		assertNull(data.features());
	}

	@Test
	void testParseMaterialWithOnlyTagsAndAttributes() {
		String json = """
				{
				  "type": "forgero:material",
				  "name": "basic_wood",
				  "tags": ["forgero:wood", "forgero:common"],
				  "attributes": [
				    {
				      "id": "forgero:wood-hardness",
				      "type": "forgero:hardness",
				      "computation": 2.0
				    }
				  ]
				}
				""";

		DataResult<MaterialData> result = MaterialCodecs.MATERIAL_DATA_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json));
		assertTrue(result.result().isPresent(), "Parsing material with only tags and attributes should succeed.");

		MaterialData data = result.result().get();
		assertEquals(CodecConstants.IDENTIFIER_FACTORY.of("forgero:material"), data.type()); // Changed to OpenIdentifier
		assertEquals("basic_wood", data.name());
		assertNull(data.include());
		assertNotNull(data.tags());
		assertEquals(2, data.tags().size());
		assertTrue(data.tags().contains(CodecConstants.IDENTIFIER_FACTORY.of("forgero:wood"))); // Changed to OpenIdentifier
		assertTrue(data.tags().contains(CodecConstants.IDENTIFIER_FACTORY.of("forgero:common"))); // Changed to OpenIdentifier
		assertNotNull(data.attributes());
		assertEquals(1, data.attributes().size());
		AttributeData hardness = data.attributes().get(0);
		assertEquals(CodecConstants.IDENTIFIER_FACTORY.of("forgero:wood-hardness"), hardness.id()); // Changed to OpenIdentifier
		assertEquals(CodecConstants.IDENTIFIER_FACTORY.of("forgero:hardness"), hardness.type()); // Changed to OpenIdentifier
		assertEquals(2.0f, hardness.computation().value());
		assertNull(data.features());
	}

	@Test
	void testParseMaterialMissingRequiredFields() {
		String json = """
				{
				  "type": "forgero:material"
				}
				"""; // Missing 'name'

		DataResult<MaterialData> result = MaterialCodecs.MATERIAL_DATA_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json));
		assertTrue(result.result().isEmpty(), "Parsing material data missing 'name' should fail.");
		assertTrue(result.error().isPresent());
		assertTrue(result.error().get().message().contains("No key name"), result.error().get().message());
	}
}
