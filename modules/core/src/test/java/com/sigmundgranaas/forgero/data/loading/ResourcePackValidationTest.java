package com.sigmundgranaas.forgero.data.loading;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.core.condition.api.ConditionCodec;
import com.sigmundgranaas.forgero.core.condition.api.DynamicCondition;
import com.sigmundgranaas.forgero.core.condition.api.StaticCondition;
import com.sigmundgranaas.forgero.data.loading.api.data.MaterialData;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.EquipmentTemplateData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.PartTemplateData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.UpgradeSlotData;
import com.sigmundgranaas.forgero.data.loading.impl.codec.AttributeCodecs;
import com.sigmundgranaas.forgero.data.loading.impl.codec.EquipmentTemplateCodecs;
import com.sigmundgranaas.forgero.data.loading.impl.codec.MaterialCodecs;
import com.sigmundgranaas.forgero.data.loading.impl.codec.PartTemplateCodecs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Resource Pack Validation")
class ResourcePackValidationTest {

	private Codec<MaterialData> materialCodec;
	private Codec<PartTemplateData> partTemplateCodec;
	private Codec<EquipmentTemplateData> equipmentTemplateCodec;

	@BeforeEach
	void setUp() {
		Map<String, Codec<? extends StaticCondition>> staticCodecs = new HashMap<>();
		Map<String, Codec<? extends DynamicCondition>> dynamicCodecs = new HashMap<>();
		ConditionCodec conditionCodec = new ConditionCodec(staticCodecs, dynamicCodecs);
		Codec<List<AttributeData>> attributeListCodec = Codec.list(AttributeCodecs.create(conditionCodec));
		Codec<List<UpgradeSlotData>> upgradeSlotCodec = Codec.list(PartTemplateCodecs.UPGRADE_SLOT_DATA_CODEC);
		
		this.materialCodec = MaterialCodecs.create(attributeListCodec);
		this.partTemplateCodec = PartTemplateCodecs.create(attributeListCodec, upgradeSlotCodec);
		this.equipmentTemplateCodec = EquipmentTemplateCodecs.create(attributeListCodec, upgradeSlotCodec);
	}

	private <T> DataResult<T> parse(Codec<T> codec, String json) {
		return codec.parse(JsonOps.INSTANCE, JsonParser.parseString(json));
	}

	@Nested
	@DisplayName("Material JSON Validation")
	class MaterialValidation {

		@Test
		@DisplayName("Valid material with all fields loads successfully")
		void validMaterialLoadsSuccessfully() {
			String json = """
				{
				  "type": "forgero:material",
				  "name": "diamond",
				  "tags": ["forgero:gem", "forgero:tool_material"],
				  "attributes": [
				    { "id": "forgero:diamond-durability", "type": "forgero:durability", "computation": 1561 }
				  ]
				}
				""";

			DataResult<MaterialData> result = parse(materialCodec, json);

			assertTrue(result.result().isPresent(), "Valid material should parse successfully");
			MaterialData data = result.result().get();
			assertEquals("diamond", data.name());
			assertEquals(1561f, data.attributes().get(0).computation().value());
		}

		@Test
		@DisplayName("Minimal material with only required fields loads successfully")
		void minimalMaterialLoadsSuccessfully() {
			String json = """
				{
				  "type": "forgero:material",
				  "name": "custom_material"
				}
				""";

			DataResult<MaterialData> result = parse(materialCodec, json);

			assertTrue(result.result().isPresent(), "Minimal material should parse successfully");
			assertEquals("custom_material", result.result().get().name());
		}

		@Test
		@DisplayName("Missing 'name' field produces clear error message")
		void missingNameFieldProducesClearError() {
			String json = """
				{
				  "type": "forgero:material"
				}
				""";

			DataResult<MaterialData> result = parse(materialCodec, json);

			assertTrue(result.error().isPresent(), "Missing required field should fail");
			String errorMessage = result.error().get().message();
			assertTrue(errorMessage.contains("name"),
					"Error should mention the missing 'name' field. Got: " + errorMessage);
		}

