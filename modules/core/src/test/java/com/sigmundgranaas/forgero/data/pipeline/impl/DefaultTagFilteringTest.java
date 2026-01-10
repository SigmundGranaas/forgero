package com.sigmundgranaas.forgero.data.pipeline.impl;

import com.sigmundgranaas.forgero.cof.ComponentTypeRegistry;
import com.sigmundgranaas.forgero.cof.dto.CofComponent;
import com.sigmundgranaas.forgero.common.identifier.api.IdentifierFactory;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.tags.api.TagResolver;
import com.sigmundgranaas.forgero.common.tags.engine.TagGraphBuilder;
import com.sigmundgranaas.forgero.data.loading.api.RawDefinition;
import com.sigmundgranaas.forgero.data.loading.api.data.ResourceData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.EquipmentTemplateData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.EquipmentTemplateSlotData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.EquipmentTemplateStructureData;
import com.sigmundgranaas.forgero.data.pipeline.util.IdTemplateResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Default Tag Filtering in Equipment Generation")
class DefaultTagFilteringTest {

	private IdentifierFactory idFactory;
	private TagResolver tagResolver;
	private PropertyMerger propertyMerger;
	private IdTemplateResolver idTemplateResolver;
	private Map<OpenIdentifier, CofComponent> staticComponents;
	private Map<OpenIdentifier, RawDefinition> rawDefinitions;

	@BeforeEach
	void setUp() {
		idFactory = new IdentifierFactory.Builder().defaultNamespace("forgero").build();

		TagGraphBuilder tagGraphBuilder = new TagGraphBuilder();
		tagGraphBuilder.add(id("parts/types/pickaxe_head"), Set.of());
		tagGraphBuilder.add(id("parts/types/axe_head"), Set.of());
		tagGraphBuilder.add(id("parts/types/shovel_head"), Set.of());
		tagGraphBuilder.add(id("parts/pickaxe_head"), Set.of());
		tagGraphBuilder.add(id("parts/axe_head"), Set.of());
		tagGraphBuilder.add(id("parts/shovel_head"), Set.of());
		tagGraphBuilder.add(id("parts/hammer_head"), Set.of());
		tagGraphBuilder.add(id("parts/mandrill_pickaxe_head"), Set.of());
		tagGraphBuilder.add(id("parts/spade_head"), Set.of());
		tagGraphBuilder.add(id("parts/felling_axe_head"), Set.of());
		tagGraphBuilder.add(id("parts/categories/handle"), Set.of());
		tagResolver = tagGraphBuilder.build();

		propertyMerger = new PropertyMerger(Map.of());
		staticComponents = new HashMap<>();
		rawDefinitions = new HashMap<>();
		idTemplateResolver = new IdTemplateResolver(idFactory, rawDefinitions);
	}

	@Nested
	@DisplayName("Mining tool heads should not generate default equipment")
	class MiningToolHeadFiltering {

		@Test
		@DisplayName("Standard pickaxe_head generates pickaxe equipment")
		void standardPickaxeHeadGeneratesEquipment() {
			CofComponent standardHead = createStructuredPart(
					"forgero:iron-pickaxe_head",
					Set.of("forgero:parts/types/pickaxe_head", "forgero:parts/pickaxe_head")
			);
			CofComponent handle = createStructuredPart(
					"forgero:oak-handle",
					Set.of("forgero:parts/categories/handle")
			);
			staticComponents.put(standardHead.id(), standardHead);
			staticComponents.put(handle.id(), handle);

			EquipmentTemplateData template = createPickaxeTemplate();
			addRawDefinition("forgero:pickaxe_template", template);

			TemplateGenerator generator = createGenerator();
			TemplateGenerator.TemplateResult result = generator.generate();

			assertEquals(1, result.components().size(),
					"Standard pickaxe_head should generate exactly one pickaxe");
			assertTrue(result.components().get(0).id().toString().contains("pickaxe"),
					"Generated equipment should be a pickaxe");
		}

