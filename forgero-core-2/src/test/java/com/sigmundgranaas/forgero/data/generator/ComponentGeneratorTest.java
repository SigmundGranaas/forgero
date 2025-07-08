package com.sigmundgranaas.forgero.data.generator;

import com.sigmundgranaas.forgero.core.ForgeroTest;
import com.sigmundgranaas.forgero.core.data.definition.GeneratedState;
import com.sigmundgranaas.forgero.core.data.definition.NormalizedState;
import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.tags.engine.TagGraph;
import com.sigmundgranaas.forgero.core.tags.engine.TagGraphBuilder;
import com.sigmundgranaas.forgero.data.v3.dto.attribute.AttributeDataImpl;
import com.sigmundgranaas.forgero.data.v3.dto.attribute.ComputationData;
import com.sigmundgranaas.forgero.data.v3.dto.template.EquipmentTemplateSlotData;
import com.sigmundgranaas.forgero.data.v3.dto.template.EquipmentTemplateStructureData;
import com.sigmundgranaas.forgero.data.v3.dto.template.PartTemplateStructureData;
import com.sigmundgranaas.forgero.data.v3.dto.template.PartTemplateStructureSlotData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.sigmundgranaas.forgero.data.v3.codec.AttributeCodecs.ADDITION_OPERATOR;
import static com.sigmundgranaas.forgero.data.v3.codec.AttributeCodecs.MULTIPLICATION_OPERATOR;
import static org.junit.jupiter.api.Assertions.*;

class ComponentGeneratorTest extends ForgeroTest {

	private ComponentGenerator generator;

	private TagGraph tagGraph;

	@BeforeEach
	void setUp() {
		generator = new ComponentGeneratorImpl(idFactory);

		TagGraphBuilder tagBuilder = new TagGraphBuilder();
		tagBuilder.add(idFactory.of("forgero:material"), Set.of());
		tagBuilder.add(idFactory.of("forgero:shape"), Set.of());
		tagBuilder.add(idFactory.of("forgero:tool_material"), Set.of(idFactory.of("forgero:material")));
		tagBuilder.add(idFactory.of("forgero:default_shape"), Set.of(idFactory.of("forgero:shape"))); // Use a default for testing
		tagBuilder.add(idFactory.of("forgero:metal"), Set.of(idFactory.of("forgero:tool_material")));
		tagBuilder.add(idFactory.of("forgero:wood"), Set.of(idFactory.of("forgero:material"))); // For handle materials
		tagBuilder.add(idFactory.of("forgero:handle_material"), Set.of(idFactory.of("forgero:material")));


		tagBuilder.add(idFactory.of("forgero:parts/pickaxe_head_type"), Set.of());
		tagBuilder.add(idFactory.of("forgero:handle_type"), Set.of());
		tagBuilder.add(idFactory.of("forgero:tool"), Set.of());
		tagBuilder.add(idFactory.of("forgero:pickaxe"), Set.of(idFactory.of("forgero:tool")));


		tagGraph = tagBuilder.build();
	}

