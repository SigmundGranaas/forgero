package com.sigmundgranaas.forgero.data.loading.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.property.condition.DynamicCondition;
import com.sigmundgranaas.forgero.core.property.condition.StaticCondition;
import com.sigmundgranaas.forgero.core.property.predicate.TagMatchCondition;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.loading.api.data.feature.FeatureData;
import com.sigmundgranaas.forgero.data.loading.api.data.feature.VeinMiningFeatureData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.PartTemplateData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.UpgradeSlotData;
import com.sigmundgranaas.forgero.data.loading.impl.codec.AttributeCodecs;
import com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants;
import com.sigmundgranaas.forgero.data.loading.impl.codec.ConditionCodec;
import com.sigmundgranaas.forgero.data.loading.impl.codec.FeatureCodecs;
import com.sigmundgranaas.forgero.data.loading.impl.codec.PartTemplateCodecs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class PartTemplateDataCodecTest {

	private Codec<PartTemplateData> partTemplateDataCodec;

	@BeforeEach
	void setUp() {
		Map<String, Codec<? extends StaticCondition>> staticCodecs = new HashMap<>();
		Map<String, Codec<? extends DynamicCondition>> dynamicCodecs = new HashMap<>();
		ConditionCodec conditionCodec = new ConditionCodec(staticCodecs, dynamicCodecs);

		Codec<List<AttributeData>> attributeListCodec = Codec.list(AttributeCodecs.create(conditionCodec));
		FeatureCodecs.registerCodecs(conditionCodec); // Ensure feature codecs are initialized
		Codec<List<FeatureData>> featureListCodec = FeatureCodecs.createFeatureDataListCodec();
		Codec<List<UpgradeSlotData>> upgradeSlotDataListCodec = Codec.list(PartTemplateCodecs.UPGRADE_SLOT_DATA_CODEC);

		this.partTemplateDataCodec = PartTemplateCodecs.create(attributeListCodec, featureListCodec, upgradeSlotDataListCodec);
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

	private static final String FULL_PART_TEMPLATE_JSON = """
			{
			  "type": "forgero:part_template",
			  "name": "mandrill-pickaxe-head",
			  "tags": ["forgero:pickaxe_head", "forgero:mandrill_head"],
			  "include": ["forgero:parts/heads/pickaxe_head_base"],
			  "structure": {
			    "id": "forgero:{material.name}-{shape.name}",
			    "slots": {
			      "material": { "type": "forgero:tool_material", "count": 3, "description": "The primary material of the pickaxe head." },
			      "shape": { "type": "forgero:pickaxe_head_shape", "count": 1, "description": "The shape of the pickaxe head." }
			    }
			  },
			  "upgrades": [
			    { "id": "forgero:head-smithing-offensive", "type": "forgero:upgrade_material", "tags": ["forgero:offensive"], "tier": 1 },
			    { "id": "forgero:head-smithing-cosmetic", "type": "forgero:dye", "tags": ["forgero:utility"], "tier": 1 }
			  ],
			  "attributes": [
			    { "id": "forgero:schematic-rarity-local", "type": "forgero:rarity", "computation": 75 },
			    { "id": "forgero:variant-schematic-mining_speed-composite", "type": "forgero:mining_speed", "composite": "forgero:material-mining-speed", "computation": { "multiply": 1.45 } }
			  ],
			  "features": [
			    { "type": "forgero:vein_mining", "selector": { "type": "forgero:radius", "radius": 1, "tag": "forgero:vein_mining_ores" }, "title": "feature.forgero.vein_mining.title", "description": "feature.forgero.ore_vein_mining.description" }
			  ],
			  "properties": {
			    "forgero:crafting_difficulty": 5,
			    "forgero:visual_blueprint": { "blueprint_type": "HEAD" }
			  }
			}
			""";

	@Test
	void testParseFullPartTemplate() {
		PartTemplateData data = parseSuccess(partTemplateDataCodec, FULL_PART_TEMPLATE_JSON);

		assertEquals(id("forgero:part_template"), data.type());
		assertEquals("mandrill-pickaxe-head", data.name());

		assertNotNull(data.include());
		assertTrue(data.include().contains(id("forgero:parts/heads/pickaxe_head_base")));

		assertNotNull(data.tags());
		assertTrue(data.tags().containsAll(List.of(id("forgero:pickaxe_head"), id("forgero:mandrill_head"))));

		assertNotNull(data.structure());
		assertEquals("forgero:{material.name}-{shape.name}", data.structure().id());
		var materialSlot = data.structure().slots().get("material");
		assertEquals(id("forgero:tool_material"), materialSlot.type());
		assertEquals(3, materialSlot.count());
		assertEquals("The primary material of the pickaxe head.", materialSlot.description());

		assertNotNull(data.upgrades());
		assertEquals(2, data.upgrades().size());
		UpgradeSlotData offensiveUpgrade = data.upgrades().get(0);
		assertEquals(id("forgero:head-smithing-offensive"), offensiveUpgrade.id());
		assertEquals(id("forgero:upgrade_material"), offensiveUpgrade.type());
		assertTrue(offensiveUpgrade.tags().contains(id("forgero:offensive")));
		assertEquals(1, offensiveUpgrade.tier());

		assertNotNull(data.attributes());
		assertEquals(2, data.attributes().size());
		AttributeData miningSpeed = data.attributes().get(1);
		assertEquals(id("forgero:variant-schematic-mining_speed-composite"), miningSpeed.id());

		assertNotNull(data.features());
		assertEquals(1, data.features().size());
		assertInstanceOf(VeinMiningFeatureData.class, data.features().get(0));

		assertNotNull(data.properties());
		assertEquals(2, data.properties().size());
		assertTrue(data.properties().containsKey("forgero:crafting_difficulty"));
		assertEquals(5, data.properties().get("forgero:crafting_difficulty").getAsInt());
		assertTrue(data.properties().containsKey("forgero:visual_blueprint"));
		JsonElement visualBlueprint = data.properties().get("forgero:visual_blueprint");
		assertTrue(visualBlueprint.isJsonObject());
		assertEquals("HEAD", visualBlueprint.getAsJsonObject().get("blueprint_type").getAsString());
	}

	@Test
	void testParseMinimalPartTemplate() {
		String json = """
				{
				  "type": "forgero:part_template",
				  "name": "minimal-part",
				  "structure": {
				    "id": "forgero:minimal-part-id",
				    "slots": {
				      "material": { "type": "forgero:material", "count": 1 }
				    }
				  }
				}
				""";

		PartTemplateData data = parseSuccess(partTemplateDataCodec, json);
		assertEquals(id("forgero:part_template"), data.type());
		assertEquals("minimal-part", data.name());
		assertNull(data.include());
		assertNull(data.tags());
		assertNull(data.upgrades());
		assertNull(data.attributes());
		assertNull(data.features());
		assertNull(data.properties());

		assertNotNull(data.structure());
		assertEquals("forgero:minimal-part-id", data.structure().id());
		var materialSlot = data.structure().slots().get("material");
		assertEquals(id("forgero:material"), materialSlot.type());
		assertEquals(1, materialSlot.count());
		assertNull(materialSlot.description());
	}

	@Test
	void testParsePartTemplateMissingStructure() {
		String json = """
				{
				  "type": "forgero:part_template",
				  "name": "invalid-part"
				}
				""";
		parseFailure(partTemplateDataCodec, json, "No key structure");
	}
}