		@Test
		@DisplayName("Hammer head does NOT generate pickaxe equipment when default_tag is specific")
		void hammerHeadDoesNotGeneratePickaxe() {
			CofComponent hammerHead = createStructuredPart(
					"forgero:iron-hammer_head",
					Set.of("forgero:parts/types/pickaxe_head", "forgero:parts/hammer_head")
			);
			CofComponent handle = createStructuredPart(
					"forgero:oak-handle",
					Set.of("forgero:parts/categories/handle")
			);
			staticComponents.put(hammerHead.id(), hammerHead);
			staticComponents.put(handle.id(), handle);

			EquipmentTemplateData template = createPickaxeTemplate();
			addRawDefinition("forgero:pickaxe_template", template);

			TemplateGenerator generator = createGenerator();
			TemplateGenerator.TemplateResult result = generator.generate();

			assertEquals(0, result.components().size(),
					"Hammer head should NOT generate pickaxe equipment because it lacks forgero:parts/pickaxe_head tag");
		}

		@Test
		@DisplayName("Mandrill pickaxe head does NOT generate pickaxe equipment")
		void mandrillHeadDoesNotGeneratePickaxe() {
			CofComponent mandrillHead = createStructuredPart(
					"forgero:iron-mandrill_pickaxe_head",
					Set.of("forgero:parts/types/pickaxe_head", "forgero:parts/mandrill_pickaxe_head")
			);
			CofComponent handle = createStructuredPart(
					"forgero:oak-handle",
					Set.of("forgero:parts/categories/handle")
			);
			staticComponents.put(mandrillHead.id(), mandrillHead);
			staticComponents.put(handle.id(), handle);

			EquipmentTemplateData template = createPickaxeTemplate();
			addRawDefinition("forgero:pickaxe_template", template);

			TemplateGenerator generator = createGenerator();
			TemplateGenerator.TemplateResult result = generator.generate();

			assertEquals(0, result.components().size(),
					"Mandrill head should NOT generate pickaxe equipment");
		}

		@Test
		@DisplayName("Only standard head generates equipment when both standard and variant exist")
		void onlyStandardHeadGeneratesWhenBothExist() {
			CofComponent standardHead = createStructuredPart(
					"forgero:iron-pickaxe_head",
					Set.of("forgero:parts/types/pickaxe_head", "forgero:parts/pickaxe_head")
			);
			CofComponent hammerHead = createStructuredPart(
					"forgero:iron-hammer_head",
					Set.of("forgero:parts/types/pickaxe_head", "forgero:parts/hammer_head")
			);
			CofComponent mandrillHead = createStructuredPart(
					"forgero:iron-mandrill_pickaxe_head",
					Set.of("forgero:parts/types/pickaxe_head", "forgero:parts/mandrill_pickaxe_head")
			);
			CofComponent handle = createStructuredPart(
					"forgero:oak-handle",
					Set.of("forgero:parts/categories/handle")
			);
			staticComponents.put(standardHead.id(), standardHead);
			staticComponents.put(hammerHead.id(), hammerHead);
			staticComponents.put(mandrillHead.id(), mandrillHead);
			staticComponents.put(handle.id(), handle);

			EquipmentTemplateData template = createPickaxeTemplate();
			addRawDefinition("forgero:pickaxe_template", template);

			TemplateGenerator generator = createGenerator();
			TemplateGenerator.TemplateResult result = generator.generate();

			assertEquals(1, result.components().size(),
					"Only ONE pickaxe should be generated (from standard head), not 3");
			
			CofComponent generated = result.components().get(0);
			assertTrue(generated.structure().isPresent());
			CofComponent headInSlot = generated.structure().get().slots().get(id("head")).content();
			assertEquals(standardHead.id(), headInSlot.id(),
					"Generated pickaxe should use standard pickaxe_head, not hammer or mandrill");
		}
	}

	@Nested
	@DisplayName("Axe and shovel mining variants")
	class AxeAndShovelVariants {