	@Test
	void testGenerate_SimplePartCombination() {
		// Input NormalizedState
		var iron = new NormalizedState.NormalizedMaterial(
				idFactory.of("forgero:iron"), "iron", Set.of(idFactory.of("forgero:metal")), List.of(), List.of()
		);
		var roundShape = new NormalizedState.NormalizedShape(
				idFactory.of("forgero:round_shape"), "round_shape", Set.of(idFactory.of("forgero:default_shape")), List.of(), List.of()
		);
		var headTemplate = new NormalizedState.NormalizedPartTemplate(
				idFactory.of("forgero:pickaxe_head_template"), "Pickaxe Head", Set.of(idFactory.of("forgero:parts/pickaxe_head_type")),
				new PartTemplateStructureData("forgero:{material.name}-{shape.name}_head",
						Map.of(
								"material", new PartTemplateStructureSlotData(idFactory.of("forgero:tool_material"), 0, null),
								"shape", new PartTemplateStructureSlotData(idFactory.of("forgero:default_shape"), 0, null)
						)),
				 // ID pattern for testing
				List.of()
		);

		NormalizedState state = new NormalizedState(
				Map.of(iron.id(), iron),
				Map.of(roundShape.id(), roundShape),
				Map.of(),
				Map.of(headTemplate.id(), headTemplate),
				Map.of(),
				Map.of()
		);

		// Action
		GeneratedState generated = generator.generate(state, tagGraph);

		// Assertion
		assertEquals(1, generated.parts().size());
		OpenIdentifier expectedId = idFactory.of("forgero:iron-round_shape_head"); // Based on the new structure.id in JSON
		assertTrue(generated.parts().containsKey(expectedId), "Generated part ID should be " + expectedId + " but was " + generated.parts().keySet());

		var generatedPart = generated.parts().get(expectedId);
		assertNotNull(generatedPart);

		assertEquals("iron-round_shape_head", generatedPart.id().name()); // Now reflects proper naming based on default in ComponentGeneratorImpl
		assertTrue(generatedPart.tags().contains(idFactory.of("forgero:metal"))); // From material
		assertTrue(generatedPart.tags().contains(idFactory.of("forgero:default_shape"))); // From shape
		assertTrue(generatedPart.tags().contains(idFactory.of("forgero:parts/pickaxe_head_type"))); // From template

		assertEquals(iron.id(), generatedPart.materialId());
		assertEquals(roundShape.id(), generatedPart.shapeId());
	}

	@Test
	void testGenerate_PartWithCompositeAttributes() {
		var iron = new NormalizedState.NormalizedMaterial(
				idFactory.of("forgero:iron"), "Iron", Set.of(idFactory.of("forgero:metal")),
				List.of(
						new AttributeDataImpl(idFactory.of("iron-durability"), idFactory.of("forgero:durability"), new ComputationData(250f, ADDITION_OPERATOR, "forgero:base"), null, null),
						new AttributeDataImpl(idFactory.of("iron-mining_speed_comp"), idFactory.of("forgero:mining_speed"), new ComputationData(5f, ADDITION_OPERATOR, "forgero:base"), null, idFactory.of("forgero:material-mining-speed"))
				), null
		);
		var headShape = new NormalizedState.NormalizedShape(
				idFactory.of("forgero:pickaxe_head_shape"), "Pickaxe Head Shape", Set.of(idFactory.of("forgero:default_shape")),
				List.of(
						new AttributeDataImpl(idFactory.of("shape-durability-mult"), idFactory.of("forgero:durability"), new ComputationData(1.5f, MULTIPLICATION_OPERATOR, "forgero:middle"), null, null),
						new AttributeDataImpl(idFactory.of("shape-mining_speed-mult"), idFactory.of("forgero:mining_speed"), new ComputationData(1.2f, MULTIPLICATION_OPERATOR, "forgero:middle"), null, idFactory.of("forgero:material-mining-speed"))
				), null
		);


		var headTemplate = new NormalizedState.NormalizedPartTemplate(
				idFactory.of("forgero:pickaxe_head_template"), "Pickaxe Head Template", Set.of(idFactory.of("forgero:parts/pickaxe_head_type")),
				new PartTemplateStructureData("forgero:{material.name}-{shape.name}_head",
						Map.of(
								"material", new PartTemplateStructureSlotData(idFactory.of("forgero:tool_material"), 0, null),
								"shape", new PartTemplateStructureSlotData(idFactory.of("forgero:default_shape"), 0, null)
						)),
				List.of()
		);

		NormalizedState state = new NormalizedState(
				Map.of(iron.id(), iron),
				Map.of(headShape.id(), headShape),
				Map.of(),
				Map.of(headTemplate.id(), headTemplate),
				Map.of(),
				Map.of()
		);

		GeneratedState generated = generator.generate(state, tagGraph);

		OpenIdentifier generatedPartId = idFactory.of("forgero:iron-pickaxe_head_shape_head");
		assertTrue(generated.parts().containsKey(generatedPartId));
		var generatedPart = generated.parts().get(generatedPartId);
		assertNotNull(generatedPart.attributes());
		assertEquals(4, generatedPart.attributes().size()); // 2 from material, 2 from shape
	}

