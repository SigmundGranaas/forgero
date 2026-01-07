package com.sigmundgranaas.forgero.data.pipeline;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.condition.api.ConditionCodec;
import com.sigmundgranaas.forgero.core.condition.api.DynamicCondition;
import com.sigmundgranaas.forgero.core.condition.api.StaticCondition;
import com.sigmundgranaas.forgero.core.condition.predicate.HasOtherContributorCondition;
import com.sigmundgranaas.forgero.core.condition.predicate.InSlotTypeCondition;
import com.sigmundgranaas.forgero.data.loading.api.data.ExtensionData;
import com.sigmundgranaas.forgero.data.loading.api.data.MaterialData;
import com.sigmundgranaas.forgero.data.loading.api.data.SchematicData;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.EquipmentTemplateData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.PartTemplateData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.UpgradeSlotData;
import com.sigmundgranaas.forgero.data.loading.impl.codec.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Data Pipeline Integration")
class DataPipelineIntegrationTest {

	private Codec<MaterialData> materialCodec;
	private Codec<PartTemplateData> partTemplateCodec;
	private Codec<EquipmentTemplateData> equipmentTemplateCodec;
	private Codec<SchematicData> schematicCodec;
	private Codec<ExtensionData> extensionCodec;

	@BeforeEach
	void setUp() {
		Map<String, Codec<? extends StaticCondition>> staticCodecs = new HashMap<>();
		staticCodecs.put("forgero:in_slot_type", InSlotTypeCondition.CODEC);
		staticCodecs.put("forgero:has_other_contributor", HasOtherContributorCondition.CODEC);

		Map<String, Codec<? extends DynamicCondition>> dynamicCodecs = new HashMap<>();
		ConditionCodec conditionCodec = new ConditionCodec(staticCodecs, dynamicCodecs);
		Codec<List<AttributeData>> attributeListCodec = Codec.list(AttributeCodecs.create(conditionCodec));
		Codec<List<UpgradeSlotData>> upgradeSlotCodec = Codec.list(PartTemplateCodecs.UPGRADE_SLOT_DATA_CODEC);

		this.materialCodec = MaterialCodecs.create(attributeListCodec);
		this.partTemplateCodec = PartTemplateCodecs.create(attributeListCodec, upgradeSlotCodec);
		this.equipmentTemplateCodec = EquipmentTemplateCodecs.create(attributeListCodec, upgradeSlotCodec);
		this.schematicCodec = SchematicCodecs.create(attributeListCodec);
		this.extensionCodec = ExtensionCodecs.create(attributeListCodec, upgradeSlotCodec);
	}

