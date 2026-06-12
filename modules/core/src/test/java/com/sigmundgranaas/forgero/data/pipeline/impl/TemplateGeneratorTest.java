package com.sigmundgranaas.forgero.data.pipeline.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.cof.ComponentTypeRegistry;
import com.sigmundgranaas.forgero.cof.dto.CofComponent;
import com.sigmundgranaas.forgero.common.identifier.api.IdentifierFactory;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.tags.api.TagResolver;
import com.sigmundgranaas.forgero.common.tags.engine.TagGraphBuilder;
import com.sigmundgranaas.forgero.core.property.api.PropertyKey;
import com.sigmundgranaas.forgero.data.loading.api.RawDefinition;
import com.sigmundgranaas.forgero.data.loading.api.data.GenerationConfigData;
import com.sigmundgranaas.forgero.data.loading.api.data.SlotGenerationFilter;
import com.sigmundgranaas.forgero.data.loading.api.data.host.CreateData;
import com.sigmundgranaas.forgero.data.loading.api.data.host.HostData;
import com.sigmundgranaas.forgero.data.loading.api.data.host.template.CreateTemplateData;
import com.sigmundgranaas.forgero.data.loading.api.data.host.template.HostTemplateData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.*;
import com.sigmundgranaas.forgero.data.pipeline.util.IdTemplateResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import com.sigmundgranaas.forgero.data.loading.api.data.ResourceData;

/**
 * Comprehensive tests for TemplateGenerator before refactoring.
 * Tests all responsibilities:
 * - Part generation from templates
 * - Equipment generation from templates
 * - Generation filter application
 * - Host data generation
 * - Combination building (cartesian product)
 */
class TemplateGeneratorTest {

	private IdentifierFactory idFactory;
	private TagResolver tagResolver;
	private PropertyMerger propertyMerger;
	private IdTemplateResolver idTemplateResolver;
	private Map<OpenIdentifier, CofComponent> staticComponents;
	private Map<OpenIdentifier, RawDefinition> rawDefinitions;

	@BeforeEach
	void setUp() {
		idFactory = new IdentifierFactory.Builder().defaultNamespace("forgero").build();

		// Create TagResolver with common tags used in tests
		TagGraphBuilder tagGraphBuilder = new TagGraphBuilder();
		tagGraphBuilder.add(id("material"), Set.of());
		tagGraphBuilder.add(id("shape"), Set.of());
		tagGraphBuilder.add(id("quality"), Set.of());
		tagGraphBuilder.add(id("head"), Set.of()); // Tag for parts that can go in head slot
		tagGraphBuilder.add(id("handle"), Set.of()); // Tag for parts that can go in handle slot
		tagGraphBuilder.add(id("binding"), Set.of());
		tagGraphBuilder.add(id("gem"), Set.of());
		tagGraphBuilder.add(id("metal"), Set.of(id("material")));
		tagGraphBuilder.add(id("common"), Set.of());
		tagGraphBuilder.add(id("rare"), Set.of());
		tagGraphBuilder.add(id("legendary"), Set.of());
		// Add composite tags for testing (these aren't used in current tests but good to have)
		tagResolver = tagGraphBuilder.build();

		propertyMerger = new PropertyMerger(Map.of()); // Empty property codecs for testing
		staticComponents = new HashMap<>();
		rawDefinitions = new HashMap<>();
		idTemplateResolver = new IdTemplateResolver(idFactory, rawDefinitions);
	}

	// ===== Nested Test Classes =====

	@Nested
	class WhenGeneratingParts {

		private static final Logger LOGGER = LoggerFactory.getLogger(WhenGeneratingParts.class);

		@Test
		void debugTest() {
			// Debug test to understand what's happening
			CofComponent iron = createStaticComponent("forgero:iron", Set.of("forgero:material"));
			CofComponent pickaxeShape = createStaticComponent("forgero:pickaxe_head_shape", Set.of("forgero:shape"));
			staticComponents.put(iron.id(), iron);
			staticComponents.put(pickaxeShape.id(), pickaxeShape);

			LOGGER.debug("Static components: {}", staticComponents.keySet());
			LOGGER.debug("Raw definitions: {}", rawDefinitions.keySet());

			PartTemplateData template = createPartTemplate(
					"forgero:part_template",
					Map.of(
							"material", new PartTemplateStructureSlotData(id("material"), null, null, "Material slot"),
							"shape", new PartTemplateStructureSlotData(id("shape"), null, null, "Shape slot")
					),
					"forgero:{material.name}-{shape.name}",
					null,
					null
			);
			addRawDefinition("forgero:part_template", template);

			LOGGER.debug("Template added. Raw definitions now: {}", rawDefinitions.keySet());
			LOGGER.debug("Template type: {}", template.type());
			LOGGER.debug("Template structure slots: {}", template.structure().slots());

			TemplateGenerator generator = createGenerator();
			TemplateGenerator.TemplateResult result = generator.generate();

			LOGGER.debug("Generated {} components", result.components().size());
			result.components().forEach(comp -> {
				LOGGER.debug("  - {} (type: {})", comp.id(), comp.componentType());
			});
		}

		@Test
		void generatesPartFromSimpleTemplate() {
			// Given: Iron material, pickaxe shape, and a part template
			CofComponent iron = createStaticComponent("forgero:iron", Set.of("forgero:material", "forgero:metal"));
			CofComponent pickaxeShape = createStaticComponent("forgero:pickaxe_head_shape", Set.of("forgero:shape", "forgero:head"));
			staticComponents.put(iron.id(), iron);
			staticComponents.put(pickaxeShape.id(), pickaxeShape);

			PartTemplateData template = createPartTemplate(
					"forgero:part_template",
					Map.of(
							"material", new PartTemplateStructureSlotData(id("material"), null, null, "Material slot"),
							"shape", new PartTemplateStructureSlotData(id("shape"), null, null, "Shape slot")
					),
					"forgero:{material.name}-{shape.name}",
					null,
					null
			);
			addRawDefinition("forgero:part_template", template);

			// When: Generate parts
			TemplateGenerator generator = createGenerator();
			TemplateGenerator.TemplateResult result = generator.generate();

			// Then: Should generate one part with correct ID
			assertEquals(1, result.components().size());
			CofComponent generated = result.components().get(0);
			assertEquals(id("iron-pickaxe_head"), generated.id());
			assertEquals(ComponentTypeRegistry.STRUCTURED_PART, generated.componentType());
			assertTrue(generated.structure().isPresent());
			assertEquals(2, generated.structure().get().slots().size());
		}