	@Test
	void testGenerate_ToolWithAllDefaultSlots() {
		// Prepare Normalized Parts and Tool Template
		// Need to create a generated head first, as tools consume generated parts for complex IDs
		var iron = new NormalizedState.NormalizedMaterial(
				idFactory.of("forgero:iron"), "Iron", Set.of(idFactory.of("forgero:metal")), List.of(), List.of()
		);
		var headShape = new NormalizedState.NormalizedShape(
				idFactory.of("forgero:pickaxe_head_shape"), "Pickaxe Head Shape", Set.of(idFactory.of("forgero:default_shape")), List.of(), List.of()
		);
		var headTemplate = new NormalizedState.NormalizedPartTemplate(
				idFactory.of("forgero:pickaxe_head_template"), "Pickaxe Head", Set.of(idFactory.of("forgero:parts/pickaxe_head_type")),
				new PartTemplateStructureData("forgero:{material.name}-{shape.name}_head",
						Map.of(
								"material", new PartTemplateStructureSlotData(idFactory.of("forgero:tool_material"), 0, null),
								"shape", new PartTemplateStructureSlotData(idFactory.of("forgero:default_shape"), 0, null)
						)),
				List.of()
		);

		// Manually create a generated part that would result from the above
		var generatedIronHead = new GeneratedState.GeneratedPart(
				idFactory.of("forgero:iron-pickaxe_head_shape_head"), Set.of(idFactory.of("forgero:parts/pickaxe_head_type"), idFactory.of("forgero:metal"), idFactory.of("forgero:default_shape")),
				iron.id(), headShape.id(), List.of(), List.of(), List.of()
		);

		var oakHandle = new NormalizedState.NormalizedStaticPart(idFactory.of("forgero:static_oak_handle"), "Oak Handle", Set.of(idFactory.of("forgero:parts/handle_type")), List.of(), List.of(), List.of());

		var pickaxeTemplate = new NormalizedState.NormalizedEquipmentTemplate(
				idFactory.of("forgero:pickaxe_template"), "Pickaxe", Set.of(idFactory.of("forgero:pickaxe")),
				new EquipmentTemplateStructureData("forgero:{head.material.name}-pickaxe", // ID pattern
						Map.of(
								"head", new EquipmentTemplateSlotData(idFactory.of("forgero:parts/pickaxe_head_type"), generatedIronHead.id()), // Use the generated head's ID
								"handle", new EquipmentTemplateSlotData(idFactory.of("forgero:parts/handle_type"), oakHandle.id())
						)), List.of()
		);

		NormalizedState state = new NormalizedState(
				Map.of(iron.id(), iron),
				Map.of(headShape.id(), headShape),
				Map.of(),
				Map.of(headTemplate.id(), headTemplate),
				Map.of(pickaxeTemplate.id(), pickaxeTemplate),
				Map.of(oakHandle.id(), oakHandle)
		);
		// Need to ensure generatedPartsCache is pre-populated for accurate resolution
		// This is normally handled internally by the generator, but for specific test setup,
		// we are simulating the two-stage generation manually.
		ComponentGeneratorImpl generatorImpl = (ComponentGeneratorImpl) generator;
		Map<OpenIdentifier, GeneratedState.GeneratedPart> preGeneratedParts = new HashMap<>();
		preGeneratedParts.put(generatedIronHead.id(), generatedIronHead);
		// Simulate the internal state for the second part of generation
		// Note: The actual `generate` method creates its own internal IdResolver, so this test's manual IdResolver setup might not be fully accurate for state transfer.
		// However, the `generate` method correctly passes a mutable map for parts.

		// Action
		GeneratedState generated = generator.generate(state, tagGraph);

		// Assertions
		assertEquals(1, generated.equipment().size());
		OpenIdentifier expectedToolId = idFactory.of("forgero:iron-pickaxe"); // Based on new ID pattern: {head.material.name}-pickaxe -> iron-pickaxe
		assertTrue(generated.equipment().containsKey(expectedToolId), "Generated tool ID should be " + expectedToolId + " but was " + generated.equipment().keySet());

		var generatedTool = generated.equipment().get(expectedToolId);
		assertTrue(generatedTool.tags().contains(idFactory.of("forgero:pickaxe")));
		assertEquals(generatedIronHead.id(), generatedTool.structure().get("head"));
		assertEquals(oakHandle.id(), generatedTool.structure().get("handle"));
	}