		@Test
		@DisplayName("Felling axe head does NOT generate axe equipment")
		void fellingAxeHeadDoesNotGenerateAxe() {
			CofComponent fellingHead = createStructuredPart(
					"forgero:iron-felling_axe_head",
					Set.of("forgero:parts/types/axe_head", "forgero:parts/felling_axe_head")
			);
			CofComponent handle = createStructuredPart(
					"forgero:oak-handle",
					Set.of("forgero:parts/categories/handle")
			);
			staticComponents.put(fellingHead.id(), fellingHead);
			staticComponents.put(handle.id(), handle);

			EquipmentTemplateData template = createAxeTemplate();
			addRawDefinition("forgero:axe_template", template);

			TemplateGenerator generator = createGenerator();
			TemplateGenerator.TemplateResult result = generator.generate();

			assertEquals(0, result.components().size(),
					"Felling axe head should NOT generate axe equipment");
		}

		@Test
		@DisplayName("Spade head does NOT generate shovel equipment")
		void spadeHeadDoesNotGenerateShovel() {
			CofComponent spadeHead = createStructuredPart(
					"forgero:iron-spade_head",
					Set.of("forgero:parts/types/shovel_head", "forgero:parts/spade_head")
			);
			CofComponent handle = createStructuredPart(
					"forgero:oak-handle",
					Set.of("forgero:parts/categories/handle")
			);
			staticComponents.put(spadeHead.id(), spadeHead);
			staticComponents.put(handle.id(), handle);

			EquipmentTemplateData template = createShovelTemplate();
			addRawDefinition("forgero:shovel_template", template);

			TemplateGenerator generator = createGenerator();
			TemplateGenerator.TemplateResult result = generator.generate();

			assertEquals(0, result.components().size(),
					"Spade head should NOT generate shovel equipment");
		}

		@Test
		@DisplayName("Standard axe_head generates axe equipment")
		void standardAxeHeadGeneratesAxe() {
			CofComponent standardHead = createStructuredPart(
					"forgero:iron-axe_head",
					Set.of("forgero:parts/types/axe_head", "forgero:parts/axe_head")
			);
			CofComponent handle = createStructuredPart(
					"forgero:oak-handle",
					Set.of("forgero:parts/categories/handle")
			);
			staticComponents.put(standardHead.id(), standardHead);
			staticComponents.put(handle.id(), handle);

			EquipmentTemplateData template = createAxeTemplate();
			addRawDefinition("forgero:axe_template", template);

			TemplateGenerator generator = createGenerator();
			TemplateGenerator.TemplateResult result = generator.generate();

			assertEquals(1, result.components().size(),
					"Standard axe_head should generate exactly one axe");
		}

		@Test
		@DisplayName("Standard shovel_head generates shovel equipment")
		void standardShovelHeadGeneratesShovel() {
			CofComponent standardHead = createStructuredPart(
					"forgero:iron-shovel_head",
					Set.of("forgero:parts/types/shovel_head", "forgero:parts/shovel_head")
			);
			CofComponent handle = createStructuredPart(
					"forgero:oak-handle",
					Set.of("forgero:parts/categories/handle")
			);
			staticComponents.put(standardHead.id(), standardHead);
			staticComponents.put(handle.id(), handle);

			EquipmentTemplateData template = createShovelTemplate();
			addRawDefinition("forgero:shovel_template", template);

			TemplateGenerator generator = createGenerator();
			TemplateGenerator.TemplateResult result = generator.generate();

			assertEquals(1, result.components().size(),
					"Standard shovel_head should generate exactly one shovel");
		}
	}

	@Nested
	@DisplayName("Backwards compatibility - type tag without default_tag")
	class BackwardsCompatibility {