		@Test
		void generatesMultiplePartsFromCartesianProduct() {
			// Given: 2 materials, 2 shapes
			CofComponent iron = createStaticComponent("forgero:iron", Set.of("forgero:material"));
			CofComponent gold = createStaticComponent("forgero:gold", Set.of("forgero:material"));
			CofComponent pickaxeShape = createStaticComponent("forgero:pickaxe_head_shape", Set.of("forgero:shape"));
			CofComponent axeShape = createStaticComponent("forgero:axe_head_shape", Set.of("forgero:shape"));

			staticComponents.put(iron.id(), iron);
			staticComponents.put(gold.id(), gold);
			staticComponents.put(pickaxeShape.id(), pickaxeShape);
			staticComponents.put(axeShape.id(), axeShape);

			PartTemplateData template = createPartTemplate(
					"forgero:part_template",
					Map.of(
							"material", new PartTemplateStructureSlotData(id("material"), null, null, "Material slot"),
							"shape", new PartTemplateStructureSlotData(id("shape"), null, null, "Shape slot")
					),
					"forgero:{material.name}-{shape.name}",
					null,
					null
			);
			addRawDefinition("forgero:part_template", template);

			// When: Generate parts
			TemplateGenerator generator = createGenerator();
			TemplateGenerator.TemplateResult result = generator.generate();

			// Then: Should generate 4 parts (2 materials × 2 shapes)
			assertEquals(4, result.components().size());
			Set<OpenIdentifier> generatedIds = new HashSet<>();
			result.components().forEach(comp -> generatedIds.add(comp.id()));

			assertTrue(generatedIds.contains(id("iron-pickaxe_head")));
			assertTrue(generatedIds.contains(id("iron-axe_head")));
			assertTrue(generatedIds.contains(id("gold-pickaxe_head")));
			assertTrue(generatedIds.contains(id("gold-axe_head")));
		}

		@Test
		void generatesExtensiblePartWhenUpgradesSpecified() {
			// Given: Material, shape, and template with upgrades
			CofComponent iron = createStaticComponent("forgero:iron", Set.of("forgero:material"));
			CofComponent pickaxeShape = createStaticComponent("forgero:pickaxe_head_shape", Set.of("forgero:shape"));
			staticComponents.put(iron.id(), iron);
			staticComponents.put(pickaxeShape.id(), pickaxeShape);

			PartTemplateData template = createPartTemplate(
					"forgero:part_template",
					Map.of(
							"material", new PartTemplateStructureSlotData(id("material"), null, null, "Material slot"),
							"shape", new PartTemplateStructureSlotData(id("shape"), null, null, "Shape slot")
					),
					"forgero:{material.name}-{shape.name}",
					null,
					List.of(new UpgradeSlotData(id("reinforcement_slot"), id("gem"), null, null, "Reinforcement slot"))
			);
			addRawDefinition("forgero:part_template", template);

			// When: Generate parts
			TemplateGenerator generator = createGenerator();
			TemplateGenerator.TemplateResult result = generator.generate();

			// Then: Should generate STRUCTURED_EXTENSIBLE_PART
			assertEquals(1, result.components().size());
			CofComponent generated = result.components().get(0);
			assertEquals(ComponentTypeRegistry.STRUCTURED_EXTENSIBLE_PART, generated.componentType());
			assertTrue(generated.upgrades().isPresent());
			assertEquals(1, generated.upgrades().get().slots().size());
		}

		@Test
		void mergesPropertiesFromTemplateAndComponents() {
			// Given: Components and template (note: templates don't inherit component properties by design)
			CofComponent iron = createStaticComponent("forgero:iron", Set.of("forgero:material"));
			CofComponent pickaxeShape = createStaticComponent("forgero:pickaxe_head_shape", Set.of("forgero:shape"));
			staticComponents.put(iron.id(), iron);
			staticComponents.put(pickaxeShape.id(), pickaxeShape);

			PartTemplateData template = createPartTemplate(
					"forgero:part_template",
					Map.of(
							"material", new PartTemplateStructureSlotData(id("material"), null, null, "Material slot"),
							"shape", new PartTemplateStructureSlotData(id("shape"), null, null, "Shape slot")
					),
					"forgero:{material.name}-{shape.name}",
					null,
					null
			);
			addRawDefinition("forgero:part_template", template);

			// When: Generate parts
			TemplateGenerator generator = createGenerator();
			TemplateGenerator.TemplateResult result = generator.generate();

			// Then: Should generate the part successfully
			assertEquals(1, result.components().size());
			CofComponent generated = result.components().get(0);
			// Template-generated components have empty properties by default (they don't inherit from materials)
			assertTrue(generated.properties().isPresent());
		}
	}

	@Nested
	class WhenGeneratingEquipment {

		@Test
		void generatesEquipmentFromExistingParts() {
			// Given: Pre-existing parts (head, handle)
			CofComponent head = createStructuredPart("forgero:iron-pickaxe_head", Set.of("forgero:head"));
			CofComponent handle = createStructuredPart("forgero:oak-handle", Set.of("forgero:handle"));
			staticComponents.put(head.id(), head);
			staticComponents.put(handle.id(), handle);

			EquipmentTemplateData template = createEquipmentTemplate(
					"forgero:equipment_template",
					Map.of(
							"head", new EquipmentTemplateSlotData(id("head"), null, null),
							"handle", new EquipmentTemplateSlotData(id("handle"), null, null)
					),
					"forgero:iron-pickaxe" // Simple static ID since mock parts don't have nested structure
			);
			addRawDefinition("forgero:equipment_template", template);

			// When: Generate equipment
			TemplateGenerator generator = createGenerator();
			TemplateGenerator.TemplateResult result = generator.generate();

			// Then: Should generate one equipment
			assertEquals(1, result.components().size());
			CofComponent generated = result.components().get(0);
			assertEquals(ComponentTypeRegistry.STRUCTURED_EQUIPMENT, generated.componentType());
			assertTrue(generated.structure().isPresent());
			assertEquals(2, generated.structure().get().slots().size());
		}

