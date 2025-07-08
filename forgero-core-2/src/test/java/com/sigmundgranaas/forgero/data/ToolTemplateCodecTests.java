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
				  "type": "forgero:tool_template",
				  "name": "pickaxe",
				  "tags": ["forgero:tool", "forgero:pickaxe", "forgero:mining_tool"],
				  "include": ["forgero:tools/base_tool"],
				  "structure": {
				    "id": "forgero:{head.material.name}-pickaxe",
				    "slots": {
				      "head": {
				        "type": "forgero:pickaxe_head",
				        "default": "forgero:vanilla-pickaxe_head"
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
		assertEquals(CodecConstants.IDENTIFIER_FACTORY.of("forgero:tool_template"), data.type());
		assertEquals("pickaxe", data.name());

		// Test includes
		assertNotNull(data.include());
		assertEquals(1, data.include().size());
		assertTrue(data.include().contains(CodecConstants.IDENTIFIER_FACTORY.of("forgero:tools/base_tool")));

		// Test tags
		assertNotNull(data.tags());
		assertEquals(3, data.tags().size());
		assertTrue(data.tags().contains(CodecConstants.IDENTIFIER_FACTORY.of("forgero:tool")));
		assertTrue(data.tags().contains(CodecConstants.IDENTIFIER_FACTORY.of("forgero:pickaxe")));
		assertTrue(data.tags().contains(CodecConstants.IDENTIFIER_FACTORY.of("forgero:mining_tool")));

		// Test structure
		assertNotNull(data.structure());
		assertEquals("forgero:{head.material.name}-pickaxe", data.structure().id()); // ADDED ASSERTION for ID
		assertNotNull(data.structure().slots()); // Slots are now nested
		assertEquals(2, data.structure().slots().size());

		assertTrue(data.structure().slots().containsKey("head"));
		assertEquals(CodecConstants.IDENTIFIER_FACTORY.of("forgero:pickaxe_head"), data.structure().slots().get("head").type());
		assertEquals(CodecConstants.IDENTIFIER_FACTORY.of("forgero:vanilla-pickaxe_head"), data.structure().slots().get("head").defaultComponent());

		assertTrue(data.structure().slots().containsKey("handle"));
		assertEquals(CodecConstants.IDENTIFIER_FACTORY.of("forgero:handle"), data.structure().slots().get("handle").type());
		assertEquals(CodecConstants.IDENTIFIER_FACTORY.of("forgero:oak-handle"), data.structure().slots().get("handle").defaultComponent());

		// Test upgrades
		assertNotNull(data.upgrades());
		assertEquals(1, data.upgrades().size());
		UpgradeSlotData bindingUpgrade = data.upgrades().get(0);
		assertEquals(CodecConstants.IDENTIFIER_FACTORY.of("forgero:pickaxe-binding"), bindingUpgrade.id());
		assertEquals(CodecConstants.IDENTIFIER_FACTORY.of("forgero:binding"), bindingUpgrade.type());
		assertEquals("An optional binding for the tool.", bindingUpgrade.description());

		// Test attributes
		assertNotNull(data.attributes());
		assertEquals(1, data.attributes().size());
		AttributeData attackSpeed = data.attributes().get(0);
		assertEquals(CodecConstants.IDENTIFIER_FACTORY.of("forgero:tool-attack_speed"), attackSpeed.id());
		assertEquals(CodecConstants.IDENTIFIER_FACTORY.of("forgero:attack_speed"), attackSpeed.type());
		assertEquals(-2.8f, attackSpeed.computation().value());
		assertNull(data.features());
	}

	@Test
	void testParseMinimalToolTemplate() {
		String json = """
				{
				  "type": "forgero:tool_template",
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
		assertTrue(result.result().isPresent(), "Parsing minimal ToolTemplateData should succeed. " + result.error().map(DataResult.PartialResult::message).orElse(""));

		EquipmentTemplateData data = result.result().get();
		assertEquals(CodecConstants.IDENTIFIER_FACTORY.of("forgero:tool_template"), data.type());
		assertEquals("minimal_tool", data.name());
		assertNull(data.include());
		assertNull(data.tags());
		assertNull(data.upgrades());
		assertNull(data.attributes());
		assertNull(data.features());

		assertNotNull(data.structure());
		assertEquals("forgero:minimal_tool", data.structure().id()); // ADDED ASSERTION for ID
		assertNotNull(data.structure().slots());
		assertEquals(1, data.structure().slots().size());
		assertTrue(data.structure().slots().containsKey("main"));
		assertEquals(CodecConstants.IDENTIFIER_FACTORY.of("forgero:some_part"), data.structure().slots().get("main").type());
		assertNull(data.structure().slots().get("main").defaultComponent());
	}

	@Test
	void testParseToolTemplateMissingStructure() {
		String json = """
				{
				  "type": "forgero:tool_template",
				  "name": "invalid-tool"
				}
				""";

		DataResult<EquipmentTemplateData> result = EquipmentTemplateCodecs.TOOL_TEMPLATE_DATA_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json));
		assertTrue(result.result().isEmpty(), "Parsing ToolTemplateData missing 'structure' should fail.");
		assertTrue(result.error().isPresent());
		assertTrue(result.error().get().message().contains("No key structure"));
	}
}