		@Test
		@DisplayName("When default_tag equals type, all matching parts generate equipment")
		void typeTagGeneratesAllMatchingParts() {
			CofComponent standardHead = createStructuredPart(
					"forgero:iron-pickaxe_head",
					Set.of("forgero:parts/types/pickaxe_head", "forgero:parts/pickaxe_head")
			);
			CofComponent hammerHead = createStructuredPart(
					"forgero:iron-hammer_head",
					Set.of("forgero:parts/types/pickaxe_head", "forgero:parts/hammer_head")
			);
			CofComponent handle = createStructuredPart(
					"forgero:oak-handle",
					Set.of("forgero:parts/categories/handle")
			);
			staticComponents.put(standardHead.id(), standardHead);
			staticComponents.put(hammerHead.id(), hammerHead);
			staticComponents.put(handle.id(), handle);

			EquipmentTemplateData template = createPickaxeTemplateWithTypeAsDefault();
			addRawDefinition("forgero:pickaxe_template", template);

			TemplateGenerator generator = createGenerator();
			TemplateGenerator.TemplateResult result = generator.generate();

			assertEquals(2, result.components().size(),
					"When default_tag = type tag, ALL matching parts should generate equipment (old behavior)");
		}
	}

	private OpenIdentifier id(String id) {
		if (id.contains(":")) {
			return OpenIdentifier.parse(id);
		}
		return OpenIdentifier.of("forgero", id);
	}

	private TemplateGenerator createGenerator() {
		return new TemplateGenerator(idFactory, tagResolver, propertyMerger, idTemplateResolver, staticComponents, rawDefinitions);
	}

	private CofComponent createStructuredPart(String id, Set<String> tags) {
		OpenIdentifier compId = idFactory.of(id);
		Set<OpenIdentifier> tagSet = new HashSet<>();
		tags.forEach(tag -> tagSet.add(id(tag)));

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

	private EquipmentTemplateData createPickaxeTemplate() {
		return new EquipmentTemplateData(
				id("equipment_template"),
				"Pickaxe",
				null,
				null,
				null,
				new EquipmentTemplateStructureData(
						"forgero:{head.name}-pickaxe",
						Map.of(
								"head", new EquipmentTemplateSlotData(
										id("parts/types/pickaxe_head"),
										id("parts/pickaxe_head"),
										null
								),
								"handle", new EquipmentTemplateSlotData(
										id("parts/categories/handle"),
										null,
										id("oak-handle")
								)
						)
				),
				null,
				null,
				null
		);
	}

	private EquipmentTemplateData createPickaxeTemplateWithTypeAsDefault() {
		return new EquipmentTemplateData(
				id("equipment_template"),
				"Pickaxe",
				null,
				null,
				null,
				new EquipmentTemplateStructureData(
						"forgero:{head.name}-pickaxe",
						Map.of(
								"head", new EquipmentTemplateSlotData(
										id("parts/types/pickaxe_head"),
										id("parts/types/pickaxe_head"),
										null
								),
								"handle", new EquipmentTemplateSlotData(
										id("parts/categories/handle"),
										null,
										id("oak-handle")
								)
						)
				),
				null,
				null,
				null
		);
	}

	private EquipmentTemplateData createAxeTemplate() {
		return new EquipmentTemplateData(
				id("equipment_template"),
				"Axe",
				null,
				null,
				null,
				new EquipmentTemplateStructureData(
						"forgero:{head.name}-axe",
						Map.of(
								"head", new EquipmentTemplateSlotData(
										id("parts/types/axe_head"),
										id("parts/axe_head"),
										null
								),
								"handle", new EquipmentTemplateSlotData(
										id("parts/categories/handle"),
										null,
										id("oak-handle")
								)
						)
				),
				null,
				null,
				null
		);
	}

	private EquipmentTemplateData createShovelTemplate() {
		return new EquipmentTemplateData(
				id("equipment_template"),
				"Shovel",
				null,
				null,
				null,
				new EquipmentTemplateStructureData(
						"forgero:{head.name}-shovel",
						Map.of(
								"head", new EquipmentTemplateSlotData(
										id("parts/types/shovel_head"),
										id("parts/shovel_head"),
										null
								),
								"handle", new EquipmentTemplateSlotData(
										id("parts/categories/handle"),
										null,
										id("oak-handle")
								)
						)
				),
				null,
				null,
				null
		);
	}

	private void addRawDefinition(String id, EquipmentTemplateData data) {
		RawDefinition rawDef = new RawDefinition(idFactory.of(id), data);
		rawDefinitions.put(idFactory.of(id), rawDef);
	}
}