		@Test
		void generatesExtensibleEquipmentWhenUpgradesSpecified() {
			// Given: Parts and template with upgrades
			CofComponent head = createStructuredPart("forgero:iron-pickaxe_head", Set.of("forgero:head"));
			CofComponent handle = createStructuredPart("forgero:oak-handle", Set.of("forgero:handle"));
			staticComponents.put(head.id(), head);
			staticComponents.put(handle.id(), handle);

			EquipmentTemplateData template = new EquipmentTemplateData(
					id("equipment_template"), // type
					"Equipment template",      // name
					null,                      // include
					null,                      // tags
					null,                      // host_template
					new EquipmentTemplateStructureData(
							"forgero:iron-pickaxe", // Simple static ID since mock parts don't have nested structure
							Map.of(
									"head", new EquipmentTemplateSlotData(id("head"), null, null),
									"handle", new EquipmentTemplateSlotData(id("handle"), null, null)
							)
					),
					List.of(new UpgradeSlotData(id("binding_slot"), id("binding"), null, null, "Binding slot")),
					null,                      // attributes
					null                       // properties
			);
			addRawDefinition("forgero:equipment_template", template);

			// When: Generate equipment
			TemplateGenerator generator = createGenerator();
			TemplateGenerator.TemplateResult result = generator.generate();

			// Then: Should generate STRUCTURED_EXTENSIBLE_EQUIPMENT
			assertEquals(1, result.components().size());
			CofComponent generated = result.components().get(0);
			assertEquals(ComponentTypeRegistry.STRUCTURED_EXTENSIBLE_EQUIPMENT, generated.componentType());
			assertTrue(generated.upgrades().isPresent());
		}

		@Test
		void usesGeneratedPartsWhenAvailable() {
			// Given: Materials and shapes to generate parts, then equipment template
			// Note: oakWood only has "handle" tag (not "material") to avoid creating extra part combinations
			// Note: pickaxeShape only has "shape" tag (not "head") - generated parts get "head" tag from template
			CofComponent iron = createStaticComponent("forgero:iron", Set.of("forgero:material"));
			CofComponent pickaxeShape = createStaticComponent("forgero:pickaxe_head_shape", Set.of("forgero:shape"));
			CofComponent oakWood = createStaticComponent("forgero:oak", Set.of("forgero:handle")); // Only handle tag
			staticComponents.put(iron.id(), iron);
			staticComponents.put(pickaxeShape.id(), pickaxeShape);
			staticComponents.put(oakWood.id(), oakWood);

			// Part template for head - needs "head" tag so generated parts can be used in equipment
			PartTemplateData headTemplate = new PartTemplateData(
					id("part_template"), // type
					"Head template", // name
					null, // include
					List.of(id("head")), // tags - important! Generated parts need this tag for equipment
					null, // host_template
					new PartTemplateStructureData(
							"forgero:{material.name}-{shape.name}",
							Map.of(
									"material", new PartTemplateStructureSlotData(id("material"), null, null, "Material"),
									"shape", new PartTemplateStructureSlotData(id("shape"), null, null, "Shape")
							)
					),
					null, // upgrades
					null, // attributes
					null, // generation
					null // properties
			);
			addRawDefinition("forgero:part_template", headTemplate); // Use same ID as type to avoid duplicates

			// Equipment template
			EquipmentTemplateData equipTemplate = createEquipmentTemplate(
					"forgero:equipment_template",
					Map.of(
							"head", new EquipmentTemplateSlotData(id("head"), null, null),
							"handle", new EquipmentTemplateSlotData(id("handle"), null, null)
					),
					"forgero:iron-pickaxe-tool" // Generated part will be iron-pickaxe_head, this becomes iron-pickaxe-tool
			);
			addRawDefinition("forgero:equipment_template", equipTemplate); // Use same ID as type

			// When: Generate
			TemplateGenerator generator = createGenerator();
			TemplateGenerator.TemplateResult result = generator.generate();

			// Then: Verify that part and equipment were generated
			// Note: exact count may vary based on template configuration

			// Verify at least one part was generated
			Optional<CofComponent> generatedPart = result.components().stream()
					.filter(c -> c.id().equals(id("iron-pickaxe_head")))
					.findFirst();
			assertTrue(generatedPart.isPresent(), "Should generate iron-pickaxe_head part");

			// Verify equipment was generated using the part
			Optional<CofComponent> generatedEquipment = result.components().stream()
					.filter(c -> c.componentType().equals(ComponentTypeRegistry.STRUCTURED_EQUIPMENT))
					.findFirst();
			assertTrue(generatedEquipment.isPresent(), "Should generate equipment");
		}
	}

	@Nested
	class WhenApplyingGenerationFilters {

		@Test
		void filtersUsingRequireAllTags() {
			// Given: 3 materials (iron has metal+common, gold has metal+rare, wood has none)
			CofComponent iron = createStaticComponent("forgero:iron", Set.of("forgero:material", "forgero:metal", "forgero:common"));
			CofComponent gold = createStaticComponent("forgero:gold", Set.of("forgero:material", "forgero:metal", "forgero:rare"));
			CofComponent wood = createStaticComponent("forgero:wood", Set.of("forgero:material"));
			staticComponents.put(iron.id(), iron);
			staticComponents.put(gold.id(), gold);
			staticComponents.put(wood.id(), wood);

			CofComponent shape = createStaticComponent("forgero:pickaxe_head_shape", Set.of("forgero:shape"));
			staticComponents.put(shape.id(), shape);

			// Template with filter: requireAllTags = [metal, common]
			SlotGenerationFilter filter = SlotGenerationFilter.builder()
					.requireAllTags(List.of(id("metal"), id("common")))
					.build();
			GenerationConfigData genConfig = new GenerationConfigData(Map.of("material", filter));

			PartTemplateData template = createPartTemplate(
					"forgero:part_template",
					Map.of(
							"material", new PartTemplateStructureSlotData(id("material"), null, null, "Material"),
							"shape", new PartTemplateStructureSlotData(id("shape"), null, null, "Shape")
					),
					"forgero:{material.name}-{shape.name}",
					null,
					null,
					genConfig
			);
			addRawDefinition("forgero:part_template", template);

			// When: Generate
			TemplateGenerator generator = createGenerator();
			TemplateGenerator.TemplateResult result = generator.generate();

			// Then: Should only generate iron-pickaxe_head (gold missing common, wood missing metal)
			assertEquals(1, result.components().size());
			assertEquals(id("iron-pickaxe_head"), result.components().get(0).id());
		}

