package com.sigmundgranaas.forgero.data;

import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.data.v3.codec.PartTemplateCodecs;
import com.sigmundgranaas.forgero.data.v3.codec.CodecConstants;
import com.sigmundgranaas.forgero.data.v3.dto.template.PartTemplateData;
import com.sigmundgranaas.forgero.data.v3.dto.template.UpgradeSlotData;
import com.sigmundgranaas.forgero.data.v3.dto.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.v3.dto.feature.VeinMiningFeatureData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PartTemplateDataCodecTest {

	@Test
	void testParseFullPartTemplate() {
		String json = """
				{
				  "type": "forgero:part_template",
				  "name": "mandrill-pickaxe-head",
				  "tags": ["forgero:pickaxe_head", "forgero:mandrill_head"],
				  "include": ["forgero:parts/heads/pickaxe_head_base"],
				  "structure": {
				    "id": "forgero:{material.name}-{shape.name}",
				    "slots": {
						 "material": {
						  "type": "forgero:tool_material",
						  "count": 3,
						  "description": "The primary material of the pickaxe head."
						},
						 "shape": {
						  "type": "forgero:pickaxe_head_shape",
						  "count": 1,
						  "description": "The shape of the pickaxe head."
						}
					}
				   },
				  "upgrades": [
				    {
				      "id": "forgero:head-smithing-offensive",
				      "type": "forgero:upgrade_material",
				      "tags": ["forgero:offensive"],
				      "tier": 1
				    },
				    {
				      "id": "forgero:head-smithing-cosmetic",
				      "type": "forgero:dye",
				      "tags": ["forgero:utility"],
				      "tier": 1
				    }
				  ],
				  "attributes": [
				    {
				      "id": "forgero:schematic-rarity-local",
				      "type": "forgero:rarity",
				      "computation": 75
				    },
				    {
				      "id": "forgero:variant-schematic-mining_speed-composite",
				      "type": "forgero:mining_speed",
				      "composite": "forgero:material-mining-speed",
				      "computation": { "multiply": 1.45 }
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

		DataResult<PartTemplateData> result = PartTemplateCodecs.PART_TEMPLATE_DATA_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json));
		assertTrue(result.result().isPresent(), "Parsing full PartTemplateData should succeed. " + result.error().map(DataResult.PartialResult::message).orElse(""));

		PartTemplateData data = result.result().get();
		assertEquals(CodecConstants.IDENTIFIER_FACTORY.of("forgero:part_template"), data.type());
		assertEquals("mandrill-pickaxe-head", data.name());

		// Test includes
		assertNotNull(data.include());
		assertEquals(1, data.include().size());
		assertTrue(data.include().contains(CodecConstants.IDENTIFIER_FACTORY.of("forgero:parts/heads/pickaxe_head_base")));

		// Test tags
		assertNotNull(data.tags());
		assertEquals(2, data.tags().size());
		assertTrue(data.tags().contains(CodecConstants.IDENTIFIER_FACTORY.of("forgero:pickaxe_head")));
		assertTrue(data.tags().contains(CodecConstants.IDENTIFIER_FACTORY.of("forgero:mandrill_head")));

		// Test structure
		assertNotNull(data.structure());
		assertEquals("forgero:{material.name}-{shape.name}", data.structure().id());
		assertNotNull(data.structure().slots().get("material"));
		assertEquals(CodecConstants.IDENTIFIER_FACTORY.of("forgero:tool_material"), data.structure().slots().get("material").type());
		assertEquals(3, data.structure().slots().get("material").count());
		assertEquals("The primary material of the pickaxe head.", data.structure().slots().get("material").description());

		// Test upgrades
		assertNotNull(data.upgrades());
		assertEquals(2, data.upgrades().size());
		UpgradeSlotData offensiveUpgrade = data.upgrades().get(0);
		assertEquals(CodecConstants.IDENTIFIER_FACTORY.of("forgero:head-smithing-offensive"), offensiveUpgrade.id());
		assertEquals(CodecConstants.IDENTIFIER_FACTORY.of("forgero:upgrade_material"), offensiveUpgrade.type());
		assertEquals(1, offensiveUpgrade.tags().size());
		assertTrue(offensiveUpgrade.tags().contains(CodecConstants.IDENTIFIER_FACTORY.of("forgero:offensive")));
		assertEquals(1, offensiveUpgrade.tier());

		// Test attributes
		assertNotNull(data.attributes());
		assertEquals(2, data.attributes().size());
		AttributeData rarity = data.attributes().get(0);
		assertEquals(CodecConstants.IDENTIFIER_FACTORY.of("forgero:schematic-rarity-local"), rarity.id());
		assertEquals(CodecConstants.IDENTIFIER_FACTORY.of("forgero:rarity"), rarity.type());
		assertEquals(75f, rarity.computation().value());
		AttributeData miningSpeed = data.attributes().get(1);
		assertEquals(CodecConstants.IDENTIFIER_FACTORY.of("forgero:variant-schematic-mining_speed-composite"), miningSpeed.id());
		assertEquals(CodecConstants.IDENTIFIER_FACTORY.of("forgero:mining_speed"), miningSpeed.type());
		assertEquals(CodecConstants.IDENTIFIER_FACTORY.of("forgero:material-mining-speed"), miningSpeed.composite());


		// Test features
		assertNotNull(data.features());
		assertEquals(1, data.features().size());
		assertTrue(data.features().get(0) instanceof VeinMiningFeatureData);
		VeinMiningFeatureData veinMining = (VeinMiningFeatureData) data.features().get(0);
		assertEquals(CodecConstants.IDENTIFIER_FACTORY.of("forgero:vein_mining"), veinMining.type());
	}

	@Test
	void testParseMinimalPartTemplate() {
		String json = """
				{
				  "type": "forgero:part_template",
				  "name": "minimal-part",
				  "structure": {
				    "id": "forgero:minimal-part",
				    "slots": {
						"material": {
						  "type": "forgero:material",
						  "count": 1
						},
						"shape": {
						 "type": "forgero:shape",
						 "count": 1
						}
				    }
				  }
				}
				""";

		DataResult<PartTemplateData> result = PartTemplateCodecs.PART_TEMPLATE_DATA_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json));
		assertTrue(result.result().isPresent(), "Parsing minimal PartTemplateData should succeed. " + result.error().map(DataResult.PartialResult::message).orElse(""));

		PartTemplateData data = result.result().get();
		assertEquals(CodecConstants.IDENTIFIER_FACTORY.of("forgero:part_template"), data.type());
		assertEquals("minimal-part", data.name());
		assertNull(data.include());
		assertNull(data.tags());
		assertNull(data.upgrades());
		assertNull(data.attributes());
		assertNull(data.features());

		assertNotNull(data.structure());
		assertEquals("forgero:minimal-part", data.structure().id()); // ADDED ASSERTION
		assertNotNull(data.structure().slots().get("material"));
		assertEquals(CodecConstants.IDENTIFIER_FACTORY.of("forgero:material"), data.structure().slots().get("material").type());
		assertEquals(1, data.structure().slots().get("material").count());
		assertNull(data.structure().slots().get("material").description());
	}

	@Test
	void testParsePartTemplateMissingStructure() {
		String json = """
				{
				  "type": "forgero:part_template",
				  "name": "invalid-part"
				}
				""";

		DataResult<PartTemplateData> result = PartTemplateCodecs.PART_TEMPLATE_DATA_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json));
		assertTrue(result.result().isEmpty(), "Parsing PartTemplateData missing 'structure' should fail.");
		assertTrue(result.error().isPresent());
		assertTrue(result.error().get().message().contains("No key structure"), result.error().get().message());
	}
}