	@Test
	void testGenerate_ToolWithOneCombinatorialSlot() {
		// Prepare
		var iron = new NormalizedState.NormalizedMaterial(idFactory.of("forgero:iron"), "Iron", Set.of(idFactory.of("forgero:metal")), List.of(), List.of());
		var diamond = new NormalizedState.NormalizedMaterial(idFactory.of("forgero:diamond"), "Diamond", Set.of(idFactory.of("forgero:metal")), List.of(), List.of());
		var headShape = new NormalizedState.NormalizedShape(idFactory.of("forgero:pickaxe_head_shape"), "Pickaxe Head Shape", Set.of(idFactory.of("forgero:default_shape")), List.of(), List.of());

		var headTemplate = new NormalizedState.NormalizedPartTemplate(
				idFactory.of("forgero:pickaxe_head_template"), "Pickaxe Head", Set.of(idFactory.of("forgero:parts/pickaxe_head_type")),
				new PartTemplateStructureData("forgero:{material.name}-{shape.name}_head",
						Map.of(
								"material", new PartTemplateStructureSlotData(idFactory.of("forgero:tool_material"), 0, null),
								"shape", new PartTemplateStructureSlotData(idFactory.of("forgero:default_shape"), 0, null)
						)),
				List.of()
		);

		var oakHandle = new NormalizedState.NormalizedStaticPart(idFactory.of("forgero:static_oak_handle"), "Oak Handle", Set.of(idFactory.of("forgero:parts/handle_type")), List.of(), List.of(), List.of());

		var pickaxeTemplate = new NormalizedState.NormalizedEquipmentTemplate(
				idFactory.of("forgero:pickaxe_template"), "Pickaxe", Set.of(idFactory.of("forgero:pickaxe")),
				new EquipmentTemplateStructureData("forgero:{head.material.name}-pickaxe", // ID pattern
						Map.of(
								"head", new EquipmentTemplateSlotData(idFactory.of("forgero:parts/pickaxe_head_type"), null), // Combinatorial
								"handle", new EquipmentTemplateSlotData(idFactory.of("forgero:parts/handle_type"), oakHandle.id()) // Default
						)), List.of()
		);

		NormalizedState state = new NormalizedState(
				Map.of(iron.id(), iron, diamond.id(), diamond),
				Map.of(headShape.id(), headShape),
				Map.of(),
				Map.of(headTemplate.id(), headTemplate),
				Map.of(pickaxeTemplate.id(), pickaxeTemplate),
				Map.of(oakHandle.id(), oakHandle)
		);

		// Action
		GeneratedState generated = generator.generate(state, tagGraph);

		// Assertions (2 tool templates -> 2 tools)
		assertEquals(2, generated.equipment().size());
		// IDs should be based on the head material name
		assertTrue(generated.equipment().containsKey(idFactory.of("forgero:iron-pickaxe")), "Iron Pickaxe should be generated.");
		assertTrue(generated.equipment().containsKey(idFactory.of("forgero:diamond-pickaxe")), "Diamond Pickaxe should be generated.");
	}