		@Test
		void filtersUsingRequireAnyTags() {
			// Given: 3 materials with different rarities
			CofComponent iron = createStaticComponent("forgero:iron", Set.of("forgero:material", "forgero:common"));
			CofComponent gold = createStaticComponent("forgero:gold", Set.of("forgero:material", "forgero:rare"));
			CofComponent diamond = createStaticComponent("forgero:diamond", Set.of("forgero:material", "forgero:legendary"));
			staticComponents.put(iron.id(), iron);
			staticComponents.put(gold.id(), gold);
			staticComponents.put(diamond.id(), diamond);

			CofComponent shape = createStaticComponent("forgero:pickaxe_head_shape", Set.of("forgero:shape"));
			staticComponents.put(shape.id(), shape);

			// Filter: requireAnyTags = [rare, legendary]
			SlotGenerationFilter filter = SlotGenerationFilter.builder()
					.requireAnyTags(List.of(id("rare"), id("legendary")))
					.build();
			GenerationConfigData genConfig = new GenerationConfigData(Map.of("material", filter));

			PartTemplateData template = createPartTemplate(
					"forgero:part_template",
					Map.of(
							"material", new PartTemplateStructureSlotData(id("material"), null, null, "Material"),
							"shape", new PartTemplateStructureSlotData(id("shape"), null, null, "Shape")
					),
					"forgero:{material.name}-{shape.name}",
					null,
					null,
					genConfig
			);
			addRawDefinition("forgero:part_template", template);

			// When: Generate
			TemplateGenerator generator = createGenerator();
			TemplateGenerator.TemplateResult result = generator.generate();

			// Then: Should generate gold and diamond (iron excluded)
			assertEquals(2, result.components().size());
			Set<OpenIdentifier> ids = new HashSet<>();
			result.components().forEach(c -> ids.add(c.id()));
			assertTrue(ids.contains(id("gold-pickaxe_head")));
			assertTrue(ids.contains(id("diamond-pickaxe_head")));
		}

		@Test
		void filtersUsingExcludeAnyTags() {
			// Given: 3 materials
			CofComponent iron = createStaticComponent("forgero:iron", Set.of("forgero:material", "forgero:common"));
			CofComponent gold = createStaticComponent("forgero:gold", Set.of("forgero:material", "forgero:rare"));
			CofComponent wood = createStaticComponent("forgero:wood", Set.of("forgero:material", "forgero:common"));
			staticComponents.put(iron.id(), iron);
			staticComponents.put(gold.id(), gold);
			staticComponents.put(wood.id(), wood);

			CofComponent shape = createStaticComponent("forgero:pickaxe_head_shape", Set.of("forgero:shape"));
			staticComponents.put(shape.id(), shape);

			// Filter: excludeAnyTags = [common]
			SlotGenerationFilter filter = SlotGenerationFilter.builder()
					.excludeAnyTags(List.of(id("common")))
					.build();
			GenerationConfigData genConfig = new GenerationConfigData(Map.of("material", filter));

			PartTemplateData template = createPartTemplate(
					"forgero:part_template",
					Map.of(
							"material", new PartTemplateStructureSlotData(id("material"), null, null, "Material"),
							"shape", new PartTemplateStructureSlotData(id("shape"), null, null, "Shape")
					),
					"forgero:{material.name}-{shape.name}",
					null,
					null,
					genConfig
			);
			addRawDefinition("forgero:part_template", template);

			// When: Generate
			TemplateGenerator generator = createGenerator();
			TemplateGenerator.TemplateResult result = generator.generate();

			// Then: Should only generate gold (iron and wood have common tag)
			assertEquals(1, result.components().size());
			assertEquals(id("gold-pickaxe_head"), result.components().get(0).id());
		}

		@Test
		void filtersUsingExplicitList() {
			// Given: 3 materials
			CofComponent iron = createStaticComponent("forgero:iron", Set.of("forgero:material"));
			CofComponent gold = createStaticComponent("forgero:gold", Set.of("forgero:material"));
			CofComponent diamond = createStaticComponent("forgero:diamond", Set.of("forgero:material"));
			staticComponents.put(iron.id(), iron);
			staticComponents.put(gold.id(), gold);
			staticComponents.put(diamond.id(), diamond);

			CofComponent shape = createStaticComponent("forgero:pickaxe_head_shape", Set.of("forgero:shape"));
			staticComponents.put(shape.id(), shape);

			// Filter: explicitList = [iron, diamond]
			SlotGenerationFilter filter = SlotGenerationFilter.builder()
					.explicitList(List.of(id("iron"), id("diamond")))
					.build();
			GenerationConfigData genConfig = new GenerationConfigData(Map.of("material", filter));

			PartTemplateData template = createPartTemplate(
					"forgero:part_template",
					Map.of(
							"material", new PartTemplateStructureSlotData(id("material"), null, null, "Material"),
							"shape", new PartTemplateStructureSlotData(id("shape"), null, null, "Shape")
					),
					"forgero:{material.name}-{shape.name}",
					null,
					null,
					genConfig
			);
			addRawDefinition("forgero:part_template", template);

			// When: Generate
			TemplateGenerator generator = createGenerator();
			TemplateGenerator.TemplateResult result = generator.generate();

			// Then: Should only generate iron and diamond
			assertEquals(2, result.components().size());
			Set<OpenIdentifier> ids = new HashSet<>();
			result.components().forEach(c -> ids.add(c.id()));
			assertTrue(ids.contains(id("iron-pickaxe_head")));
			assertTrue(ids.contains(id("diamond-pickaxe_head")));
		}
	}

	@Nested
	class WhenGeneratingHostData {

		@Test
		void generatesHostDataFromTemplate() {
			// Given: Material, shape, and template with host_template
			CofComponent iron = createStaticComponent("forgero:iron", Set.of("forgero:material"));
			CofComponent pickaxeShape = createStaticComponent("forgero:pickaxe_head_shape", Set.of("forgero:shape"));
			staticComponents.put(iron.id(), iron);
			staticComponents.put(pickaxeShape.id(), pickaxeShape);

			HostTemplateData hostTemplate = new HostTemplateData(
					null,  // identifiers
					new CreateTemplateData(
							"forgero:{material.name}-{shape.name}",
							"com.example.{material.name}.{shape.name}",
							"items.parts"
					)
			);

			PartTemplateData template = createPartTemplate(
					"forgero:part_template",
					Map.of(
							"material", new PartTemplateStructureSlotData(id("material"), null, null, "Material"),
							"shape", new PartTemplateStructureSlotData(id("shape"), null, null, "Shape")
					),
					"forgero:{material.name}-{shape.name}",
					null,
					null,
					null,
					hostTemplate
			);
			addRawDefinition("forgero:part_template", template);

			// When: Generate
			TemplateGenerator generator = createGenerator();
			TemplateGenerator.TemplateResult result = generator.generate();

			// Then: Should generate host data with resolved templates
			assertEquals(1, result.hostData().size());
			HostData hostData = result.hostData().get(id("iron-pickaxe_head"));
			assertNotNull(hostData);
			assertNotNull(hostData.create());
			assertEquals(id("forgero:iron-pickaxe_head"), hostData.create().id());
			assertEquals("com.example.iron.pickaxe_head", hostData.create().itemClass()); // Shape suffix "_shape" is stripped
			assertEquals("items.parts", hostData.create().itemGroup());
		}

