package com.sigmundgranaas.forgero.data.loading.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.tags.engine.TagGraph;
import com.sigmundgranaas.forgero.core.condition.api.DynamicCondition;
import com.sigmundgranaas.forgero.core.condition.api.StaticCondition;
import com.sigmundgranaas.forgero.core.condition.predicate.TagMatchCondition;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.EquipmentTemplateData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.UpgradeSlotData;
import com.sigmundgranaas.forgero.data.loading.impl.codec.AttributeCodecs;
import com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants;
import com.sigmundgranaas.forgero.core.condition.api.ConditionCodec;
import com.sigmundgranaas.forgero.data.loading.impl.codec.EquipmentTemplateCodecs;
import com.sigmundgranaas.forgero.data.loading.impl.codec.PartTemplateCodecs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.sigmundgranaas.forgero.testutils.TestIdentifiers.id;
import static org.junit.jupiter.api.Assertions.*;

class EquipmentTemplateDataCodecTest {

	private Codec<EquipmentTemplateData> equipmentTemplateDataCodec;

	@BeforeEach
	void setUp() {
		Map<String, Codec<? extends StaticCondition>> staticCodecs = new HashMap<>();
		Map<OpenIdentifier, Set<OpenIdentifier>> tagMap = new HashMap<>();
		tagMap.put(id("pickaxe"), new HashSet<>());
		staticCodecs.put("forgero:root_has_tag", TagMatchCondition.codec(() -> new TagGraph(tagMap)));
		Map<String, Codec<? extends DynamicCondition>> dynamicCodecs = new HashMap<>();
		ConditionCodec conditionCodec = new ConditionCodec(staticCodecs, dynamicCodecs);

		Codec<List<AttributeData>> attributeListCodec = Codec.list(AttributeCodecs.create(conditionCodec));
		Codec<List<UpgradeSlotData>> upgradeSlotDataListCodec = Codec.list(PartTemplateCodecs.UPGRADE_SLOT_DATA_CODEC);

		this.equipmentTemplateDataCodec = EquipmentTemplateCodecs.create(attributeListCodec, upgradeSlotDataListCodec);
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
	void testParseFullToolTemplate() {
		String json = """
				{
				  "type": "forgero:equipment_template",
				  "name": "pickaxe",
				  "include": ["forgero:tools/base_tool"],
				  "structure": {
				    "id": "forgero:{head.material.name}-pickaxe",
				    "slots": {
				      "head": { "type": "forgero:pickaxe_head", "default_tag": "forgero:default-pickaxe_head" },
				      "handle": { "type": "forgero:handle", "default": "forgero:oak-handle" }
				    }
				  },
				  "upgrades": [
				    { "id": "forgero:pickaxe-binding", "type": "forgero:binding", "description": "An optional binding for the tool." }
				  ],
				  "attributes": [
				    { "id": "forgero:tool-attack_speed", "type": "forgero:attack_speed", "computation": -2.8 }
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
				    "forgero:tool_model_override": { "path": "pickaxe_model" },
				    "forgero:icon_replacement": { "from": "pickaxe", "to": "custom_pickaxe" }
				  }
				}
				""";
		EquipmentTemplateData data = parseSuccess(equipmentTemplateDataCodec, json);
		assertEquals(id("forgero:equipment_template"), data.type());
		assertEquals("pickaxe", data.name());

		assertNotNull(data.include());
		assertTrue(data.include().contains(id("forgero:tools/base_tool")));

		assertNotNull(data.structure());
		assertEquals("forgero:{head.material.name}-pickaxe", data.structure().id());
		assertNotNull(data.structure().slots());
		assertEquals(2, data.structure().slots().size());

		var headSlot = data.structure().slots().get("head");
		assertNotNull(headSlot);
		assertEquals(id("forgero:pickaxe_head"), headSlot.type());
		assertEquals("forgero:default-pickaxe_head", headSlot.defaultTag().toString());

		var handleSlot = data.structure().slots().get("handle");
		assertNotNull(handleSlot);
		assertEquals(id("forgero:handle"), handleSlot.type());
		assertEquals("forgero:oak-handle", handleSlot.defaultComponent().toString());

		assertNotNull(data.upgrades());
		assertEquals(1, data.upgrades().size());
		UpgradeSlotData bindingUpgrade = data.upgrades().get(0);
		assertEquals(id("forgero:pickaxe-binding"), bindingUpgrade.id());
		assertEquals(id("forgero:binding"), bindingUpgrade.type());
		assertEquals("An optional binding for the tool.", bindingUpgrade.description());

		assertNotNull(data.attributes());
		assertEquals(1, data.attributes().size());
		AttributeData attackSpeed = data.attributes().get(0);
		assertEquals(id("forgero:tool-attack_speed"), attackSpeed.id());
		assertEquals(id("forgero:attack_speed"), attackSpeed.type());

		assertNotNull(data.properties());
		assertEquals(2, data.properties().size());
		assertTrue(data.properties().containsKey("forgero:tool_model_override"));
		JsonElement modelOverride = data.properties().get("forgero:tool_model_override");
		assertTrue(modelOverride.isJsonObject());
		assertEquals("pickaxe_model", modelOverride.getAsJsonObject().get("path").getAsString());
		assertTrue(data.properties().containsKey("forgero:icon_replacement"));
	}

	@Test
	void testParseMinimalToolTemplate() {
		String json = """
				{
				  "type": "forgero:equipment_template",
				  "name": "minimal_tool",
				  "structure": {
				    "id": "forgero:minimal_tool-id",
				    "slots": {
				      "main": { "type": "forgero:some_part" }
				    }
				  }
				}
				""";

		EquipmentTemplateData data = parseSuccess(equipmentTemplateDataCodec, json);
		assertNotNull(data.structure());
		assertEquals("forgero:minimal_tool-id", data.structure().id());
		assertNotNull(data.structure().slots());
		assertEquals(1, data.structure().slots().size());
		var mainSlot = data.structure().slots().get("main");
		assertNotNull(mainSlot);
		assertEquals(id("forgero:some_part"), mainSlot.type());
		assertNull(mainSlot.defaultComponent());
		assertNull(mainSlot.defaultTag());
		assertNull(data.upgrades());
		assertNull(data.attributes());
		assertNull(data.properties());
	}
}