	private <T> DataResult<T> parseResource(Codec<T> codec, String resourcePath) throws IOException {
		try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
			if (is == null) {
				throw new IOException("Resource not found: " + resourcePath);
			}
			var reader = new InputStreamReader(is, StandardCharsets.UTF_8);
			var json = JsonParser.parseReader(reader);
			return codec.parse(JsonOps.INSTANCE, json);
		}
	}

	@Nested
	@DisplayName("Material JSON Loading")
	class MaterialJsonLoading {

		@Test
		@DisplayName("loads iron.json with all conditional attributes")
		void loadsIronJsonWithAllConditionalAttributes() throws IOException {
			DataResult<MaterialData> result = parseResource(materialCodec, "data/forgero/materials/iron.json");

			assertTrue(result.result().isPresent());
			MaterialData iron = result.result().get();

			assertEquals("Iron", iron.name());
			assertEquals(3, iron.tags().size());
			assertEquals(4, iron.attributes().size());

			AttributeData durability = iron.attributes().stream()
					.filter(a -> a.type().toString().equals("forgero:durability"))
					.findFirst()
					.orElseThrow();
			assertEquals(250f, durability.computation().value());
			assertNotNull(durability.condition());
		}

		@Test
		@DisplayName("loads diamond.json with multiple conditional attributes")
		void loadsDiamondJsonWithMultipleConditionalAttributes() throws IOException {
			DataResult<MaterialData> result = parseResource(materialCodec, "data/forgero/materials/diamond.json");

			assertTrue(result.result().isPresent());
			MaterialData diamond = result.result().get();

			assertEquals("Diamond", diamond.name());
			assertEquals(5, diamond.attributes().size());

			AttributeData attackDamage = diamond.attributes().stream()
					.filter(a -> a.type().toString().equals("forgero:attack_damage"))
					.findFirst()
					.orElseThrow();

			assertNotNull(attackDamage.condition());
			assertTrue(attackDamage.condition().isPresent());
			assertEquals(2, attackDamage.condition().get().staticConditions().size());
		}

		@Test
		@DisplayName("loads gold.json with rarity attribute")
		void loadsGoldJsonWithRarityAttribute() throws IOException {
			DataResult<MaterialData> result = parseResource(materialCodec, "data/forgero/materials/gold.json");

			assertTrue(result.result().isPresent());
			MaterialData gold = result.result().get();

			assertEquals("Gold", gold.name());

			AttributeData rarity = gold.attributes().stream()
					.filter(a -> a.type().toString().equals("forgero:rarity"))
					.findFirst()
					.orElseThrow();

			assertEquals(50f, rarity.computation().value());
			assertTrue(rarity.condition().isEmpty() || rarity.condition().get().staticConditions().isEmpty());
		}

		@Test
		@DisplayName("loads material with host identifiers")
		void loadsMaterialWithHostIdentifiers() throws IOException {
			DataResult<MaterialData> result = parseResource(materialCodec, "data/forgero/materials/iron.json");

			assertTrue(result.result().isPresent());
			MaterialData iron = result.result().get();

			assertNotNull(iron.host());
			assertEquals(2, iron.host().identifiers().size());
		}
	}

	@Nested
	@DisplayName("Part Template JSON Loading")
	class PartTemplateJsonLoading {

		@Test
		@DisplayName("loads pickaxe_head_template.json with structure")
		void loadsPickaxeHeadTemplateWithStructure() throws IOException {
			DataResult<PartTemplateData> result = parseResource(partTemplateCodec, "data/forgero/parts/pickaxe_head_template.json");

			assertTrue(result.result().isPresent());
			PartTemplateData template = result.result().get();

			assertEquals("Pickaxe Head", template.name());
			assertNotNull(template.structure());
			assertEquals(2, template.structure().slots().size());
			assertTrue(template.structure().slots().containsKey("material"));
			assertTrue(template.structure().slots().containsKey("shape"));
		}

		@Test
		@DisplayName("loads sword_blade_template.json with upgrades")
		void loadsSwordBladeTemplateWithUpgrades() throws IOException {
			DataResult<PartTemplateData> result = parseResource(partTemplateCodec, "data/forgero/parts/sword_blade_template.json");

			assertTrue(result.result().isPresent());
			PartTemplateData template = result.result().get();

			assertEquals("Sword Blade", template.name());
			assertNotNull(template.upgrades());
			assertEquals(1, template.upgrades().size());
			assertEquals("forgero:blade_gem_slot", template.upgrades().get(0).id().toString());
		}

		@Test
		@DisplayName("loads part template with generation config")
		void loadsPartTemplateWithGenerationConfig() throws IOException {
			DataResult<PartTemplateData> result = parseResource(partTemplateCodec, "data/forgero/parts/pickaxe_head_template.json");

			assertTrue(result.result().isPresent());
			PartTemplateData template = result.result().get();

			assertNotNull(template.generation());
			assertNotNull(template.generation().slots());
		}

		@Test
		@DisplayName("loads part template with structure and generation")
		void loadsPartTemplateWithStructureAndGeneration() throws IOException {
			DataResult<PartTemplateData> result = parseResource(partTemplateCodec, "data/forgero/parts/pickaxe_head_template.json");

			assertTrue(result.result().isPresent());
			PartTemplateData template = result.result().get();

			assertNotNull(template.structure());
			assertNotNull(template.generation());
			assertNotNull(template.generation().slots());
		}
	}

	@Nested
	@DisplayName("Equipment Template JSON Loading")
	class EquipmentTemplateJsonLoading {

		@Test
		@DisplayName("loads pickaxe_template.json with all slots")
		void loadsPickaxeTemplateWithAllSlots() throws IOException {
			DataResult<EquipmentTemplateData> result = parseResource(equipmentTemplateCodec, "data/forgero/equipment/pickaxe_template.json");

			assertTrue(result.result().isPresent());
			EquipmentTemplateData template = result.result().get();

			assertNotNull(template.structure());
			assertTrue(template.structure().slots().size() >= 1);
		}

		@Test
		@DisplayName("loads sword_template.json with upgrade slots")
		void loadsSwordTemplateWithUpgradeSlots() throws IOException {
			DataResult<EquipmentTemplateData> result = parseResource(equipmentTemplateCodec, "data/forgero/equipment/sword_template.json");

			assertTrue(result.result().isPresent());
			EquipmentTemplateData template = result.result().get();

			assertEquals("Sword", template.name());
			assertNotNull(template.upgrades());
			assertEquals(2, template.upgrades().size());
		}

		@Test
		@DisplayName("loads equipment template with default tags")
		void loadsEquipmentTemplateWithDefaultTags() throws IOException {
			DataResult<EquipmentTemplateData> result = parseResource(equipmentTemplateCodec, "data/forgero/equipment/sword_template.json");

			assertTrue(result.result().isPresent());
			EquipmentTemplateData template = result.result().get();

			var bladeSlot = template.structure().slots().get("blade");
			assertNotNull(bladeSlot);
			assertNotNull(bladeSlot.defaultTag());
		}
	}

	@Nested
	@DisplayName("Schematic JSON Loading")
	class SchematicJsonLoading {

		@Test
		@DisplayName("loads pickaxe_head_shape.json")
		void loadsPickaxeHeadShape() throws IOException {
			DataResult<SchematicData> result = parseResource(schematicCodec, "data/forgero/shapes/pickaxe_head_shape.json");

			assertTrue(result.result().isPresent());
			SchematicData schematic = result.result().get();

			assertNotNull(schematic.name());
			assertNotNull(schematic.tags());
		}

		@Test
		@DisplayName("loads schematic with attributes")
		void loadsSchematicWithAttributes() throws IOException {
			DataResult<SchematicData> result = parseResource(schematicCodec, "data/forgero/shapes/pickaxe_head_shape.json");

			assertTrue(result.result().isPresent());
			SchematicData schematic = result.result().get();

			assertNotNull(schematic.tags());
			assertTrue(schematic.tags().contains(OpenIdentifier.parse("forgero:pickaxe_head_shape")));
		}
	}

	@Nested
	@DisplayName("Extension JSON Loading")
	class ExtensionJsonLoading {

		@Test
		@DisplayName("loads extension with conditional bonus")
		void loadsExtensionWithConditionalBonus() throws IOException {
			DataResult<ExtensionData> result = parseResource(extensionCodec, "data/forgero/extensions/iron_tool_extension.json");

			assertTrue(result.result().isPresent());
			ExtensionData extension = result.result().get();

			assertEquals("forgero:iron", extension.target().toString());
			assertNotNull(extension.tags());
			assertEquals(2, extension.tags().size());
			assertNotNull(extension.attributes());
			assertEquals(1, extension.attributes().size());
		}
	}

	@Nested
	@DisplayName("Attribute Condition Parsing")
	class AttributeConditionParsing {

		@Test
		@DisplayName("parses in_slot_type condition correctly")
		void parsesInSlotTypeConditionCorrectly() throws IOException {
			DataResult<MaterialData> result = parseResource(materialCodec, "data/forgero/materials/iron.json");

			assertTrue(result.result().isPresent());
			MaterialData iron = result.result().get();

			AttributeData durability = iron.attributes().stream()
					.filter(a -> a.id().map(id -> id.toString().contains("durability")).orElse(false))
					.findFirst()
					.orElseThrow();

			assertTrue(durability.condition().isPresent());
			assertEquals(1, durability.condition().get().staticConditions().size());

			StaticCondition condition = durability.condition().get().staticConditions().get(0);
			assertInstanceOf(InSlotTypeCondition.class, condition);
		}

		@Test
		@DisplayName("parses has_other_contributor condition correctly")
		void parsesHasOtherContributorConditionCorrectly() throws IOException {
			DataResult<MaterialData> result = parseResource(materialCodec, "data/forgero/materials/diamond.json");

			assertTrue(result.result().isPresent());
			MaterialData diamond = result.result().get();

			AttributeData attackDamage = diamond.attributes().stream()
					.filter(a -> a.type().toString().equals("forgero:attack_damage"))
					.findFirst()
					.orElseThrow();

			assertTrue(attackDamage.condition().isPresent());
			assertEquals(2, attackDamage.condition().get().staticConditions().size());

			boolean hasOtherContributor = attackDamage.condition().get().staticConditions().stream()
					.anyMatch(c -> c instanceof HasOtherContributorCondition);
			assertTrue(hasOtherContributor);
		}
	}

	@Nested
	@DisplayName("Error Handling")
	class ErrorHandling {

		@Test
		@DisplayName("missing file returns IO exception")
		void missingFileReturnsIoException() {
			assertThrows(IOException.class, () ->
					parseResource(materialCodec, "data/forgero/materials/nonexistent.json")
			);
		}
	}
}