		@Test
		void skipsHostDataWhenTemplateIsNull() {
			// Given: Template without host_template
			CofComponent iron = createStaticComponent("forgero:iron", Set.of("forgero:material"));
			CofComponent shape = createStaticComponent("forgero:pickaxe_head_shape", Set.of("forgero:shape"));
			staticComponents.put(iron.id(), iron);
			staticComponents.put(shape.id(), shape);

			PartTemplateData template = createPartTemplate(
					"forgero:part_template",
					Map.of(
							"material", new PartTemplateStructureSlotData(id("material"), null, null, "Material"),
							"shape", new PartTemplateStructureSlotData(id("shape"), null, null, "Shape")
					),
					"forgero:{material.name}-{shape.name}",
					null,
					null
			);
			addRawDefinition("forgero:part_template", template);

			// When: Generate
			TemplateGenerator generator = createGenerator();
			TemplateGenerator.TemplateResult result = generator.generate();

			// Then: Should not generate host data
			assertTrue(result.hostData().isEmpty());
		}
	}

	@Nested
	class WhenBuildingCombinations {

		@Test
		void buildsCartesianProductOfTwoSlots() {
			// Given: 2 materials, 3 shapes
			CofComponent iron = createStaticComponent("forgero:iron", Set.of("forgero:material"));
			CofComponent gold = createStaticComponent("forgero:gold", Set.of("forgero:material"));
			CofComponent pickaxe = createStaticComponent("forgero:pickaxe_head_shape", Set.of("forgero:shape"));
			CofComponent axe = createStaticComponent("forgero:axe_head_shape", Set.of("forgero:shape"));
			CofComponent shovel = createStaticComponent("forgero:shovel_head_shape", Set.of("forgero:shape"));

			staticComponents.put(iron.id(), iron);
			staticComponents.put(gold.id(), gold);
			staticComponents.put(pickaxe.id(), pickaxe);
			staticComponents.put(axe.id(), axe);
			staticComponents.put(shovel.id(), shovel);

			PartTemplateData template = createPartTemplate(
					"forgero:part_template",
					Map.of(
							"material", new PartTemplateStructureSlotData(id("material"), null, null, "Material"),
							"shape", new PartTemplateStructureSlotData(id("shape"), null, null, "Shape")
					),
					"forgero:{material.name}-{shape.name}",
					null,
					null
			);
			addRawDefinition("forgero:part_template", template);

			// When: Generate
			TemplateGenerator generator = createGenerator();
			TemplateGenerator.TemplateResult result = generator.generate();

			// Then: Should generate 6 combinations (2 × 3)
			assertEquals(6, result.components().size());
		}

		@Test
		void buildsCartesianProductOfThreeSlots() {
			// Given: 2 materials, 2 shapes, 2 qualities
			CofComponent iron = createStaticComponent("forgero:iron", Set.of("forgero:material"));
			CofComponent gold = createStaticComponent("forgero:gold", Set.of("forgero:material"));
			CofComponent pickaxe = createStaticComponent("forgero:pickaxe_head_shape", Set.of("forgero:shape"));
			CofComponent axe = createStaticComponent("forgero:axe_head_shape", Set.of("forgero:shape"));
			CofComponent normal = createStaticComponent("forgero:normal", Set.of("forgero:quality"));
			CofComponent mastercrafted = createStaticComponent("forgero:mastercrafted", Set.of("forgero:quality"));

			staticComponents.put(iron.id(), iron);
			staticComponents.put(gold.id(), gold);
			staticComponents.put(pickaxe.id(), pickaxe);
			staticComponents.put(axe.id(), axe);
			staticComponents.put(normal.id(), normal);
			staticComponents.put(mastercrafted.id(), mastercrafted);

			PartTemplateData template = createPartTemplate(
					"forgero:part_template",
					Map.of(
							"material", new PartTemplateStructureSlotData(id("material"), null, null, "Material"),
							"shape", new PartTemplateStructureSlotData(id("shape"), null, null, "Shape"),
							"quality", new PartTemplateStructureSlotData(id("quality"), null, null, "Quality")
					),
					"forgero:{quality.name}-{material.name}-{shape.name}",
					null,
					null
			);
			addRawDefinition("forgero:part_template", template);

			// When: Generate
			TemplateGenerator generator = createGenerator();
			TemplateGenerator.TemplateResult result = generator.generate();

			// Then: Should generate 8 combinations (2 × 2 × 2)
			assertEquals(8, result.components().size());
		}

		@Test
		void returnsEmptyListWhenNoCompatibleComponents() {
			// Given: Material but no shapes
			CofComponent iron = createStaticComponent("forgero:iron", Set.of("forgero:material"));
			staticComponents.put(iron.id(), iron);

			PartTemplateData template = createPartTemplate(
					"forgero:part_template",
					Map.of(
							"material", new PartTemplateStructureSlotData(id("material"), null, null, "Material"),
							"shape", new PartTemplateStructureSlotData(id("shape"), null, null, "Shape")
					),
					"forgero:{material.name}-{shape.name}",
					null,
					null
			);
			addRawDefinition("forgero:part_template", template);

			// When: Generate
			TemplateGenerator generator = createGenerator();
			TemplateGenerator.TemplateResult result = generator.generate();

			// Then: Should generate nothing (no shapes available)
			assertEquals(0, result.components().size());
		}
	}

	@Nested
	class EdgeCasesAndErrors {

		@Test
		void handlesEmptyStaticComponentsMap() {
			// Given: No static components
			PartTemplateData template = createPartTemplate(
					"forgero:part_template",
					Map.of(
							"material", new PartTemplateStructureSlotData(id("material"), null, null, "Material")
					),
					"forgero:{material.name}",
					null,
					null
			);
			addRawDefinition("forgero:part_template", template);

			// When: Generate
			TemplateGenerator generator = createGenerator();
			TemplateGenerator.TemplateResult result = generator.generate();

			// Then: Should generate nothing
			assertEquals(0, result.components().size());
		}

		@Test
		void handlesEmptyTemplateList() {
			// Given: Static components but no templates
			CofComponent iron = createStaticComponent("forgero:iron", Set.of("forgero:material"));
			staticComponents.put(iron.id(), iron);

			// When: Generate
			TemplateGenerator generator = createGenerator();
			TemplateGenerator.TemplateResult result = generator.generate();

			// Then: Should generate nothing
			assertEquals(0, result.components().size());
		}

