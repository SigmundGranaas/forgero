package com.sigmundgranaas.forgero.data.loading.impl;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.core.condition.api.ConditionCodec;
import com.sigmundgranaas.forgero.core.condition.api.DynamicCondition;
import com.sigmundgranaas.forgero.core.condition.api.StaticCondition;
import com.sigmundgranaas.forgero.data.loading.api.data.DefinitionData;
import com.sigmundgranaas.forgero.data.loading.api.data.ResourceData;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.PartTemplateData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.UpgradeSlotData;
import com.sigmundgranaas.forgero.data.loading.impl.codec.AttributeCodecs;
import com.sigmundgranaas.forgero.data.loading.impl.codec.DefinitionCodecRegistry;
import com.sigmundgranaas.forgero.data.loading.impl.codec.PartTemplateCodecs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DefinitionCodecRegistryTest {

	private DefinitionCodecRegistry registry;
	private Codec<List<AttributeData>> attributeCodec;
	private Codec<List<UpgradeSlotData>> upgradeCodec;

	@BeforeEach
	void setUp() {
		Map<String, Codec<? extends StaticCondition>> staticCodecs = new HashMap<>();
		Map<String, Codec<? extends DynamicCondition>> dynamicCodecs = new HashMap<>();
		ConditionCodec conditionCodec = new ConditionCodec(staticCodecs, dynamicCodecs);

		attributeCodec = Codec.list(AttributeCodecs.create(conditionCodec));
		upgradeCodec = Codec.list(PartTemplateCodecs.UPGRADE_SLOT_DATA_CODEC);

		registry = DefinitionCodecRegistry.createDefault(attributeCodec, upgradeCodec);
	}

	@Nested
	class CodecLookup {

		@Test
		void shouldReturnRegisteredCodecForMaterial() {
			Codec<? extends DefinitionData> codec = registry.codecFor("material");
			assertNotNull(codec);
		}

		@Test
		void shouldReturnRegisteredCodecForShape() {
			Codec<? extends DefinitionData> codec = registry.codecFor("shape");
			assertNotNull(codec);
		}

		@Test
		void shouldReturnRegisteredCodecForSchematic() {
			Codec<? extends DefinitionData> codec = registry.codecFor("schematic");
			assertNotNull(codec);
		}

		@Test
		void shouldReturnRegisteredCodecForCast() {
			Codec<? extends DefinitionData> codec = registry.codecFor("cast");
			assertNotNull(codec);
		}

		@Test
		void shouldReturnRegisteredCodecForStaticPart() {
			Codec<? extends DefinitionData> codec = registry.codecFor("static_part");
			assertNotNull(codec);
		}

		@Test
		void shouldReturnRegisteredCodecForPartTemplate() {
			Codec<? extends DefinitionData> codec = registry.codecFor("part_template");
			assertNotNull(codec);
		}

		@Test
		void shouldReturnRegisteredCodecForEquipmentTemplate() {
			Codec<? extends DefinitionData> codec = registry.codecFor("equipment_template");
			assertNotNull(codec);
		}

		@Test
		void shouldReturnRegisteredCodecForExtension() {
			Codec<? extends DefinitionData> codec = registry.codecFor("extension");
			assertNotNull(codec);
		}

		@Test
		void shouldReturnDefaultCodecForUnknownType() {
			Codec<? extends DefinitionData> codec = registry.codecFor("unknown_type");
			assertNotNull(codec);
			// Default should be ResourceData codec
		}
	}

	@Nested
	class HasCodecFor {

		@Test
		void shouldReturnTrueForRegisteredType() {
			assertTrue(registry.hasCodecFor("material"));
			assertTrue(registry.hasCodecFor("shape"));
			assertTrue(registry.hasCodecFor("part_template"));
		}

		@Test
		void shouldReturnFalseForUnregisteredType() {
			assertFalse(registry.hasCodecFor("unknown_type"));
		}
	}

	@Nested
	class CustomRegistration {

		@Test
		void shouldAllowCustomCodecRegistration() {
			// Use the existing codec directly for the test
			Codec<? extends DefinitionData> materialCodec = registry.codecFor("material");

			registry.register("custom_type", materialCodec);
			assertTrue(registry.hasCodecFor("custom_type"));
		}

		@Test
		void registeredCodec_shouldOverrideDefault() {
			// Create a custom codec that always returns a specific resource
			Codec<ResourceData> customCodec = Codec.unit(() -> new ResourceData(
					null, "custom_name", null, null, null, null, null, null, null
			));

			registry.register("special", customCodec);
			Codec<? extends DefinitionData> retrieved = registry.codecFor("special");

			assertNotNull(retrieved);
			assertSame(customCodec, retrieved);
		}
	}

	@Nested
	class ParsingWithRegistry {

		@Test
		void materialCodec_shouldParseToResourceData() {
			String json = """
					{
					  "type": "forgero:material",
					  "name": "iron"
					}
					""";

			Codec<? extends DefinitionData> codec = registry.codecFor("material");
			DataResult<? extends DefinitionData> result = codec.parse(JsonOps.INSTANCE, JsonParser.parseString(json));

			assertTrue(result.result().isPresent());
			assertTrue(result.result().get() instanceof ResourceData);
		}

		@Test
		void partTemplateCodec_shouldParseToPartTemplateData() {
			String json = """
					{
					  "type": "forgero:part_template",
					  "name": "pickaxe_head_template",
					  "structure": {
					    "id": "forgero:{material.name}-pickaxe_head",
					    "slots": {
					      "material": { "type": "forgero:tool_material" },
					      "shape": { "type": "forgero:pickaxe_head_shape" }
					    }
					  }
					}
					""";

			Codec<? extends DefinitionData> codec = registry.codecFor("part_template");
			DataResult<? extends DefinitionData> result = codec.parse(JsonOps.INSTANCE, JsonParser.parseString(json));

			assertTrue(result.result().isPresent(), "Parsing should succeed. Error: " + result.error().map(e -> e.message()).orElse("no error"));
			assertTrue(result.result().get() instanceof PartTemplateData);
		}

		@Test
		void allResourceTypes_shouldParseToSameType() {
			String[] types = {"material", "shape", "schematic", "cast", "static_part"};

			for (String type : types) {
				String json = String.format("""
						{
						  "type": "forgero:%s",
						  "name": "test_%s"
						}
						""", type, type);

				Codec<? extends DefinitionData> codec = registry.codecFor(type);
				DataResult<? extends DefinitionData> result = codec.parse(JsonOps.INSTANCE, JsonParser.parseString(json));

				assertTrue(result.result().isPresent(), "Failed for type: " + type);
				assertTrue(result.result().get() instanceof ResourceData, "Expected ResourceData for type: " + type);
			}
		}
	}

	@Nested
	class LegacySupport {

		@Test
		void toolTemplate_shouldWorkAsAliasForEquipmentTemplate() {
			assertTrue(registry.hasCodecFor("tool_template"));

			String json = """
					{
					  "type": "forgero:tool_template",
					  "name": "pickaxe",
					  "structure": {
					    "id": "forgero:{head.material.name}-pickaxe",
					    "slots": {
					      "head": { "type": "forgero:pickaxe_head" },
					      "handle": { "type": "forgero:handle" }
					    }
					  }
					}
					""";

			Codec<? extends DefinitionData> codec = registry.codecFor("tool_template");
			DataResult<? extends DefinitionData> result = codec.parse(JsonOps.INSTANCE, JsonParser.parseString(json));

			assertTrue(result.result().isPresent(), "Parsing should succeed. Error: " + result.error().map(e -> e.message()).orElse("no error"));
		}
	}
}
