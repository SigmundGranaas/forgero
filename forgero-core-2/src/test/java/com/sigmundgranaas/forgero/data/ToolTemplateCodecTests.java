package com.sigmundgranaas.forgero.data;

import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.data.v3.codec.EquipmentTemplateCodecs;
import com.sigmundgranaas.forgero.data.v3.codec.CodecConstants;
import com.sigmundgranaas.forgero.data.v3.dto.template.EquipmentTemplateData;
import com.sigmundgranaas.forgero.data.v3.dto.template.UpgradeSlotData;
import com.sigmundgranaas.forgero.data.v3.dto.attribute.AttributeData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ToolTemplateDataCodecTest {

	@Test
	void testParseFullToolTemplate() {
		String json = """
				{
				  "type": "forgero:equipment_template",
				  "name": "pickaxe",
				  "tags": ["forgero:tool", "forgero:pickaxe", "forgero:mining_tool"],
				  "include": ["forgero:tools/base_tool"],
				  "structure": {
				    "id": "forgero:{head.material.name}-pickaxe",
				    "slots": {
				      "head": {
				        "type": "forgero:pickaxe_head",
				        "default_tag": "forgero:default-pickaxe_head"
				      },
				      "handle": {
				        "type": "forgero:handle",
				        "default": "forgero:oak-handle"
				      }
				    }
				  },
				  "upgrades": [
				    {
				      "id": "forgero:pickaxe-binding",
				      "type": "forgero:binding",
				      "description": "An optional binding for the tool."
				    }
				  ],
				  "attributes": [
				    {
				      "id": "forgero:tool-attack_speed",
				      "type": "forgero:attack_speed",
				      "computation": -2.8
				    }
				  ]
				}
				""";

		DataResult<EquipmentTemplateData> result = EquipmentTemplateCodecs.TOOL_TEMPLATE_DATA_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json));
		assertTrue(result.result().isPresent(), "Parsing full ToolTemplateData should succeed. " + result.error().map(DataResult.PartialResult::message).orElse(""));

		EquipmentTemplateData data = result.result().get();
		assertEquals(CodecConstants.IDENTIFIER_FACTORY.of("forgero:equipment_template"), data.type());
		assertEquals("pickaxe", data.name());

		assertNotNull(data.include());
		assertEquals(1, data.include().size());
		assertTrue(data.include().contains(CodecConstants.IDENTIFIER_FACTORY.of("forgero:tools/base_tool")));

		assertNotNull(data.structure());
		assertEquals("forgero:{head.material.name}-pickaxe", data.structure().id());
		assertNotNull(data.structure().slots());
		assertEquals(2, data.structure().slots().size());

		assertTrue(data.structure().slots().containsKey("head"));
		assertEquals(CodecConstants.IDENTIFIER_FACTORY.of("forgero:pickaxe_head"), data.structure().slots().get("head").type());
		assertEquals("forgero:default-pickaxe_head", data.structure().slots().get("head").defaultTag().toString());

		assertTrue(data.structure().slots().containsKey("handle"));
		assertEquals(CodecConstants.IDENTIFIER_FACTORY.of("forgero:handle"), data.structure().slots().get("handle").type());
		assertEquals("forgero:oak-handle", data.structure().slots().get("handle").defaultComponent().toString());
	}

	@Test
	void testParseMinimalToolTemplate() {
		String json = """
				{
				  "type": "forgero:equipment_template",
				  "name": "minimal_tool",
				  "structure": {
				    "id": "forgero:minimal_tool",
				    "slots": {
				      "main": {
				        "type": "forgero:some_part"
				      }
				    }
				  }
				}
				""";

		DataResult<EquipmentTemplateData> result = EquipmentTemplateCodecs.TOOL_TEMPLATE_DATA_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json));
		assertTrue(result.result().isPresent(), "Parsing minimal ToolTemplateData should succeed.");

		EquipmentTemplateData data = result.result().get();
		assertNotNull(data.structure());
		assertEquals("forgero:minimal_tool", data.structure().id());
		assertNotNull(data.structure().slots());
		assertEquals(1, data.structure().slots().size());
		assertTrue(data.structure().slots().containsKey("main"));
		assertEquals(CodecConstants.IDENTIFIER_FACTORY.of("forgero:some_part"), data.structure().slots().get("main").type());
		assertNull(data.structure().slots().get("main").defaultComponent());
	}
}