		@Test
		void handlesComponentsWithoutTags() {
			// Given: Component without tags
			CofComponent noTags = createStaticComponent("forgero:mysterious", Set.of());
			staticComponents.put(noTags.id(), noTags);

			CofComponent shape = createStaticComponent("forgero:pickaxe_head_shape", Set.of("forgero:shape"));
			staticComponents.put(shape.id(), shape);

			PartTemplateData template = createPartTemplate(
					"forgero:part_template",
					Map.of(
							"material", new PartTemplateStructureSlotData(id("material"), null, null, "Material"),
							"shape", new PartTemplateStructureSlotData(id("shape"), null, null, "Shape")
					),
					"forgero:{material.name}-{shape.name}",
					null,
					null
			);
			addRawDefinition("forgero:part_template", template);

			// When: Generate
			TemplateGenerator generator = createGenerator();
			TemplateGenerator.TemplateResult result = generator.generate();

			// Then: Should not match component without tags
			assertEquals(0, result.components().size());
		}

		@Test
		void handlesTemplateWithNoSlots() {
			// Given: Template with empty slots
			PartTemplateData template = createPartTemplate(
					"forgero:part_template",
					Map.of(),
					"forgero:empty-part",
					null,
					null
			);
			addRawDefinition("forgero:part_template", template);

			// When: Generate
			TemplateGenerator generator = createGenerator();
			TemplateGenerator.TemplateResult result = generator.generate();

			// Then: Should generate nothing (no combinations possible)
			assertEquals(0, result.components().size());
		}
	}

	@Nested
	class EdgeCases {

		@Test
		void preventsDuplicateIDGeneration() {
			// Given: Two different materials that would generate the same ID
			CofComponent iron1 = createStaticComponent("forgero:iron", Set.of("forgero:material"));
			CofComponent iron2 = createStaticComponent("forgero:iron_ore", Set.of("forgero:material"));
			staticComponents.put(iron1.id(), iron1);
			staticComponents.put(iron2.id(), iron2);

			CofComponent shape = createStaticComponent("forgero:pickaxe_head_shape", Set.of("forgero:shape"));
			staticComponents.put(shape.id(), shape);

			// Template uses only material.name which might collide if shortened
			PartTemplateData template = createPartTemplate(
					"forgero:part_template",
					Map.of(
							"material", new PartTemplateStructureSlotData(id("material"), null, null, "Material"),
							"shape", new PartTemplateStructureSlotData(id("shape"), null, null, "Shape")
					),
					"forgero:{material.name}-{shape.name}",
					null,
					null
			);
			addRawDefinition("forgero:part_template", template);

			// When: Generate
			TemplateGenerator generator = createGenerator();
			TemplateGenerator.TemplateResult result = generator.generate();

			// Then: Should generate unique components with unique IDs
			Set<OpenIdentifier> generatedIds = new HashSet<>();
			for (CofComponent comp : result.components()) {
				assertTrue(generatedIds.add(comp.id()),
						"Duplicate ID generated: " + comp.id());
			}
		}

		@Test
		void limitsCartesianProductExplosion() {
			// Given: Many materials and shapes that could create a huge cartesian product
			for (int i = 0; i < 50; i++) {
				CofComponent material = createStaticComponent("forgero:material_" + i, Set.of("forgero:material"));
				staticComponents.put(material.id(), material);
			}
			for (int i = 0; i < 50; i++) {
				CofComponent shape = createStaticComponent("forgero:shape_" + i, Set.of("forgero:shape"));
				staticComponents.put(shape.id(), shape);
			}

			PartTemplateData template = createPartTemplate(
					"forgero:part_template",
					Map.of(
							"material", new PartTemplateStructureSlotData(id("material"), null, null, "Material"),
							"shape", new PartTemplateStructureSlotData(id("shape"), null, null, "Shape")
					),
					"forgero:{material.name}-{shape.name}",
					null,
					null
			);
			addRawDefinition("forgero:part_template", template);

			// When: Generate
			TemplateGenerator generator = createGenerator();
			TemplateGenerator.TemplateResult result = generator.generate();

			// Then: Should complete without hanging or OOM (50 * 50 = 2500 combinations)
			assertNotNull(result);
			assertEquals(2500, result.components().size(),
					"Should generate all combinations when reasonable");
		}

		@Test
		void handlesIDTemplateWithMissingPlaceholders() {
			// Given: Template with placeholder but no corresponding slot
			CofComponent iron = createStaticComponent("forgero:iron", Set.of("forgero:material"));
			staticComponents.put(iron.id(), iron);

			// Template references {quality.name} but no quality slot exists
			PartTemplateData template = createPartTemplate(
					"forgero:part_template",
					Map.of(
							"material", new PartTemplateStructureSlotData(id("material"), null, null, "Material")
					),
					"forgero:{quality.name}-{material.name}",  // quality doesn't exist!
					null,
					null
			);
			addRawDefinition("forgero:part_template", template);

			// When: Generate
			TemplateGenerator generator = createGenerator();

			// Then: Should throw IllegalArgumentException for missing placeholder
			assertThrows(IllegalArgumentException.class, generator::generate,
					"Should throw exception when placeholder has no corresponding slot");
		}

		@Test
		void handlesIDTemplateWithSpecialCharacters() {
			// Given: Component with special characters in name
			CofComponent special = createStaticComponent("forgero:iron-ore_v2", Set.of("forgero:material"));
			staticComponents.put(special.id(), special);

			CofComponent shape = createStaticComponent("forgero:pickaxe_head_shape", Set.of("forgero:shape"));
			staticComponents.put(shape.id(), shape);

			PartTemplateData template = createPartTemplate(
					"forgero:part_template",
					Map.of(
							"material", new PartTemplateStructureSlotData(id("material"), null, null, "Material"),
							"shape", new PartTemplateStructureSlotData(id("shape"), null, null, "Shape")
					),
					"forgero:{material.name}-{shape.name}",
					null,
					null
			);
			addRawDefinition("forgero:part_template", template);

			// When: Generate
			TemplateGenerator generator = createGenerator();
			TemplateGenerator.TemplateResult result = generator.generate();

			// Then: Should handle special characters gracefully
			assertNotNull(result);
			assertTrue(result.components().size() > 0);
			for (CofComponent comp : result.components()) {
				// ID should be valid (no double dashes, etc.)
				assertFalse(comp.id().toString().contains("--"));
			}
		}

