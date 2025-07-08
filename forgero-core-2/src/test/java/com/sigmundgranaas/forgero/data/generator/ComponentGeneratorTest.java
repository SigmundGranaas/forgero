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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
		tagBuilder.add(idFactory.of("forgero:default_shape"), Set.of(idFactory.of("forgero:shape")));
		tagBuilder.add(idFactory.of("forgero:metal"), Set.of(idFactory.of("forgero:tool_material")));
		tagBuilder.add(idFactory.of("forgero:wood"), Set.of(idFactory.of("forgero:material"))); // For handle materials
		tagBuilder.add(idFactory.of("forgero:handle_material"), Set.of(idFactory.of("forgero:material")));


		tagBuilder.add(idFactory.of("forgero:pickaxe_head_type"), Set.of());
		tagBuilder.add(idFactory.of("forgero:handle_type"), Set.of());
		tagBuilder.add(idFactory.of("forgero:tool"), Set.of());
		tagBuilder.add(idFactory.of("forgero:pickaxe_tool"), Set.of(idFactory.of("forgero:tool")));


		tagGraph = tagBuilder.build();
	}

	@Test
	void testGenerate_SimplePartCombination() {
		// Input NormalizedState
		var iron = new NormalizedState.NormalizedMaterial(
				idFactory.of("forgero:iron"), "Iron", Set.of(idFactory.of("forgero:metal")), List.of(), List.of()
		);
		var roundShape = new NormalizedState.NormalizedShape(
				idFactory.of("forgero:round_shape"), "Round", Set.of(idFactory.of("forgero:default_shape")), List.of(), List.of()
		);
		var headTemplate = new NormalizedState.NormalizedPartTemplate(
				idFactory.of("forgero:pickaxe_head_template"), "Pickaxe Head", Set.of(idFactory.of("forgero:pickaxe_head_type")),
				idFactory.of("forgero:tool_material"), // Material type tag
				idFactory.of("forgero:default_shape"), // Shape type tag
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
		OpenIdentifier expectedId = idFactory.of("iron_round_pickaxe_head"); // Based on new naming convention
		assertTrue(generated.parts().containsKey(expectedId));

		var generatedPart = generated.parts().get(expectedId);
		assertNotNull(generatedPart);
		assertEquals("Iron Round Pickaxe Head", generatedPart.name());
		assertTrue(generatedPart.tags().contains(idFactory.of("forgero:metal"))); // From material
		assertTrue(generatedPart.tags().contains(idFactory.of("forgero:default_shape"))); // From shape
		assertTrue(generatedPart.tags().contains(idFactory.of("forgero:pickaxe_head_type"))); // From template

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
				idFactory.of("forgero:pickaxe_head_template"), "Pickaxe Head Template", Set.of(idFactory.of("forgero:pickaxe_head_type")),
				idFactory.of("forgero:tool_material"), idFactory.of("forgero:default_shape"), List.of()
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

		OpenIdentifier generatedPartId = idFactory.of("iron_pickaxe_head_shape_pickaxe_head_template");
		assertTrue(generated.parts().containsKey(generatedPartId));
		var generatedPart = generated.parts().get(generatedPartId);
		assertNotNull(generatedPart.attributes());
		assertEquals(4, generatedPart.attributes().size()); // 2 from material, 2 from shape
	}

	@Test
	void testGenerate_ToolWithAllDefaultSlots() {
		// Prepare Normalized Parts and Tool Template
		var ironHead = new NormalizedState.NormalizedStaticPart(idFactory.of("forgero:iron_pickaxe_head"), "Iron Pickaxe Head", Set.of(idFactory.of("forgero:pickaxe_head_type")), List.of(), List.of(), List.of());
		var oakHandle = new NormalizedState.NormalizedStaticPart(idFactory.of("forgero:oak_handle"), "Oak Handle", Set.of(idFactory.of("forgero:handle_type")), List.of(), List.of(), List.of());

		var pickaxeTemplate = new NormalizedState.NormalizedEquipmentTemplate(
				idFactory.of("forgero:pickaxe_template"), "Pickaxe", Set.of(idFactory.of("forgero:pickaxe_tool")),
				Map.of(
						"head", new EquipmentTemplateSlotData(idFactory.of("forgero:pickaxe_head_type"), ironHead.id()),
						"handle", new EquipmentTemplateSlotData(idFactory.of("forgero:handle_type"), oakHandle.id())
				), List.of()
		);

		NormalizedState state = new NormalizedState(
				Map.of(), Map.of(), Map.of(), Map.of(),
				Map.of(pickaxeTemplate.id(), pickaxeTemplate),
				Map.of(ironHead.id(), ironHead, oakHandle.id(), oakHandle)
		);

		// Action
		GeneratedState generated = generator.generate(state, tagGraph);

		// Assertions
		assertEquals(1, generated.equipment().size());
		OpenIdentifier expectedToolId = idFactory.of("pickaxe-iron_pickaxe_head-oak_handle");
		assertTrue(generated.equipment().containsKey(expectedToolId));

		var generatedTool = generated.equipment().get(expectedToolId);
		assertTrue(generatedTool.tags().contains(idFactory.of("forgero:pickaxe_tool")));
		assertEquals(ironHead.id(), generatedTool.structure().get("head"));
		assertEquals(oakHandle.id(), generatedTool.structure().get("handle"));
	}

	@Test
	void testGenerate_ToolWithOneCombinatorialSlot() {
		// Prepare
		var ironHead = new NormalizedState.NormalizedStaticPart(idFactory.of("forgero:iron_pickaxe_head"), "Iron Pickaxe Head", Set.of(idFactory.of("forgero:pickaxe_head_type")), List.of(), List.of(), List.of());
		var diamondHead = new NormalizedState.NormalizedStaticPart(idFactory.of("forgero:diamond_pickaxe_head"), "Diamond Pickaxe Head", Set.of(idFactory.of("forgero:pickaxe_head_type")), List.of(), List.of(), List.of());
		var oakHandle = new NormalizedState.NormalizedStaticPart(idFactory.of("forgero:oak_handle"), "Oak Handle", Set.of(idFactory.of("forgero:handle_type")), List.of(), List.of(), List.of());

		var pickaxeTemplate = new NormalizedState.NormalizedEquipmentTemplate(
				idFactory.of("forgero:pickaxe_template"), "Pickaxe", Set.of(idFactory.of("forgero:pickaxe_tool")),
				Map.of(
						"head", new EquipmentTemplateSlotData(idFactory.of("forgero:pickaxe_head_type"), null), // Combinatorial
						"handle", new EquipmentTemplateSlotData(idFactory.of("forgero:handle_type"), oakHandle.id()) // Default
				), List.of()
		);

		NormalizedState state = new NormalizedState(
				Map.of(), Map.of(), Map.of(), Map.of(),
				Map.of(pickaxeTemplate.id(), pickaxeTemplate),
				Map.of(ironHead.id(), ironHead, diamondHead.id(), diamondHead, oakHandle.id(), oakHandle)
		);

		// Action
		GeneratedState generated = generator.generate(state, tagGraph);

		// Assertions (2 tools + 3 static parts = 5 total in generated)
		assertEquals(2, generated.equipment().size());
		assertTrue(generated.equipment().containsKey(idFactory.of("pickaxe-iron_pickaxe_head-oak_handle")));
		assertTrue(generated.equipment().containsKey(idFactory.of("pickaxe-diamond_pickaxe_head-oak_handle")));
	}

	@Test
	void testGenerate_ToolWithMultipleCombinatorialSlots() {
		// Prepare
		var ironHead = new NormalizedState.NormalizedStaticPart(idFactory.of("forgero:iron_pickaxe_head"), "Iron Pickaxe Head", Set.of(idFactory.of("forgero:pickaxe_head_type")), List.of(), List.of(), List.of());
		var diamondHead = new NormalizedState.NormalizedStaticPart(idFactory.of("forgero:diamond_pickaxe_head"), "Diamond Pickaxe Head", Set.of(idFactory.of("forgero:pickaxe_head_type")), List.of(), List.of(), List.of());
		var oakHandle = new NormalizedState.NormalizedStaticPart(idFactory.of("forgero:oak_handle"), "Oak Handle", Set.of(idFactory.of("forgero:handle_type")), List.of(), List.of(), List.of());
		var birchHandle = new NormalizedState.NormalizedStaticPart(idFactory.of("forgero:birch_handle"), "Birch Handle", Set.of(idFactory.of("forgero:handle_type")), List.of(), List.of(), List.of());

		var pickaxeTemplate = new NormalizedState.NormalizedEquipmentTemplate(
				idFactory.of("forgero:pickaxe_template"), "Pickaxe", Set.of(idFactory.of("forgero:pickaxe_tool")),
				Map.of(
						"head", new EquipmentTemplateSlotData(idFactory.of("forgero:pickaxe_head_type"), null), // Combinatorial
						"handle", new EquipmentTemplateSlotData(idFactory.of("forgero:handle_type"), null) // Combinatorial
				), List.of()
		);

		NormalizedState state = new NormalizedState(
				Map.of(), Map.of(), Map.of(), Map.of(),
				Map.of(pickaxeTemplate.id(), pickaxeTemplate),
				Map.of(
						ironHead.id(), ironHead,
						diamondHead.id(), diamondHead,
						oakHandle.id(), oakHandle,
						birchHandle.id(), birchHandle
				)
		);

		// Action
		GeneratedState generated = generator.generate(state, tagGraph);

		// Assertions (2 heads * 2 handles = 4 tools)
		assertEquals(4, generated.equipment().size());
		assertTrue(generated.equipment().containsKey(idFactory.of("pickaxe-birch_handle-diamond_pickaxe_head")));
		assertTrue(generated.equipment().containsKey(idFactory.of("pickaxe-birch_handle-iron_pickaxe_head")));
		assertTrue(generated.equipment().containsKey(idFactory.of("pickaxe-diamond_pickaxe_head-oak_handle")));
		assertTrue(generated.equipment().containsKey(idFactory.of("pickaxe-iron_pickaxe_head-oak_handle")));
	}

	@Test
	void testGenerate_MissingMatchingPartsForCombinatorialSlot() {
		// Only heads are available, no handles matching the handle_type tag
		var ironHead = new NormalizedState.NormalizedStaticPart(idFactory.of("forgero:iron_pickaxe_head"), "Iron Pickaxe Head", Set.of(idFactory.of("forgero:pickaxe_head_type")), List.of(), List.of(), List.of());

		var pickaxeTemplate = new NormalizedState.NormalizedEquipmentTemplate(
				idFactory.of("forgero:pickaxe_template"), "Pickaxe", Set.of(idFactory.of("forgero:pickaxe_tool")),
				Map.of(
						"head", new EquipmentTemplateSlotData(idFactory.of("forgero:pickaxe_head_type"), null),
						"handle", new EquipmentTemplateSlotData(idFactory.of("forgero:handle_type"), null)
				), List.of()
		);

		NormalizedState state = new NormalizedState(
				Map.of(), Map.of(), Map.of(), Map.of(),
				Map.of(pickaxeTemplate.id(), pickaxeTemplate),
				Map.of(ironHead.id(), ironHead)
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
		var roundShape = new NormalizedState.NormalizedShape(idFactory.of("forgero:round"), "Round", Set.of(idFactory.of("forgero:default_shape")), List.of(), null);

		var headTemplate = new NormalizedState.NormalizedPartTemplate(
				idFactory.of("forgero:pickaxe_head_template"), "Pickaxe Head", Set.of(idFactory.of("forgero:pickaxe_head_type")),
				idFactory.of("forgero:tool_material"), idFactory.of("forgero:default_shape"), List.of()
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
		assertTrue(generated.parts().containsKey(idFactory.of("iron_round_pickaxe_head")));
		assertFalse(generated.parts().containsKey(idFactory.of("oak_round_pickaxe_head")));
	}
}
