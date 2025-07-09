package com.sigmundgranaas.forgero.data;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.data.loading.impl.codec.EquipmentTemplateCodecs;
import com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants;
import com.sigmundgranaas.forgero.data.loading.api.data.template.EquipmentTemplateData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EquipmentTemplateDataCodecTest {

	// region Helpers
	private <T> T parseSuccess(Codec<T> codec, String json) {
		DataResult<T> result = codec.parse(JsonOps.INSTANCE, JsonParser.parseString(json));
		assertTrue(result.result().isPresent(), "Parsing should succeed. Error: " + result.error().map(DataResult.PartialResult::message).orElse("No error message"));
		return result.result().get();
	}

	private OpenIdentifier id(String id) {
		return CodecConstants.IDENTIFIER_FACTORY.of(id);
	}
	// endregion

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
				  ]
				}
				""";
		EquipmentTemplateData data = parseSuccess(EquipmentTemplateCodecs.TOOL_TEMPLATE_DATA_CODEC, json);
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

		EquipmentTemplateData data = parseSuccess(EquipmentTemplateCodecs.TOOL_TEMPLATE_DATA_CODEC, json);
		assertNotNull(data.structure());
		assertEquals("forgero:minimal_tool-id", data.structure().id());
		assertNotNull(data.structure().slots());
		assertEquals(1, data.structure().slots().size());
		var mainSlot = data.structure().slots().get("main");
		assertNotNull(mainSlot);
		assertEquals(id("forgero:some_part"), mainSlot.type());
		assertNull(mainSlot.defaultComponent());
		assertNull(mainSlot.defaultTag());
	}
}