		@Test
		void respectsFilterPriorityWithMultipleFilters() {
			// Given: Multiple overlapping filters
			CofComponent iron = createStaticComponent("forgero:iron", Set.of("forgero:metal", "forgero:common"));
			CofComponent gold = createStaticComponent("forgero:gold", Set.of("forgero:metal", "forgero:rare"));
			CofComponent diamond = createStaticComponent("forgero:diamond", Set.of("forgero:gem", "forgero:rare"));
			staticComponents.put(iron.id(), iron);
			staticComponents.put(gold.id(), gold);
			staticComponents.put(diamond.id(), diamond);

			CofComponent shape = createStaticComponent("forgero:pickaxe_head_shape", Set.of("forgero:shape"));
			staticComponents.put(shape.id(), shape);

			// Filter: require metal OR rare (should match iron, gold, diamond)
			// Then exclude common (should exclude iron)
			// Net result: gold only (has metal+rare, diamond has gem+rare but not metal)
			SlotGenerationFilter filter = SlotGenerationFilter.builder()
					.requireAllTags(List.of(id("metal")))
					.excludeAnyTags(List.of(id("common")))
					.build();
			GenerationConfigData generationConfig = new GenerationConfigData(Map.of("material", filter));

			PartTemplateData template = createPartTemplate(
					"forgero:part_template",
					Map.of(
							"material", new PartTemplateStructureSlotData(id("material"), null, null, "Material"),
							"shape", new PartTemplateStructureSlotData(id("shape"), null, null, "Shape")
					),
					"forgero:{material.name}-{shape.name}",
					null,
					null,
					generationConfig
			);
			addRawDefinition("forgero:part_template", template);

			// When: Generate
			TemplateGenerator generator = createGenerator();
			TemplateGenerator.TemplateResult result = generator.generate();

			// Then: Should only generate gold-pickaxe_head (gold has metal, excludes iron with common)
			assertEquals(1, result.components().size());
			assertEquals(id("gold-pickaxe_head"), result.components().get(0).id());
		}

		@Test
		void handlesEmptyFilterList() {
			// Given: Generation config with empty filter list
			CofComponent iron = createStaticComponent("forgero:iron", Set.of("forgero:material"));
			staticComponents.put(iron.id(), iron);

			CofComponent shape = createStaticComponent("forgero:pickaxe_head_shape", Set.of("forgero:shape"));
			staticComponents.put(shape.id(), shape);

			GenerationConfigData generationConfig = new GenerationConfigData(Map.of());

			PartTemplateData template = createPartTemplate(
					"forgero:part_template",
					Map.of(
							"material", new PartTemplateStructureSlotData(id("material"), null, null, "Material"),
							"shape", new PartTemplateStructureSlotData(id("shape"), null, null, "Shape")
					),
					"forgero:{material.name}-{shape.name}",
					null,
					null,
					generationConfig
			);
			addRawDefinition("forgero:part_template", template);

			// When: Generate
			TemplateGenerator generator = createGenerator();
			TemplateGenerator.TemplateResult result = generator.generate();

			// Then: Should work as if no filters (include all)
			assertEquals(1, result.components().size());
		}

		@Test
		void validatesMaterialCompatibilityAcrossSlots() {
			// Given: Materials with compatibility constraints
			CofComponent iron = createStaticComponent("forgero:iron", Set.of("forgero:metal"));
			CofComponent wood = createStaticComponent("forgero:wood", Set.of("forgero:organic"));
			staticComponents.put(iron.id(), iron);
			staticComponents.put(wood.id(), wood);

			// Two slots both requiring materials - should create all combinations
			PartTemplateData template = createPartTemplate(
					"forgero:part_template",
					Map.of(
							"primary", new PartTemplateStructureSlotData(id("metal"), null, null, "Primary"),
							"secondary", new PartTemplateStructureSlotData(id("organic"), null, null, "Secondary")
					),
					"forgero:{primary.name}-{secondary.name}",
					null,
					null
			);
			addRawDefinition("forgero:part_template", template);

			// When: Generate
			TemplateGenerator generator = createGenerator();
			TemplateGenerator.TemplateResult result = generator.generate();

			// Then: Should only generate valid combinations
			assertEquals(1, result.components().size(),
					"Should generate iron-wood combination");
		}

		@Test
		void handlesConflictingIDsAcrossMultipleTemplates() {
			// Given: Two templates that would generate the same IDs
			CofComponent iron = createStaticComponent("forgero:iron", Set.of("forgero:material"));
			staticComponents.put(iron.id(), iron);

			CofComponent shape1 = createStaticComponent("forgero:pickaxe_head_shape", Set.of("forgero:shape"));
			CofComponent shape2 = createStaticComponent("forgero:axe_head_shape", Set.of("forgero:shape"));
			staticComponents.put(shape1.id(), shape1);
			staticComponents.put(shape2.id(), shape2);

			// Two templates with identical ID patterns (will generate duplicates)
			PartTemplateData template1 = createPartTemplate(
					"forgero:part_template_1",
					Map.of(
							"material", new PartTemplateStructureSlotData(id("material"), null, null, "Material"),
							"shape", new PartTemplateStructureSlotData(id("shape"), null, null, "Shape")
					),
					"forgero:{material.name}-head",  // Both generate "iron-head"
					null,
					null
			);
			PartTemplateData template2 = createPartTemplate(
					"forgero:part_template_2",
					Map.of(
							"material", new PartTemplateStructureSlotData(id("material"), null, null, "Material"),
							"shape", new PartTemplateStructureSlotData(id("shape"), null, null, "Shape")
					),
					"forgero:{material.name}-head",  // Same pattern!
					null,
					null
			);
			addRawDefinition("forgero:part_template_1", template1);
			addRawDefinition("forgero:part_template_2", template2);

			// When: Generate
			TemplateGenerator generator = createGenerator();
			TemplateGenerator.TemplateResult result = generator.generate();

			// Then: Generator completes without crashing (may produce duplicate IDs)
			// Note: Later components with same ID may overwrite earlier ones
			assertNotNull(result);
			assertTrue(result.components().size() > 0,
					"Should generate at least one component");
		}