		@Test
		@DisplayName("Missing 'type' field produces clear error message")
		void missingTypeFieldProducesClearError() {
			String json = """
				{
				  "name": "my_material"
				}
				""";

			DataResult<MaterialData> result = parse(materialCodec, json);

			assertTrue(result.error().isPresent(), "Missing type field should fail");
			String errorMessage = result.error().get().message();
			assertTrue(errorMessage.contains("type"),
					"Error should mention the missing 'type' field. Got: " + errorMessage);
		}

		@Test
		@DisplayName("Attribute with float computation is parsed correctly")
		void attributeWithFloatComputationIsParsedCorrectly() {
			String json = """
				{
				  "type": "forgero:material",
				  "name": "precise_material",
				  "attributes": [
				    { "type": "forgero:durability", "computation": 1.5 }
				  ]
				}
				""";

			DataResult<MaterialData> result = parse(materialCodec, json);

			assertTrue(result.result().isPresent());
			assertEquals(1.5f, result.result().get().attributes().get(0).computation().value(),
					"Float computation values should be preserved");
		}

		@Test
		@DisplayName("Wrong type for tags field is handled gracefully")
		void wrongTypeForTagsFieldIsHandledGracefully() {
			String json = """
				{
				  "type": "forgero:material",
				  "name": "wrong_tags_type",
				  "tags": "forgero:metal"
				}
				""";

			DataResult<MaterialData> result = parse(materialCodec, json);

			// optionalFieldOf silently ignores type mismatches
			assertTrue(result.result().isPresent());
			assertNull(result.result().get().tags(),
					"Tags should be null when provided as wrong type");
		}

		@Test
		@DisplayName("Negative attribute values are allowed")
		void negativeAttributeValuesAreAllowed() {
			String json = """
				{
				  "type": "forgero:material",
				  "name": "debuff_material",
				  "attributes": [
				    { "id": "test", "type": "forgero:durability", "computation": -50 }
				  ]
				}
				""";

			DataResult<MaterialData> result = parse(materialCodec, json);

			assertTrue(result.result().isPresent());
			assertEquals(-50f, result.result().get().attributes().get(0).computation().value(),
					"Negative values should be preserved for debuff effects");
		}

		@Test
		@DisplayName("Material includes are resolved correctly")
		void materialIncludesAreResolvedCorrectly() {
			String json = """
				{
				  "type": "forgero:material",
				  "name": "derived_material",
				  "include": ["forgero:materials/metal_base", "forgero:materials/smeltable"]
				}
				""";

			DataResult<MaterialData> result = parse(materialCodec, json);

			assertTrue(result.result().isPresent());
			MaterialData data = result.result().get();
			assertNotNull(data.include());
			assertEquals(2, data.include().size());
		}
	}

	@Nested
	@DisplayName("Attribute Definition Validation")
	class AttributeValidation {

		@Test
		@DisplayName("Attribute with all fields loads correctly")
		void attributeWithAllFieldsLoadsCorrectly() {
			String json = """
				{
				  "type": "forgero:material",
				  "name": "test",
				  "attributes": [
				    {
				      "id": "forgero:my-damage",
				      "type": "forgero:attack_damage",
				      "computation": 5,
				      "operator": "forgero:addition",
				      "order": "forgero:base"
				    }
				  ]
				}
				""";

			DataResult<MaterialData> result = parse(materialCodec, json);

			assertTrue(result.result().isPresent());
			List<AttributeData> attrs = result.result().get().attributes();
			assertEquals(1, attrs.size());
			Optional<com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier> id = attrs.get(0).id();
			assertTrue(id.isPresent());
			assertEquals("forgero:my-damage", id.get().toString());
		}

		@Test
		@DisplayName("Attribute with only required fields loads correctly")
		void attributeWithOnlyRequiredFieldsLoadsCorrectly() {
			String json = """
				{
				  "type": "forgero:material",
				  "name": "test",
				  "attributes": [
				    { "type": "forgero:durability", "computation": 100 }
				  ]
				}
				""";

			DataResult<MaterialData> result = parse(materialCodec, json);

			assertTrue(result.result().isPresent());
			assertEquals(100f, result.result().get().attributes().get(0).computation().value());
		}
	}