	@Test
	void testGenerate_ToolWithMultipleCombinatorialSlots() {
		// Prepare
		var iron = new NormalizedState.NormalizedMaterial(idFactory.of("forgero:iron"), "Iron", Set.of(idFactory.of("forgero:metal")), List.of(), List.of());
		var diamond = new NormalizedState.NormalizedMaterial(idFactory.of("forgero:diamond"), "Diamond", Set.of(idFactory.of("forgero:metal")), List.of(), List.of());
		var headShape = new NormalizedState.NormalizedShape(idFactory.of("forgero:pickaxe_head_shape"), "Pickaxe Head Shape", Set.of(idFactory.of("forgero:default_shape")), List.of(), List.of());
		var headTemplate = new NormalizedState.NormalizedPartTemplate(
				idFactory.of("forgero:pickaxe_head_template"), "Pickaxe Head", Set.of(idFactory.of("forgero:parts/pickaxe_head_type")),
				new PartTemplateStructureData("forgero:{material.name}-{shape.name}_head",
						Map.of(
								"material", new PartTemplateStructureSlotData(idFactory.of("forgero:tool_material"), 0, null),
								"shape", new PartTemplateStructureSlotData(idFactory.of("forgero:default_shape"), 0, null)
						)),
				List.of()
		);

		var oakHandle = new NormalizedState.NormalizedStaticPart(idFactory.of("forgero:oak_handle"), "Oak Handle", Set.of(idFactory.of("forgero:parts/handle_type")), List.of(), List.of(), List.of());
		var birchHandle = new NormalizedState.NormalizedStaticPart(idFactory.of("forgero:birch_handle"), "Birch Handle", Set.of(idFactory.of("forgero:parts/handle_type")), List.of(), List.of(), List.of());

		var pickaxeTemplate = new NormalizedState.NormalizedEquipmentTemplate(
				idFactory.of("forgero:pickaxe_template"), "Pickaxe", Set.of(idFactory.of("forgero:pickaxe")),
				new EquipmentTemplateStructureData("forgero:{head.material.name}-{handle.name}-pickaxe", // More complex ID pattern
						Map.of(
								"head", new EquipmentTemplateSlotData(idFactory.of("forgero:parts/pickaxe_head_type"), null), // Combinatorial
								"handle", new EquipmentTemplateSlotData(idFactory.of("forgero:parts/handle_type"), null) // Combinatorial
						)), List.of()
		);

		NormalizedState state = new NormalizedState(
				Map.of(iron.id(), iron, diamond.id(), diamond),
				Map.of(headShape.id(), headShape),
				Map.of(),
				Map.of(headTemplate.id(), headTemplate),
				Map.of(pickaxeTemplate.id(), pickaxeTemplate),
				Map.of(
						oakHandle.id(), oakHandle,
						birchHandle.id(), birchHandle
				)
		);

		// Action
		GeneratedState generated = generator.generate(state, tagGraph);

		// Assertions (2 heads * 2 handles = 4 tools)
		assertEquals(4, generated.equipment().size());
		assertTrue(generated.equipment().containsKey(idFactory.of("forgero:iron-oak_handle-pickaxe")));
		assertTrue(generated.equipment().containsKey(idFactory.of("forgero:iron-birch_handle-pickaxe")));
		assertTrue(generated.equipment().containsKey(idFactory.of("forgero:diamond-oak_handle-pickaxe")));
		assertTrue(generated.equipment().containsKey(idFactory.of("forgero:diamond-birch_handle-pickaxe")));
	}

