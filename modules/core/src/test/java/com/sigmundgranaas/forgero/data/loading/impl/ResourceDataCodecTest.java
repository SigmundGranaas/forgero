package com.sigmundgranaas.forgero.data.loading.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.condition.api.ConditionCodec;
import com.sigmundgranaas.forgero.core.condition.api.DynamicCondition;
import com.sigmundgranaas.forgero.core.condition.api.StaticCondition;
import com.sigmundgranaas.forgero.data.loading.api.data.DefinitionData;
import com.sigmundgranaas.forgero.data.loading.api.data.ResourceData;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.UpgradeSlotData;
import com.sigmundgranaas.forgero.data.loading.impl.codec.AttributeCodecs;
import com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants;
import com.sigmundgranaas.forgero.data.loading.impl.codec.PartTemplateCodecs;
import com.sigmundgranaas.forgero.data.loading.impl.codec.ResourceDataCodec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ResourceDataCodecTest {

	private Codec<ResourceData> resourceDataCodec;

	@BeforeEach
	void setUp() {
		Map<String, Codec<? extends StaticCondition>> staticCodecs = new HashMap<>();
		Map<String, Codec<? extends DynamicCondition>> dynamicCodecs = new HashMap<>();
		ConditionCodec conditionCodec = new ConditionCodec(staticCodecs, dynamicCodecs);

		Codec<List<AttributeData>> attributeListCodec = Codec.list(AttributeCodecs.create(conditionCodec));
		Codec<List<UpgradeSlotData>> upgradeSlotDataListCodec = Codec.list(PartTemplateCodecs.UPGRADE_SLOT_DATA_CODEC);

		this.resourceDataCodec = ResourceDataCodec.create(attributeListCodec, upgradeSlotDataListCodec);
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

	@Nested
	class MaterialJsonCompatibility {

		private static final String FULL_MATERIAL_JSON = """
				{
				  "type": "forgero:material",
				  "name": "iron",
				  "include": ["forgero:materials/metal_base"],
				  "tags": ["forgero:metal", "forgero:tool_material"],
				  "local_tags": ["forgero:special"],
				  "attributes": [
				    { "id": "forgero:iron-durability", "type": "forgero:durability", "computation": 250 }
				  ],
				  "local_attributes": [
				    { "id": "forgero:iron-bonus", "type": "forgero:attack_damage", "computation": 1.5 }
				  ],
				  "properties": {
				    "forgero:tooltip": [{ "text": "A versatile metal" }]
				  }
				}
				""";

		@Test
		void shouldParseMaterialJson() {
			ResourceData data = parseSuccess(resourceDataCodec, FULL_MATERIAL_JSON);

			assertEquals(id("forgero:material"), data.type());
			assertEquals("iron", data.name());
		}

		@Test
		void shouldParseMaterialWithAllFields() {
			ResourceData data = parseSuccess(resourceDataCodec, FULL_MATERIAL_JSON);

			assertNotNull(data.include());
			assertEquals(1, data.include().size());
			assertTrue(data.include().contains(id("forgero:materials/metal_base")));

			assertNotNull(data.tags());
			assertEquals(2, data.tags().size());

			assertNotNull(data.localTags());
			assertEquals(1, data.localTags().size());

			assertNotNull(data.attributes());
			assertEquals(1, data.attributes().size());

			assertNotNull(data.localAttributes());
			assertEquals(1, data.localAttributes().size());

			assertNotNull(data.properties());
			assertEquals(1, data.properties().size());
		}

		@Test
		void shouldParseMaterialWithMinimalFields() {
			String json = """
					{
					  "type": "forgero:material",
					  "name": "stone"
					}
					""";

			ResourceData data = parseSuccess(resourceDataCodec, json);
			assertEquals(id("forgero:material"), data.type());
			assertEquals("stone", data.name());
			assertNull(data.include());
			assertNull(data.tags());
			assertNull(data.attributes());
		}
	}

	@Nested
	class ShapeJsonCompatibility {

		@Test
		void shouldParseShapeJson() {
			String json = """
					{
					  "type": "forgero:shape",
					  "name": "pickaxe_head",
					  "tags": ["forgero:tool_shape", "forgero:mining"]
					}
					""";

			ResourceData data = parseSuccess(resourceDataCodec, json);
			assertEquals(id("forgero:shape"), data.type());
			assertEquals("pickaxe_head", data.name());
			assertNotNull(data.tags());
			assertEquals(2, data.tags().size());
		}

		@Test
		void shouldParseShapeWithLocalTags() {
			String json = """
					{
					  "type": "forgero:shape",
					  "name": "blade",
					  "tags": ["forgero:weapon_shape"],
					  "local_tags": ["forgero:specific_blade"]
					}
					""";

			ResourceData data = parseSuccess(resourceDataCodec, json);
			assertNotNull(data.localTags());
			assertEquals(1, data.localTags().size());
			assertTrue(data.localTags().contains(id("forgero:specific_blade")));
		}
	}

	@Nested
	class SchematicJsonCompatibility {

		@Test
		void shouldParseSchematicJson() {
			String json = """
					{
					  "type": "forgero:schematic",
					  "name": "refined_pickaxe_head",
					  "include": ["forgero:shapes/pickaxe_head"],
					  "attributes": [
					    { "id": "forgero:refined-bonus", "type": "forgero:mining_speed", "computation": { "add": 2 } }
					  ]
					}
					""";

			ResourceData data = parseSuccess(resourceDataCodec, json);
			assertEquals(id("forgero:schematic"), data.type());
			assertEquals("refined_pickaxe_head", data.name());
			assertNotNull(data.include());
			assertTrue(data.include().contains(id("forgero:shapes/pickaxe_head")));
		}

		@Test
		void shouldParseSchematicWithDeprecatedTargetField() {
			String json = """
					{
					  "type": "forgero:schematic",
					  "name": "legacy_schematic",
					  "target": "forgero:parts/pickaxe_head_template"
					}
					""";

			ResourceData data = parseSuccess(resourceDataCodec, json);
			assertNotNull(data.target());
			assertEquals(id("forgero:parts/pickaxe_head_template"), data.target());
		}
	}

	@Nested
	class CastJsonCompatibility {

		@Test
		void shouldParseCastJson() {
			String json = """
					{
					  "type": "forgero:cast",
					  "name": "enhanced_cast",
					  "include": ["forgero:shapes/pickaxe_head"],
					  "tags": ["forgero:enhanced"]
					}
					""";

			ResourceData data = parseSuccess(resourceDataCodec, json);
			assertEquals(id("forgero:cast"), data.type());
			assertEquals("enhanced_cast", data.name());
		}
	}

	@Nested
	class StaticPartJsonCompatibility {

		@Test
		void shouldParseStaticPartJson() {
			String json = """
					{
					  "type": "forgero:static_part",
					  "name": "oak_handle",
					  "tags": ["forgero:handle"]
					}
					""";

			ResourceData data = parseSuccess(resourceDataCodec, json);
			assertEquals(id("forgero:static_part"), data.type());
			assertEquals("oak_handle", data.name());
			assertNotNull(data.tags());
		}

		@Test
		void shouldParseStaticPartWithUpgrades() {
			String json = """
					{
					  "type": "forgero:static_part",
					  "name": "modular_handle",
					  "upgrades": [
					    { "id": "forgero:binding_slot", "type": "forgero:binding" }
					  ]
					}
					""";

			ResourceData data = parseSuccess(resourceDataCodec, json);
			assertNotNull(data.upgrades());
			assertEquals(1, data.upgrades().size());
		}
	}

	@Nested
	class DefinitionDataInterface {

		@Test
		void resourceDataShouldImplementDefinitionData() {
			String json = """
					{
					  "type": "forgero:material",
					  "name": "iron"
					}
					""";

			ResourceData data = parseSuccess(resourceDataCodec, json);
			assertTrue(data instanceof DefinitionData);
		}

		@Test
		void withMergedExtension_shouldReturnNewInstanceWithMergedFields() {
			ResourceData original = new ResourceData(
					id("forgero:material"),
					"iron",
					null,
					List.of(id("forgero:metal")),
					null,
					null,
					List.of(),
					null,
					Map.of()
			);

			List<OpenIdentifier> mergedTags = List.of(id("forgero:metal"), id("forgero:tool_material"));
			List<AttributeData> mergedAttrs = List.of();
			Map<String, JsonElement> mergedProps = Map.of();

			DefinitionData merged = original.withMergedExtension(mergedTags, mergedAttrs, mergedProps);

			assertTrue(merged instanceof ResourceData);
			ResourceData mergedResource = (ResourceData) merged;

			assertEquals(2, mergedResource.tags().size());
			assertTrue(mergedResource.tags().contains(id("forgero:tool_material")));

			// Unchanged fields should be preserved
			assertEquals("iron", mergedResource.name());
			assertEquals(id("forgero:material"), mergedResource.type());
		}

		@Test
		void withMergedExtension_shouldPreserveNonMergedFields() {
			ResourceData original = new ResourceData(
					id("forgero:material"),
					"iron",
					List.of(id("forgero:base")),
					List.of(id("forgero:metal")),
					List.of(id("forgero:local")),
					null,
					null,
					null,
					null,
					null,
					null
			);

			DefinitionData merged = original.withMergedExtension(
					List.of(id("forgero:new_tag")),
					List.of(),
					Map.of()
			);

			ResourceData mergedResource = (ResourceData) merged;

			// Non-merged fields should be unchanged
			assertNotNull(mergedResource.include());
			assertEquals(1, mergedResource.include().size());
			assertNotNull(mergedResource.localTags());
			assertEquals(1, mergedResource.localTags().size());
		}
	}

	@Nested
	class ValidationTests {

		@Test
		void shouldFailWhenMissingRequiredName() {
			String json = """
					{
					  "type": "forgero:material"
					}
					""";
			parseFailure(resourceDataCodec, json, "No key name");
		}

		@Test
		void shouldFailWhenMissingRequiredType() {
			String json = """
					{
					  "name": "iron"
					}
					""";
			parseFailure(resourceDataCodec, json, "No key type");
		}
	}
}