	@Nested
	@DisplayName("Part Template JSON Validation")
	class PartTemplateValidation {

		@Test
		@DisplayName("Valid part template with structure loads successfully")
		void validPartTemplateLoadsSuccessfully() {
			String json = """
				{
				  "type": "forgero:part_template",
				  "name": "pickaxe_head",
				  "tags": ["forgero:pickaxe_head", "forgero:tool_part"],
				  "structure": {
				    "id": "{material}-pickaxe_head",
				    "slots": {
				      "material": { "type": "forgero:tool_material" }
				    }
				  }
				}
				""";

			DataResult<PartTemplateData> result = parse(partTemplateCodec, json);

			assertTrue(result.result().isPresent(), "Valid part template should parse successfully");
			PartTemplateData data = result.result().get();
			assertEquals("pickaxe_head", data.name());
			assertNotNull(data.structure());
			assertEquals(1, data.structure().slots().size());
		}

		@Test
		@DisplayName("Missing 'structure' field produces clear error")
		void missingStructureFieldProducesClearError() {
			String json = """
				{
				  "type": "forgero:part_template",
				  "name": "missing_structure_part"
				}
				""";

			DataResult<PartTemplateData> result = parse(partTemplateCodec, json);

			assertTrue(result.error().isPresent(), "Missing structure should fail");
			String errorMessage = result.error().get().message();
			assertTrue(errorMessage.contains("structure"),
					"Error should mention 'structure'. Got: " + errorMessage);
		}

		@Test
		@DisplayName("Missing 'name' field produces clear error")
		void missingNameFieldProducesClearError() {
			String json = """
				{
				  "type": "forgero:part_template",
				  "structure": {
				    "slots": {
				      "material": { "type": "forgero:tool_material" }
				    }
				  }
				}
				""";

			DataResult<PartTemplateData> result = parse(partTemplateCodec, json);

			assertTrue(result.error().isPresent(), "Missing name should fail");
			String errorMessage = result.error().get().message();
			assertTrue(errorMessage.contains("name"),
					"Error should mention 'name'. Got: " + errorMessage);
		}

		@Test
		@DisplayName("Part template with upgrade slots loads correctly")
		void partTemplateWithUpgradeSlotsLoadsCorrectly() {
			String json = """
				{
				  "type": "forgero:part_template",
				  "name": "enhanced_blade",
				  "structure": {
				    "slots": {
				      "material": { "type": "forgero:tool_material" }
				    }
				  },
				  "upgrades": [
				    { "id": "forgero:gem_slot_1", "type": "forgero:gem" },
				    { "id": "forgero:gem_slot_2", "type": "forgero:gem", "tier": 2 }
				  ]
				}
				""";

			DataResult<PartTemplateData> result = parse(partTemplateCodec, json);

			assertTrue(result.result().isPresent());
			PartTemplateData data = result.result().get();
			assertNotNull(data.upgrades());
			assertEquals(2, data.upgrades().size());
		}

		@Test
		@DisplayName("Part template with attributes loads correctly")
		void partTemplateWithAttributesLoadsCorrectly() {
			String json = """
				{
				  "type": "forgero:part_template",
				  "name": "heavy_head",
				  "structure": {
				    "slots": {
				      "material": { "type": "forgero:tool_material" }
				    }
				  },
				  "attributes": [
				    { "type": "forgero:attack_damage", "computation": 2, "operator": "forgero:multiplication" }
				  ]
				}
				""";

			DataResult<PartTemplateData> result = parse(partTemplateCodec, json);

			assertTrue(result.result().isPresent());
			assertNotNull(result.result().get().attributes());
			assertEquals(1, result.result().get().attributes().size());
		}
	}

	@Nested
	@DisplayName("Equipment Template JSON Validation")
	class EquipmentTemplateValidation {

