package com.sigmundgranaas.forgero.data;

import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.data.v3.codec.ToolTemplateCodecs;
import com.sigmundgranaas.forgero.data.v3.codec.CodecConstants; // Added import
import com.sigmundgranaas.forgero.data.v3.dto.ToolTemplateData;
import com.sigmundgranaas.forgero.data.v3.dto.UpgradeSlotData;
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
				    "head": {
				      "type": "forgero:pickaxe_head",
				      "default": "forgero:vanilla-pickaxe_head"
				    },
				    "handle": {
				      "type": "forgero:handle",
				      "default": "forgero:oak-handle"
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

		DataResult<ToolTemplateData> result = ToolTemplateCodecs.TOOL_TEMPLATE_DATA_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json));
		assertTrue(result.result().isPresent(), "Parsing full ToolTemplateData should succeed. " + result.error().map(DataResult.PartialResult::message).orElse(""));

		ToolTemplateData data = result.result().get();
		assertEquals(CodecConstants.IDENTIFIER_FACTORY.of("forgero:tool_template"), data.type()); // Changed to OpenIdentifier
		assertEquals("pickaxe", data.name());

		// Test includes
		assertNotNull(data.include());
		assertEquals(1, data.include().size());
		assertTrue(data.include().contains(CodecConstants.IDENTIFIER_FACTORY.of("forgero:tools/base_tool"))); // Changed to OpenIdentifier

		// Test tags
		assertNotNull(data.tags());
		assertEquals(3, data.tags().size());
		assertTrue(data.tags().contains(CodecConstants.IDENTIFIER_FACTORY.of("forgero:tool"))); // Changed to OpenIdentifier
		assertTrue(data.tags().contains(CodecConstants.IDENTIFIER_FACTORY.of("forgero:pickaxe"))); // Changed to OpenIdentifier
		assertTrue(data.tags().contains(CodecConstants.IDENTIFIER_FACTORY.of("forgero:mining_tool"))); // Changed to OpenIdentifier

		// Test structure
		assertNotNull(data.structure());
		assertNotNull(data.structure().slots());
		assertEquals(2, data.structure().slots().size());

		assertTrue(data.structure().slots().containsKey("head"));
		assertEquals(CodecConstants.IDENTIFIER_FACTORY.of("forgero:pickaxe_head"), data.structure().slots().get("head").type()); // Changed to OpenIdentifier
		assertEquals(CodecConstants.IDENTIFIER_FACTORY.of("forgero:vanilla-pickaxe_head"), data.structure().slots().get("head").defaultComponent()); // Changed to OpenIdentifier

		assertTrue(data.structure().slots().containsKey("handle"));
		assertEquals(CodecConstants.IDENTIFIER_FACTORY.of("forgero:handle"), data.structure().slots().get("handle").type()); // Changed to OpenIdentifier
		assertEquals(CodecConstants.IDENTIFIER_FACTORY.of("forgero:oak-handle"), data.structure().slots().get("handle").defaultComponent()); // Changed to OpenIdentifier

		// Test upgrades
		assertNotNull(data.upgrades());
		assertEquals(1, data.upgrades().size());
		UpgradeSlotData bindingUpgrade = data.upgrades().get(0);
		assertEquals(CodecConstants.IDENTIFIER_FACTORY.of("forgero:pickaxe-binding"), bindingUpgrade.id()); // Changed to OpenIdentifier
		assertEquals(CodecConstants.IDENTIFIER_FACTORY.of("forgero:binding"), bindingUpgrade.type()); // Changed to OpenIdentifier
		assertEquals("An optional binding for the tool.", bindingUpgrade.description());

		// Test attributes
		assertNotNull(data.attributes());
		assertEquals(1, data.attributes().size());
		AttributeData attackSpeed = data.attributes().get(0);
		assertEquals(CodecConstants.IDENTIFIER_FACTORY.of("forgero:tool-attack_speed"), attackSpeed.id()); // Changed to OpenIdentifier
		assertEquals(CodecConstants.IDENTIFIER_FACTORY.of("forgero:attack_speed"), attackSpeed.type()); // Changed to OpenIdentifier
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
				    "main": {
				      "type": "forgero:some_part"
				    }
				  }
				}
				""";

		DataResult<ToolTemplateData> result = ToolTemplateCodecs.TOOL_TEMPLATE_DATA_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json));
		assertTrue(result.result().isPresent(), "Parsing minimal ToolTemplateData should succeed. " + result.error().map(DataResult.PartialResult::message).orElse(""));

		ToolTemplateData data = result.result().get();
		assertEquals(CodecConstants.IDENTIFIER_FACTORY.of("forgero:tool_template"), data.type()); // Changed to OpenIdentifier
		assertEquals("minimal_tool", data.name());
		assertNull(data.include());
		assertNull(data.tags());
		assertNull(data.upgrades());
		assertNull(data.attributes());
		assertNull(data.features());

		assertNotNull(data.structure());
		assertNotNull(data.structure().slots());
		assertEquals(1, data.structure().slots().size());
		assertTrue(data.structure().slots().containsKey("main"));
		assertEquals(CodecConstants.IDENTIFIER_FACTORY.of("forgero:some_part"), data.structure().slots().get("main").type()); // Changed to OpenIdentifier
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

		DataResult<ToolTemplateData> result = ToolTemplateCodecs.TOOL_TEMPLATE_DATA_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json));
		assertTrue(result.result().isEmpty(), "Parsing ToolTemplateData missing 'structure' should fail.");
		assertTrue(result.error().isPresent());
		assertTrue(result.error().get().message().contains("No key structure"));
	}
}