	@Test
	void testGenerate_MissingMatchingPartsForCombinatorialSlot() {
		// Only heads are available, no handles matching the handle_type tag
		var iron = new NormalizedState.NormalizedMaterial(idFactory.of("forgero:iron"), "Iron", Set.of(idFactory.of("forgero:metal")), List.of(), List.of());
		var headShape = new NormalizedState.NormalizedShape(idFactory.of("forgero:pickaxe_head_shape"), "Pickaxe Head Shape", Set.of(idFactory.of("forgero:default_shape")), List.of(), List.of());
		var headTemplate = new NormalizedState.NormalizedPartTemplate(
				idFactory.of("forgero:pickaxe_head_template"), "Pickaxe Head", Set.of(idFactory.of("forgero:parts/pickaxe_head_type")),
				new PartTemplateStructureData("forgero:{material.name}-{shape.name}_head",
						Map.of(
								"material", new PartTemplateStructureSlotData(idFactory.of("forgero:tool_material"), 0, null),
								"shape", new PartTemplateStructureSlotData(idFactory.of("forgero:default_shape"), 0, null)
						)),
				List.of()
		);

		var pickaxeTemplate = new NormalizedState.NormalizedEquipmentTemplate(
				idFactory.of("forgero:pickaxe_template"), "Pickaxe", Set.of(idFactory.of("forgero:pickaxe")),
				new EquipmentTemplateStructureData("forgero:{head.material.name}-pickaxe", // ID pattern
						Map.of(
								"head", new EquipmentTemplateSlotData(idFactory.of("forgero:parts/pickaxe_head_type"), null),
								"handle", new EquipmentTemplateSlotData(idFactory.of("forgero:parts/handle_type"), null)
						)), List.of()
		);

		NormalizedState state = new NormalizedState(
				Map.of(iron.id(), iron),
				Map.of(headShape.id(), headShape),
				Map.of(),
				Map.of(headTemplate.id(), headTemplate),
				Map.of(pickaxeTemplate.id(), pickaxeTemplate),
				Map.of() // No static parts available for handle
		);

		// Action
		GeneratedState generated = generator.generate(state, tagGraph);

		// Assertion: No tools should be generated because the 'handle' slot cannot be filled
		assertTrue(generated.equipment().isEmpty());
	}

	@Test
	void testGenerate_PartIncludesExcludedMaterialTypes() {
		var iron = new NormalizedState.NormalizedMaterial(idFactory.of("forgero:iron"), "Iron", Set.of(idFactory.of("forgero:metal")), List.of(), null);
		var wood = new NormalizedState.NormalizedMaterial(idFactory.of("forgero:oak"), "Oak", Set.of(idFactory.of("forgero:wood")), List.of(), null);
		var roundShape = new NormalizedState.NormalizedShape(idFactory.of("forgero:round_shape"), "round_shape", Set.of(idFactory.of("forgero:default_shape")), List.of(), null);

		var headTemplate = new NormalizedState.NormalizedPartTemplate(
				idFactory.of("forgero:pickaxe_head_template"), "Pickaxe Head", Set.of(idFactory.of("forgero:parts/pickaxe_head_type")),
				new PartTemplateStructureData("forgero:{material.name}-{shape.name}_head",
						Map.of(
								"material", new PartTemplateStructureSlotData(idFactory.of("forgero:tool_material"), 0, null),
								"shape", new PartTemplateStructureSlotData(idFactory.of("forgero:default_shape"), 0, null)
						)),
				List.of()
		);

		NormalizedState state = new NormalizedState(
				Map.of(iron.id(), iron, wood.id(), wood),
				Map.of(roundShape.id(), roundShape),
				Map.of(),
				Map.of(headTemplate.id(), headTemplate),
				Map.of(),
				Map.of()
		);

		// Action
		GeneratedState generated = generator.generate(state, tagGraph);

		assertEquals(1, generated.parts().size());
		assertTrue(generated.parts().containsKey(idFactory.of("forgero:iron-round_shape_head")), generated.parts().toString());
		assertFalse(generated.parts().containsKey(idFactory.of("forgero:oak-round_shape_head")), generated.parts().toString());
	}
}