		@Test
		@DisplayName("Valid equipment template loads successfully")
		void validEquipmentTemplateLoadsSuccessfully() {
			String json = """
				{
				  "type": "forgero:equipment_template",
				  "name": "pickaxe",
				  "tags": ["forgero:pickaxe", "forgero:tool"],
				  "structure": {
				    "id": "{head}-{handle}",
				    "slots": {
				      "head": { "type": "forgero:pickaxe_head" },
				      "handle": { "type": "forgero:handle" }
				    }
				  }
				}
				""";

			DataResult<EquipmentTemplateData> result = parse(equipmentTemplateCodec, json);

			assertTrue(result.result().isPresent(), "Valid equipment template should parse successfully");
			EquipmentTemplateData data = result.result().get();
			assertEquals("pickaxe", data.name());
			assertNotNull(data.structure());
			assertEquals(2, data.structure().slots().size());
		}

		@Test
		@DisplayName("Missing 'structure' field produces clear error")
		void missingStructureFieldProducesClearError() {
			String json = """
				{
				  "type": "forgero:equipment_template",
				  "name": "broken_tool"
				}
				""";

			DataResult<EquipmentTemplateData> result = parse(equipmentTemplateCodec, json);

			assertTrue(result.error().isPresent(), "Missing structure should fail");
			String errorMessage = result.error().get().message();
			assertTrue(errorMessage.contains("structure"),
					"Error should mention 'structure'. Got: " + errorMessage);
		}

		@Test
		@DisplayName("Missing 'name' field produces clear error")
		void missingNameFieldProducesClearError() {
			String json = """
				{
				  "type": "forgero:equipment_template",
				  "structure": {
				    "slots": {
				      "head": { "type": "forgero:pickaxe_head" }
				    }
				  }
				}
				""";

			DataResult<EquipmentTemplateData> result = parse(equipmentTemplateCodec, json);

			assertTrue(result.error().isPresent(), "Missing name should fail");
			String errorMessage = result.error().get().message();
			assertTrue(errorMessage.contains("name"),
					"Error should mention 'name'. Got: " + errorMessage);
		}

		@Test
		@DisplayName("Equipment with default component reference loads correctly")
		void equipmentWithDefaultComponentLoadsCorrectly() {
			String json = """
				{
				  "type": "forgero:equipment_template",
				  "name": "sword",
				  "structure": {
				    "slots": {
				      "blade": { "type": "forgero:sword_blade", "default": "forgero:iron-sword_blade" },
				      "handle": { "type": "forgero:handle", "default_tag": "forgero:basic_handle" }
				    }
				  }
				}
				""";

			DataResult<EquipmentTemplateData> result = parse(equipmentTemplateCodec, json);

			assertTrue(result.result().isPresent());
			var slots = result.result().get().structure().slots();
			assertEquals("forgero:iron-sword_blade", slots.get("blade").defaultComponent().toString());
			assertEquals("forgero:basic_handle", slots.get("handle").defaultTag().toString());
		}

		@Test
		@DisplayName("Equipment template with upgrades and attributes loads correctly")
		void equipmentWithUpgradesAndAttributesLoadsCorrectly() {
			String json = """
				{
				  "type": "forgero:equipment_template",
				  "name": "enhanced_pickaxe",
				  "structure": {
				    "slots": {
				      "head": { "type": "forgero:pickaxe_head" }
				    }
				  },
				  "upgrades": [
				    { "id": "forgero:binding_slot", "type": "forgero:binding" }
				  ],
				  "attributes": [
				    { "type": "forgero:mining_speed", "computation": 1.2, "operator": "forgero:multiplication" }
				  ]
				}
				""";

			DataResult<EquipmentTemplateData> result = parse(equipmentTemplateCodec, json);

			assertTrue(result.result().isPresent());
			EquipmentTemplateData data = result.result().get();
			assertNotNull(data.upgrades());
			assertEquals(1, data.upgrades().size());
			assertNotNull(data.attributes());
			assertEquals(1, data.attributes().size());
		}
	}
}
