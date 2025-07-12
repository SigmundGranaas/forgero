package com.sigmundgranaas.forgero.data.generation;

import com.sigmundgranaas.forgero.common.identifier.api.IdentifierFactory;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.tags.engine.TagGraph;
import com.sigmundgranaas.forgero.data.generation.api.ComponentGenerator;
import com.sigmundgranaas.forgero.data.generation.api.GeneratedState;
import com.sigmundgranaas.forgero.data.generation.impl.ComponentGeneratorImpl;
import com.sigmundgranaas.forgero.data.loading.api.data.host.HostData;
import com.sigmundgranaas.forgero.data.loading.api.data.host.template.CreateTemplateData;
import com.sigmundgranaas.forgero.data.loading.api.data.host.template.HostTemplateData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.EquipmentTemplateSlotData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.EquipmentTemplateStructureData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.PartTemplateStructureData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.PartTemplateStructureSlotData;
import com.sigmundgranaas.forgero.data.processing.api.NormalizedState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ComponentGeneratorTest {

	private ComponentGenerator generator;
	private IdentifierFactory idFactory;
	private TagGraph tagGraph;
	private NormalizedState state;
	private Map<OpenIdentifier, Set<OpenIdentifier>> tagRelationships;


	@BeforeEach
	void setUp() {
		idFactory = new IdentifierFactory.Builder().defaultNamespace("forgero").build();
		generator = new ComponentGeneratorImpl(idFactory);

		// Initialize the map for TagGraph relationships
		tagRelationships = new HashMap<>();
		tagRelationships.put(idFactory.of("forgero:tool_material"), Collections.emptySet());
		tagRelationships.put(idFactory.of("forgero:pickaxe_head_shape"), Collections.emptySet());
		tagGraph = new TagGraph(tagRelationships);


		// Manually create a NormalizedState for testing to isolate the generator
		var ironMaterial = new NormalizedState.NormalizedMaterial(
				idFactory.of("forgero:iron"),
				"Iron",
				Set.of(idFactory.of("forgero:tool_material")),
				null,
				null);

		var pickaxeHeadShape = new NormalizedState.NormalizedShape(
				idFactory.of("forgero:pickaxe_head_shape"),
				"Pickaxe Head Shape",
				Set.of(idFactory.of("forgero:pickaxe_head_shape")),
				null,
				null);

		var pickaxeHeadTemplateNoHost = new NormalizedState.NormalizedPartTemplate(
				idFactory.of("forgero:pickaxe_head_template_no_host"),
				"Pickaxe Head No Host",
				Set.of(idFactory.of("forgero:template_tag")),
				new PartTemplateStructureData(
						"forgero:{material.name}-{shape.name}_no_host",
						Map.of("material", new PartTemplateStructureSlotData(idFactory.of("forgero:tool_material"), null, null),
								"shape", new PartTemplateStructureSlotData(idFactory.of("forgero:pickaxe_head_shape"), null, null))
				),
				null,
				null,
				null
		);

		var pickaxeHeadTemplateWithHost = new NormalizedState.NormalizedPartTemplate(
				idFactory.of("forgero:pickaxe_head_template_with_host"),
				"Pickaxe Head With Host",
				Set.of(idFactory.of("forgero:template_tag")),
				new PartTemplateStructureData(
						"forgero:{material.name}-{shape.name}_with_host",
						Map.of("material", new PartTemplateStructureSlotData(idFactory.of("forgero:tool_material"), null, null),
								"shape", new PartTemplateStructureSlotData(idFactory.of("forgero:pickaxe_head_shape"), null, null))
				),
				new HostTemplateData(null, new CreateTemplateData("forgero:item/{material.name}_{shape.name}", "forgero:custom_part", "forgero:parts_group")),
				null,
				null
		);


		state = new NormalizedState(
				Map.of(ironMaterial.id(), ironMaterial),
				Map.of(pickaxeHeadShape.id(), pickaxeHeadShape),
				Collections.emptyMap(),
				Map.of(pickaxeHeadTemplateNoHost.id(), pickaxeHeadTemplateNoHost,
						pickaxeHeadTemplateWithHost.id(), pickaxeHeadTemplateWithHost),
				Collections.emptyMap(),
				Collections.emptyMap()
		);
	}

	@Test
	void generatedPartWithoutHostTemplateGetsDefaultHostData() {
		GeneratedState result = generator.generate(state, tagGraph);
		OpenIdentifier generatedId = idFactory.of("forgero:iron-pickaxe_head_shape_no_host");

		assertTrue(result.parts().containsKey(generatedId));
		GeneratedState.GeneratedPart part = result.parts().get(generatedId);

		HostData host = part.host();
		assertNotNull(host);
		assertNull(host.identifiers());
		assertNotNull(host.create());
		assertEquals(generatedId, host.create().id());
		assertEquals("forgero:part_item", host.create().itemClass());
		assertNull(host.create().item_group());
	}

	@Test
	void generatedPartWithHostTemplateIsResolvedCorrectly() {
		GeneratedState result = generator.generate(state, tagGraph);
		OpenIdentifier generatedId = idFactory.of("forgero:iron-pickaxe_head_shape_with_host");

		assertTrue(result.parts().containsKey(generatedId));
		GeneratedState.GeneratedPart part = result.parts().get(generatedId);

		HostData host = part.host();
		assertNotNull(host);
		assertNull(host.identifiers());
		assertNotNull(host.create());

		OpenIdentifier expectedHostId = idFactory.of("forgero:item/iron_pickaxe_head_shape");
		assertEquals(expectedHostId, host.create().id());
		assertEquals("forgero:custom_part", host.create().itemClass());
		assertEquals("forgero:parts_group", host.create().item_group());
	}

	@Test
	void generatedEquipmentWithDefaultHostData() {
		// Setup equipment template in a new state
		var headId = idFactory.of("forgero:iron-pickaxe_head_shape_no_host");
		var staticPart = new NormalizedState.NormalizedStaticPart(idFactory.of("forgero:oak_handle"), "Oak Handle", Collections.emptySet(), null, null, null);

		var equipmentTemplate = new NormalizedState.NormalizedEquipmentTemplate(
				idFactory.of("forgero:pickaxe_template"),
				"Pickaxe Template",
				Collections.emptySet(),
				new EquipmentTemplateStructureData("forgero:{head.name}-pickaxe", Map.of("head", new EquipmentTemplateSlotData(idFactory.of("forgero:parts/pickaxe_head_type"), null, headId))),
				null, null, null
		);

		var stateWithEquipment = new NormalizedState(state.materials(), state.shapes(), state.schematics(), state.partTemplates(), Map.of(equipmentTemplate.id(), equipmentTemplate), Map.of(staticPart.id(), staticPart));

		// Update tag graph for this test
		tagRelationships.put(idFactory.of("forgero:parts/pickaxe_head_type"), Collections.emptySet());
		tagRelationships.put(idFactory.of("forgero:template_tag"), Set.of(idFactory.of("forgero:parts/pickaxe_head_type")));
		TagGraph newTagGraph = new TagGraph(tagRelationships);


		GeneratedState result = generator.generate(stateWithEquipment, newTagGraph);

		OpenIdentifier expectedEquipmentId = idFactory.of("forgero:iron_pickaxe_head_shape_no_host-pickaxe");
		assertTrue(result.equipment().containsKey(expectedEquipmentId), "Generated equipment should contain the expected ID, but was: " + result.equipment().keySet());

		GeneratedState.GeneratedEquipment equipment = result.equipment().get(expectedEquipmentId);
		HostData host = equipment.host();

		assertNotNull(host);
		assertNotNull(host.create());
		assertEquals(expectedEquipmentId, host.create().id());
		assertEquals("forgero:tool_item", host.create().itemClass());
	}
}