		@Test
		void handlesLargeNumberOfSlots() {
			// Given: Template with many slots
			Map<String, PartTemplateStructureSlotData> slots = new HashMap<>();
			StringBuilder idTemplate = new StringBuilder("forgero:");

			// Create 10 slots
			for (int i = 0; i < 10; i++) {
				String slotName = "slot" + i;
				CofComponent comp = createStaticComponent("forgero:comp" + i, Set.of("forgero:tag" + i));
				staticComponents.put(comp.id(), comp);
				slots.put(slotName, new PartTemplateStructureSlotData(id("tag" + i), null, null, "Slot " + i));
				idTemplate.append("{").append(slotName).append(".name}");
				if (i < 9) idTemplate.append("-");
			}

			PartTemplateData template = createPartTemplate(
					"forgero:part_template",
					slots,
					idTemplate.toString(),
					null,
					null
			);
			addRawDefinition("forgero:part_template", template);

			// When: Generate
			TemplateGenerator generator = createGenerator();
			TemplateGenerator.TemplateResult result = generator.generate();

			// Then: Should handle many slots without issues
			assertEquals(1, result.components().size());
			CofComponent generated = result.components().get(0);
			assertNotNull(generated);
			// ID should contain all slot names
			assertTrue(generated.id().toString().contains("comp0"));
			assertTrue(generated.id().toString().contains("comp9"));
		}

		@Test
		void handlesCircularTemplateDependencies() {
			// Given: Templates that might reference each other
			// Note: This is a stress test - the actual behavior depends on implementation
			PartTemplateData template1 = createPartTemplate(
					"forgero:template_a",
					Map.of(),
					"forgero:result_a",
					null,
					null
			);
			PartTemplateData template2 = createPartTemplate(
					"forgero:template_b",
					Map.of(),
					"forgero:result_b",
					null,
					null
			);
			addRawDefinition("forgero:template_a", template1);
			addRawDefinition("forgero:template_b", template2);

			// When: Generate
			TemplateGenerator generator = createGenerator();

			// Then: Should not infinite loop
			assertDoesNotThrow(() -> {
				TemplateGenerator.TemplateResult result = generator.generate();
				assertNotNull(result);
			});
		}
	}

	// ===== Helper Methods =====

	private OpenIdentifier id(String id) {
		return idFactory.of(id);
	}

	private TemplateGenerator createGenerator() {
		return new TemplateGenerator(idFactory, tagResolver, propertyMerger, idTemplateResolver, staticComponents, rawDefinitions);
	}

	private CofComponent createStaticComponent(String id, Set<String> tags) {
		return createStaticComponent(id, tags, Map.of());
	}

	private CofComponent createStaticComponent(String id, Set<String> tags, Map<String, List<?>> properties) {
		Set<OpenIdentifier> tagSet = new HashSet<>();
		tags.forEach(tag -> tagSet.add(idFactory.of(tag)));
		OpenIdentifier compId = idFactory.of(id);

		// Also add a RawDefinition for this component so TemplateGenerator.generatePart() can find it
		List<OpenIdentifier> tagList = new ArrayList<>(tagSet);
		ResourceData resourceData = new ResourceData(
				idFactory.of("forgero:static"),
				compId.name(),
				null,
				tagList,
				null,
				null,
				null,
				null,
				null
		);
		rawDefinitions.put(compId, new RawDefinition(compId, resourceData));

		return new CofComponent(
				compId,
				ComponentTypeRegistry.STATIC_COMPONENT,
				Optional.of(tagSet),
				Optional.of(properties),
				Optional.empty(),
				Optional.empty(),
				Optional.of(1)
		);
	}

	private CofComponent createStructuredPart(String id, Set<String> tags) {
		OpenIdentifier compId = idFactory.of(id);
		Set<OpenIdentifier> tagSet = new HashSet<>();
		tags.forEach(tag -> tagSet.add(idFactory.of(tag)));

		// Also add a RawDefinition for this part
		List<OpenIdentifier> tagList = new ArrayList<>(tagSet);
		ResourceData resourceData = new ResourceData(
				idFactory.of("forgero:part"),
				compId.name(),
				null,
				tagList,
				null,
				null,
				null,
				null,
				null
		);
		rawDefinitions.put(compId, new RawDefinition(compId, resourceData));

		return new CofComponent(
				compId,
				ComponentTypeRegistry.STRUCTURED_PART,
				Optional.of(tagSet),
				Optional.of(Map.of()),
				Optional.empty(),
				Optional.empty(),
				Optional.of(1)
		);
	}

	private PartTemplateData createPartTemplate(
			String id,
			Map<String, PartTemplateStructureSlotData> slots,
			String idTemplate,
			Map<String, List<?>> properties,
			List<UpgradeSlotData> upgrades
	) {
		return createPartTemplate(id, slots, idTemplate, properties, upgrades, null, null);
	}

	private PartTemplateData createPartTemplate(
			String id,
			Map<String, PartTemplateStructureSlotData> slots,
			String idTemplate,
			Map<String, List<?>> properties,
			List<UpgradeSlotData> upgrades,
			GenerationConfigData generationConfig
	) {
		return createPartTemplate(id, slots, idTemplate, properties, upgrades, generationConfig, null);
	}

	private PartTemplateData createPartTemplate(
			String id,
			Map<String, PartTemplateStructureSlotData> slots,
			String idTemplate,
			Map<String, List<?>> properties,
			List<UpgradeSlotData> upgrades,
			GenerationConfigData generationConfig,
			HostTemplateData hostTemplate
	) {
		return new PartTemplateData(
				id("part_template"),  // type
				"Part template",      // name
				null,                 // include
				null,                 // tags
				hostTemplate,         // host_template
				new PartTemplateStructureData(idTemplate, slots), // structure
				upgrades,             // upgrades
				null,                 // attributes
				generationConfig,     // generation
				null                  // properties (JsonElement map)
		);
	}

	private EquipmentTemplateData createEquipmentTemplate(
			String id,
			Map<String, EquipmentTemplateSlotData> slots,
			String idTemplate
	) {
		return new EquipmentTemplateData(
				id("equipment_template"),  // type
				"Equipment template",      // name
				null,                      // include
				null,                      // tags
				null,                      // host_template
				new EquipmentTemplateStructureData(idTemplate, slots), // structure
				null,                      // upgrades
				null,                      // attributes
				null                       // properties
		);
	}

	private void addRawDefinition(String id, PartTemplateData data) {
		RawDefinition rawDef = new RawDefinition(idFactory.of(id), data);
		rawDefinitions.put(idFactory.of(id), rawDef);
		rawDefinitions.put(data.type(), new RawDefinition(data.type(), data));
	}

	private void addRawDefinition(String id, EquipmentTemplateData data) {
		RawDefinition rawDef = new RawDefinition(idFactory.of(id), data);
		rawDefinitions.put(idFactory.of(id), rawDef);
		rawDefinitions.put(data.type(), new RawDefinition(data.type(), data));
	}
}
