package com.sigmundgranaas.forgero.data.pipeline.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.sigmundgranaas.forgero.common.identifier.api.IdentifierFactory;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.data.loading.api.RawDefinition;
import com.sigmundgranaas.forgero.data.loading.api.data.ExtensionData;
import com.sigmundgranaas.forgero.data.loading.api.data.ResourceData;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeDataImpl;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.ComputationData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.EquipmentTemplateData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.EquipmentTemplateSlotData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.EquipmentTemplateStructureData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.PartTemplateData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.PartTemplateStructureData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.PartTemplateStructureSlotData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.UpgradeSlotData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ExtensionMergerTest {

	private ExtensionMerger merger;
	private IdentifierFactory idFactory;

	@BeforeEach
	void setUp() {
		merger = new ExtensionMerger();
		idFactory = new IdentifierFactory.Builder().defaultNamespace("forgero").build();
	}

	private OpenIdentifier id(String id) {
		return idFactory.of(id);
	}

	@Nested
	class WhenNoExtensionsPresent {

		@Test
		void shouldReturnOriginalDefinitionsUnchanged() {
			ResourceData iron = new ResourceData(
					id("forgero:material"),
					"iron",
					null,
					List.of(id("forgero:metal")),
					null,
					null,
					null,
					null,
					null
			);

			Map<OpenIdentifier, RawDefinition> definitions = Map.of(
					id("forgero:materials/iron"), new RawDefinition(id("forgero:materials/iron"), iron)
			);

			Map<OpenIdentifier, RawDefinition> result = merger.merge(definitions);

			assertEquals(1, result.size());
			assertSame(iron, result.get(id("forgero:materials/iron")).data());
		}
	}

	@Nested
	class WhenMergingTags {

		@Test
		void shouldUnionTagsFromExtension() {
			ResourceData iron = new ResourceData(
					id("forgero:material"),
					"iron",
					null,
					List.of(id("forgero:metal")),
					null, null, null, null, null
			);

			ExtensionData extension = new ExtensionData(
					id("forgero:extension"),
					id("forgero:materials/iron"),
					0,
					List.of(id("forgero:tool_material"), id("forgero:armor_material")),
					null, null
			);

			Map<OpenIdentifier, RawDefinition> definitions = new HashMap<>();
			definitions.put(id("forgero:materials/iron"), new RawDefinition(id("forgero:materials/iron"), iron));
			definitions.put(id("forgero:extensions/iron-tools"), new RawDefinition(id("forgero:extensions/iron-tools"), extension));

			Map<OpenIdentifier, RawDefinition> result = merger.merge(definitions);

			assertEquals(1, result.size());
			assertFalse(result.containsKey(id("forgero:extensions/iron-tools")));

			ResourceData merged = (ResourceData) result.get(id("forgero:materials/iron")).data();
			assertNotNull(merged.tags());
			assertEquals(3, merged.tags().size());
			assertTrue(merged.tags().contains(id("forgero:metal")));
			assertTrue(merged.tags().contains(id("forgero:tool_material")));
			assertTrue(merged.tags().contains(id("forgero:armor_material")));
		}

		@Test
		void shouldNotDuplicateTags() {
			ResourceData iron = new ResourceData(
					id("forgero:material"),
					"iron",
					null,
					List.of(id("forgero:metal"), id("forgero:tool_material")),
					null, null, null, null, null
			);

			ExtensionData extension = new ExtensionData(
					id("forgero:extension"),
					id("forgero:materials/iron"),
					0,
					List.of(id("forgero:tool_material"), id("forgero:armor_material")),
					null, null
			);

			Map<OpenIdentifier, RawDefinition> definitions = new HashMap<>();
			definitions.put(id("forgero:materials/iron"), new RawDefinition(id("forgero:materials/iron"), iron));
			definitions.put(id("forgero:extensions/iron-ext"), new RawDefinition(id("forgero:extensions/iron-ext"), extension));

			Map<OpenIdentifier, RawDefinition> result = merger.merge(definitions);
			ResourceData merged = (ResourceData) result.get(id("forgero:materials/iron")).data();

			// Should have 3 unique tags, not 4
			assertEquals(3, merged.tags().size());
		}
	}

	@Nested
	class WhenMergingAttributes {

		@Test
		void shouldConcatenateAttributes() {
			AttributeData durability = new AttributeDataImpl(
					Optional.of(id("forgero:iron-durability")),
					id("forgero:durability"),
					new ComputationData(250f, null, null),
					Optional.empty(),
					Optional.empty()
			);

			ResourceData iron = new ResourceData(
					id("forgero:material"),
					"iron",
					null, null, null, null,
					List.of(durability),
					null, null
			);

			AttributeData miningSpeed = new AttributeDataImpl(
					Optional.of(id("forgero:iron-mining-speed")),
					id("forgero:mining_speed"),
					new ComputationData(6f, null, null),
					Optional.empty(),
					Optional.empty()
			);

			ExtensionData extension = new ExtensionData(
					id("forgero:extension"),
					id("forgero:materials/iron"),
					0,
					null,
					List.of(miningSpeed),
					null
			);

			Map<OpenIdentifier, RawDefinition> definitions = new HashMap<>();
			definitions.put(id("forgero:materials/iron"), new RawDefinition(id("forgero:materials/iron"), iron));
			definitions.put(id("forgero:extensions/iron-tools"), new RawDefinition(id("forgero:extensions/iron-tools"), extension));

			Map<OpenIdentifier, RawDefinition> result = merger.merge(definitions);
			ResourceData merged = (ResourceData) result.get(id("forgero:materials/iron")).data();

			assertNotNull(merged.attributes());
			assertEquals(2, merged.attributes().size());
			assertTrue(merged.attributes().stream().anyMatch(a -> a.id().equals(Optional.of(id("forgero:iron-durability")))));
			assertTrue(merged.attributes().stream().anyMatch(a -> a.id().equals(Optional.of(id("forgero:iron-mining-speed")))));
		}
	}

	@Nested
	class WhenMergingProperties {

		@Test
		void shouldMergeDistinctPropertyKeys() {
			JsonArray tooltips = new JsonArray();
			tooltips.add(new JsonPrimitive("Base tooltip"));
			Map<String, com.google.gson.JsonElement> baseProps = Map.of("forgero:tooltip", tooltips);

			ResourceData iron = new ResourceData(
					id("forgero:material"),
					"iron",
					null, null, null, null, null, null,
					baseProps
			);

			JsonArray features = new JsonArray();
			features.add(new JsonPrimitive("vein_mining"));
			Map<String, com.google.gson.JsonElement> extProps = Map.of("forgero:features", features);

			ExtensionData extension = new ExtensionData(
					id("forgero:extension"),
					id("forgero:materials/iron"),
					0,
					null, null,
					extProps
			);

			Map<OpenIdentifier, RawDefinition> definitions = new HashMap<>();
			definitions.put(id("forgero:materials/iron"), new RawDefinition(id("forgero:materials/iron"), iron));
			definitions.put(id("forgero:extensions/iron-ext"), new RawDefinition(id("forgero:extensions/iron-ext"), extension));

			Map<OpenIdentifier, RawDefinition> result = merger.merge(definitions);
			ResourceData merged = (ResourceData) result.get(id("forgero:materials/iron")).data();

			assertNotNull(merged.properties());
			assertEquals(2, merged.properties().size());
			assertTrue(merged.properties().containsKey("forgero:tooltip"));
			assertTrue(merged.properties().containsKey("forgero:features"));
		}

		@Test
		void shouldConcatenateArrayProperties() {
			JsonArray baseTooltips = new JsonArray();
			baseTooltips.add(new JsonPrimitive("Base tooltip"));

			ResourceData iron = new ResourceData(
					id("forgero:material"),
					"iron",
					null, null, null, null, null, null,
					Map.of("forgero:tooltip", baseTooltips)
			);

			JsonArray extTooltips = new JsonArray();
			extTooltips.add(new JsonPrimitive("Extension tooltip"));

			ExtensionData extension = new ExtensionData(
					id("forgero:extension"),
					id("forgero:materials/iron"),
					0,
					null, null,
					Map.of("forgero:tooltip", extTooltips)
			);

			Map<OpenIdentifier, RawDefinition> definitions = new HashMap<>();
			definitions.put(id("forgero:materials/iron"), new RawDefinition(id("forgero:materials/iron"), iron));
			definitions.put(id("forgero:extensions/iron-ext"), new RawDefinition(id("forgero:extensions/iron-ext"), extension));

			Map<OpenIdentifier, RawDefinition> result = merger.merge(definitions);
			ResourceData merged = (ResourceData) result.get(id("forgero:materials/iron")).data();

			JsonArray mergedTooltips = merged.properties().get("forgero:tooltip").getAsJsonArray();
			assertEquals(2, mergedTooltips.size());
		}

		@Test
		void shouldDeepMergeObjectProperties() {
			JsonObject baseConfig = new JsonObject();
			baseConfig.addProperty("debug", false);
			baseConfig.addProperty("version", "1.0");

			ResourceData iron = new ResourceData(
					id("forgero:material"),
					"iron",
					null, null, null, null, null, null,
					Map.of("config", baseConfig)
			);

			JsonObject extConfig = new JsonObject();
			extConfig.addProperty("debug", true);
			extConfig.addProperty("extra", "value");

			ExtensionData extension = new ExtensionData(
					id("forgero:extension"),
					id("forgero:materials/iron"),
					0,
					null, null,
					Map.of("config", extConfig)
			);

			Map<OpenIdentifier, RawDefinition> definitions = new HashMap<>();
			definitions.put(id("forgero:materials/iron"), new RawDefinition(id("forgero:materials/iron"), iron));
			definitions.put(id("forgero:extensions/iron-ext"), new RawDefinition(id("forgero:extensions/iron-ext"), extension));

			Map<OpenIdentifier, RawDefinition> result = merger.merge(definitions);
			ResourceData merged = (ResourceData) result.get(id("forgero:materials/iron")).data();

			JsonObject mergedConfig = merged.properties().get("config").getAsJsonObject();
			// Extension value overwrites base for primitives
			assertTrue(mergedConfig.get("debug").getAsBoolean());
			// Base value preserved
			assertEquals("1.0", mergedConfig.get("version").getAsString());
			// Extension value added
			assertEquals("value", mergedConfig.get("extra").getAsString());
		}
	}

	@Nested
	class WhenMultipleExtensions {

		@Test
		void shouldApplyInPriorityOrder() {
			ResourceData iron = new ResourceData(
					id("forgero:material"),
					"iron",
					null,
					List.of(id("forgero:base")),
					null, null, null, null, null
			);

			ExtensionData lowPriority = new ExtensionData(
					id("forgero:extension"),
					id("forgero:materials/iron"),
					0,
					List.of(id("forgero:first")),
					null, null
			);

			ExtensionData highPriority = new ExtensionData(
					id("forgero:extension"),
					id("forgero:materials/iron"),
					100,
					List.of(id("forgero:second")),
					null, null
			);

			Map<OpenIdentifier, RawDefinition> definitions = new HashMap<>();
			definitions.put(id("forgero:materials/iron"), new RawDefinition(id("forgero:materials/iron"), iron));
			definitions.put(id("forgero:extensions/iron-low"), new RawDefinition(id("forgero:extensions/iron-low"), lowPriority));
			definitions.put(id("forgero:extensions/iron-high"), new RawDefinition(id("forgero:extensions/iron-high"), highPriority));

			Map<OpenIdentifier, RawDefinition> result = merger.merge(definitions);
			ResourceData merged = (ResourceData) result.get(id("forgero:materials/iron")).data();

			// All tags should be present in order
			assertEquals(3, merged.tags().size());
			assertTrue(merged.tags().contains(id("forgero:base")));
			assertTrue(merged.tags().contains(id("forgero:first")));
			assertTrue(merged.tags().contains(id("forgero:second")));
		}
	}

	@Nested
	class WhenTargetNotFound {

		@Test
		void shouldIgnoreOrphanedExtensions() {
			ExtensionData extension = new ExtensionData(
					id("forgero:extension"),
					id("forgero:materials/nonexistent"),
					0,
					List.of(id("forgero:tag")),
					null, null
			);

			Map<OpenIdentifier, RawDefinition> definitions = new HashMap<>();
			definitions.put(id("forgero:extensions/orphan"), new RawDefinition(id("forgero:extensions/orphan"), extension));

			Map<OpenIdentifier, RawDefinition> result = merger.merge(definitions);

			assertTrue(result.isEmpty());
		}
	}

	@Nested
	class WhenMergingDifferentTypes {

		@Test
		void shouldMergeIntoShapeData() {
			ResourceData shape = new ResourceData(
					id("forgero:shape"),
					"pickaxe_head",
					null,
					List.of(id("forgero:tool_shape")),
					null, null, null, null, null
			);

			ExtensionData extension = new ExtensionData(
					id("forgero:extension"),
					id("forgero:shapes/pickaxe_head"),
					0,
					List.of(id("forgero:mining_shape")),
					null, null
			);

			Map<OpenIdentifier, RawDefinition> definitions = new HashMap<>();
			definitions.put(id("forgero:shapes/pickaxe_head"), new RawDefinition(id("forgero:shapes/pickaxe_head"), shape));
			definitions.put(id("forgero:extensions/pickaxe-ext"), new RawDefinition(id("forgero:extensions/pickaxe-ext"), extension));

			Map<OpenIdentifier, RawDefinition> result = merger.merge(definitions);
			ResourceData merged = (ResourceData) result.get(id("forgero:shapes/pickaxe_head")).data();

			assertEquals(2, merged.tags().size());
			assertTrue(merged.tags().contains(id("forgero:tool_shape")));
			assertTrue(merged.tags().contains(id("forgero:mining_shape")));
		}
	}

	@Nested
	class WhenExtensionAddsToNullFields {

		@Test
		void shouldCreateFieldsWhenBaseIsNull() {
			ResourceData iron = new ResourceData(
					id("forgero:material"),
					"iron",
					null, null, null, null, null, null, null
			);

			ExtensionData extension = new ExtensionData(
					id("forgero:extension"),
					id("forgero:materials/iron"),
					0,
					List.of(id("forgero:tool_material")),
					null, null
			);

			Map<OpenIdentifier, RawDefinition> definitions = new HashMap<>();
			definitions.put(id("forgero:materials/iron"), new RawDefinition(id("forgero:materials/iron"), iron));
			definitions.put(id("forgero:extensions/iron-ext"), new RawDefinition(id("forgero:extensions/iron-ext"), extension));

			Map<OpenIdentifier, RawDefinition> result = merger.merge(definitions);
			ResourceData merged = (ResourceData) result.get(id("forgero:materials/iron")).data();

			assertNotNull(merged.tags());
			assertEquals(1, merged.tags().size());
			assertTrue(merged.tags().contains(id("forgero:tool_material")));
		}
	}

	@Nested
	class WhenMergingTemplateExtensions {

		private EquipmentTemplateStructureData createMinimalStructure() {
			Map<String, EquipmentTemplateSlotData> slots = Map.of(
					"head", new EquipmentTemplateSlotData(id("forgero:pickaxe_head_type"), null, null)
			);
			return new EquipmentTemplateStructureData("forgero:{head.material.name}-pickaxe", slots);
		}

		private PartTemplateStructureData createMinimalPartStructure() {
			Map<String, PartTemplateStructureSlotData> slots = Map.of(
					"material", new PartTemplateStructureSlotData(id("forgero:tool_material"), 1, null)
			);
			return new PartTemplateStructureData("forgero:{material.name}-pickaxe_head", slots);
		}

		@Test
		void shouldMergeUpgradeSlotsIntoEquipmentTemplate() {
			UpgradeSlotData bindingSlot = new UpgradeSlotData(
					id("binding_slot"),
					id("forgero:binding_type"),
					null, null, null
			);

			EquipmentTemplateData pickaxe = new EquipmentTemplateData(
					id("forgero:equipment_template"),
					"pickaxe",
					null,
					List.of(id("forgero:pickaxe")),
					null,
					createMinimalStructure(),
					List.of(bindingSlot),
					null,
					null
			);

			UpgradeSlotData dyeSlot = new UpgradeSlotData(
					id("dye_slot"),
					id("forgero:dye_material"),
					List.of(id("forgero:dye")),
					null,
					"forgero.slot.dye"
			);

			ExtensionData extension = new ExtensionData(
					id("forgero:extension"),
					id("forgero:equipment/pickaxe"),
					100,
					null, null, null,
					List.of(dyeSlot)
			);

			Map<OpenIdentifier, RawDefinition> definitions = new HashMap<>();
			definitions.put(id("forgero:equipment/pickaxe"), new RawDefinition(id("forgero:equipment/pickaxe"), pickaxe));
			definitions.put(id("forgero:extensions/pickaxe-dye"), new RawDefinition(id("forgero:extensions/pickaxe-dye"), extension));

			Map<OpenIdentifier, RawDefinition> result = merger.merge(definitions);
			EquipmentTemplateData merged = (EquipmentTemplateData) result.get(id("forgero:equipment/pickaxe")).data();

			assertNotNull(merged.upgrades());
			assertEquals(2, merged.upgrades().size());
			assertTrue(merged.upgrades().stream().anyMatch(s -> s.id().equals(id("binding_slot"))));
			assertTrue(merged.upgrades().stream().anyMatch(s -> s.id().equals(id("dye_slot"))));
		}

		@Test
		void shouldMergeUpgradeSlotsIntoPartTemplate() {
			PartTemplateData headTemplate = new PartTemplateData(
					id("forgero:part_template"),
					"pickaxe_head",
					null,
					List.of(id("forgero:parts/pickaxe_head_type")),
					null,
					createMinimalPartStructure(),
					null, // no upgrades initially
					null,
					null,
					null
			);

			UpgradeSlotData gemSlot = new UpgradeSlotData(
					id("gem_slot"),
					id("forgero:gem_material"),
					List.of(id("forgero:gem")),
					null,
					"forgero.slot.gem"
			);

			ExtensionData extension = new ExtensionData(
					id("forgero:extension"),
					id("forgero:parts/pickaxe_head"),
					50,
					null, null, null,
					List.of(gemSlot)
			);

			Map<OpenIdentifier, RawDefinition> definitions = new HashMap<>();
			definitions.put(id("forgero:parts/pickaxe_head"), new RawDefinition(id("forgero:parts/pickaxe_head"), headTemplate));
			definitions.put(id("forgero:extensions/head-gem"), new RawDefinition(id("forgero:extensions/head-gem"), extension));

			Map<OpenIdentifier, RawDefinition> result = merger.merge(definitions);
			PartTemplateData merged = (PartTemplateData) result.get(id("forgero:parts/pickaxe_head")).data();

			assertNotNull(merged.upgrades());
			assertEquals(1, merged.upgrades().size());
			assertEquals(id("gem_slot"), merged.upgrades().get(0).id());
			assertEquals(id("forgero:gem_material"), merged.upgrades().get(0).type());
		}

		@Test
		void shouldOverrideDuplicateUpgradeSlotIds() {
			UpgradeSlotData originalSlot = new UpgradeSlotData(
					id("binding_slot"),
					id("forgero:binding_type"),
					null, 1, "original"
			);

			EquipmentTemplateData pickaxe = new EquipmentTemplateData(
					id("forgero:equipment_template"),
					"pickaxe",
					null, null, null,
					createMinimalStructure(),
					List.of(originalSlot),
					null, null
			);

			UpgradeSlotData overrideSlot = new UpgradeSlotData(
					id("binding_slot"), // Same ID
					id("forgero:special_binding"),
					List.of(id("forgero:special")),
					2,
					"override"
			);

			ExtensionData extension = new ExtensionData(
					id("forgero:extension"),
					id("forgero:equipment/pickaxe"),
					0,
					null, null, null,
					List.of(overrideSlot)
			);

			Map<OpenIdentifier, RawDefinition> definitions = new HashMap<>();
			definitions.put(id("forgero:equipment/pickaxe"), new RawDefinition(id("forgero:equipment/pickaxe"), pickaxe));
			definitions.put(id("forgero:extensions/pickaxe-binding"), new RawDefinition(id("forgero:extensions/pickaxe-binding"), extension));

			Map<OpenIdentifier, RawDefinition> result = merger.merge(definitions);
			EquipmentTemplateData merged = (EquipmentTemplateData) result.get(id("forgero:equipment/pickaxe")).data();

			// Should have only 1 slot (extension wins)
			assertEquals(1, merged.upgrades().size());
			UpgradeSlotData resultSlot = merged.upgrades().get(0);
			assertEquals(id("binding_slot"), resultSlot.id());
			assertEquals(id("forgero:special_binding"), resultSlot.type());
			assertEquals("override", resultSlot.description());
		}

		@Test
		void shouldMergeTagsIntoTemplate() {
			EquipmentTemplateData pickaxe = new EquipmentTemplateData(
					id("forgero:equipment_template"),
					"pickaxe",
					null,
					List.of(id("forgero:pickaxe")),
					null,
					createMinimalStructure(),
					null, null, null
			);

			ExtensionData extension = new ExtensionData(
					id("forgero:extension"),
					id("forgero:equipment/pickaxe"),
					0,
					List.of(id("forgero:mining_tool"), id("forgero:dyeable")),
					null, null, null
			);

			Map<OpenIdentifier, RawDefinition> definitions = new HashMap<>();
			definitions.put(id("forgero:equipment/pickaxe"), new RawDefinition(id("forgero:equipment/pickaxe"), pickaxe));
			definitions.put(id("forgero:extensions/pickaxe-tags"), new RawDefinition(id("forgero:extensions/pickaxe-tags"), extension));

			Map<OpenIdentifier, RawDefinition> result = merger.merge(definitions);
			EquipmentTemplateData merged = (EquipmentTemplateData) result.get(id("forgero:equipment/pickaxe")).data();

			assertNotNull(merged.tags());
			assertEquals(3, merged.tags().size());
			assertTrue(merged.tags().contains(id("forgero:pickaxe")));
			assertTrue(merged.tags().contains(id("forgero:mining_tool")));
			assertTrue(merged.tags().contains(id("forgero:dyeable")));
		}

		@Test
		void shouldMergePropertiesIntoTemplate() {
			JsonObject baseTooltip = new JsonObject();
			baseTooltip.addProperty("text", "Base");

			EquipmentTemplateData pickaxe = new EquipmentTemplateData(
					id("forgero:equipment_template"),
					"pickaxe",
					null, null, null,
					createMinimalStructure(),
					null, null,
					Map.of("forgero:tooltip", baseTooltip)
			);

			JsonObject extTooltip = new JsonObject();
			extTooltip.addProperty("color", "gold");

			ExtensionData extension = new ExtensionData(
					id("forgero:extension"),
					id("forgero:equipment/pickaxe"),
					0,
					null, null,
					Map.of("forgero:tooltip", extTooltip),
					null
			);

			Map<OpenIdentifier, RawDefinition> definitions = new HashMap<>();
			definitions.put(id("forgero:equipment/pickaxe"), new RawDefinition(id("forgero:equipment/pickaxe"), pickaxe));
			definitions.put(id("forgero:extensions/pickaxe-props"), new RawDefinition(id("forgero:extensions/pickaxe-props"), extension));

			Map<OpenIdentifier, RawDefinition> result = merger.merge(definitions);
			EquipmentTemplateData merged = (EquipmentTemplateData) result.get(id("forgero:equipment/pickaxe")).data();

			assertNotNull(merged.properties());
			JsonObject mergedTooltip = merged.properties().get("forgero:tooltip").getAsJsonObject();
			assertEquals("Base", mergedTooltip.get("text").getAsString());
			assertEquals("gold", mergedTooltip.get("color").getAsString());
		}
	}

	@Nested
	class WhenMergingUpgradesIntoResources {

		@Test
		void shouldMergeUpgradeSlotsIntoStaticPart() {
			UpgradeSlotData existingSlot = new UpgradeSlotData(
					id("gem_slot"),
					id("forgero:gem"),
					null, null, null
			);

			ResourceData staticPart = new ResourceData(
					id("forgero:static_part"),
					"wooden_handle",
					null,
					List.of(id("forgero:handle_type")),
					null, null, null, null, null,
					List.of(existingSlot),
					null
			);

			UpgradeSlotData newSlot = new UpgradeSlotData(
					id("wrap_slot"),
					id("forgero:wrap_material"),
					null, null, null
			);

			ExtensionData extension = new ExtensionData(
					id("forgero:extension"),
					id("forgero:parts/wooden_handle"),
					0,
					null, null, null,
					List.of(newSlot)
			);

			Map<OpenIdentifier, RawDefinition> definitions = new HashMap<>();
			definitions.put(id("forgero:parts/wooden_handle"), new RawDefinition(id("forgero:parts/wooden_handle"), staticPart));
			definitions.put(id("forgero:extensions/handle-wrap"), new RawDefinition(id("forgero:extensions/handle-wrap"), extension));

			Map<OpenIdentifier, RawDefinition> result = merger.merge(definitions);
			ResourceData merged = (ResourceData) result.get(id("forgero:parts/wooden_handle")).data();

			assertNotNull(merged.upgrades());
			assertEquals(2, merged.upgrades().size());
			assertTrue(merged.upgrades().stream().anyMatch(s -> s.id().equals(id("gem_slot"))));
			assertTrue(merged.upgrades().stream().anyMatch(s -> s.id().equals(id("wrap_slot"))));
		}
	}
}
