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
		tagBuilder.add(idFactory.of("forgero:pickaxe_head_shape"), Set.of(idFactory.of("forgero:shape")));
		tagBuilder.add(idFactory.of("forgero:metal"), Set.of(idFactory.of("forgero:tool_material")));
		tagBuilder.add(idFactory.of("forgero:wood"), Set.of(idFactory.of("forgero:material")));

		// Add default tags to the graph
		tagBuilder.add(idFactory.of("forgero:default_pickaxe_head"), Set.of());
		tagBuilder.add(idFactory.of("forgero:default_handle"), Set.of());


		tagBuilder.add(idFactory.of("forgero:parts/pickaxe_head_type"), Set.of());
		tagBuilder.add(idFactory.of("forgero:parts/handle_type"), Set.of());
		tagBuilder.add(idFactory.of("forgero:tool"), Set.of());
		tagBuilder.add(idFactory.of("forgero:pickaxe"), Set.of(idFactory.of("forgero:tool")));

		tagGraph = tagBuilder.build();
	}

	@Test
	void testGenerate_SimplePartCombination() {
		var iron = new NormalizedState.NormalizedMaterial(
				idFactory.of("forgero:iron"), "iron", Set.of(idFactory.of("forgero:metal")), List.of(), List.of()
		);
		var headShape = new NormalizedState.NormalizedShape(
				idFactory.of("forgero:pickaxe_head"), "pickaxe_head", Set.of(idFactory.of("forgero:pickaxe_head_shape")), List.of(), List.of()
		);
		var headTemplate = new NormalizedState.NormalizedPartTemplate(
				idFactory.of("forgero:pickaxe_head_template"), "Pickaxe Head", Set.of(idFactory.of("forgero:parts/pickaxe_head_type")),
				new PartTemplateStructureData("forgero:{material.name}-{shape.name}",
						Map.of(
								"material", new PartTemplateStructureSlotData(idFactory.of("forgero:tool_material"), 1, null),
								"shape", new PartTemplateStructureSlotData(idFactory.of("forgero:pickaxe_head_shape"), 1, null)
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

		assertEquals(1, generated.parts().size());
		OpenIdentifier expectedId = idFactory.of("forgero:iron-pickaxe_head");
		assertTrue(generated.parts().containsKey(expectedId), "Generated part ID should be " + expectedId + " but was " + generated.parts().keySet());
		var generatedPart = generated.parts().get(expectedId);
		assertNotNull(generatedPart);
		assertTrue(generatedPart.tags().contains(idFactory.of("forgero:metal")));
		assertTrue(generatedPart.tags().contains(idFactory.of("forgero:pickaxe_head_shape")));
		assertTrue(generatedPart.tags().contains(idFactory.of("forgero:parts/pickaxe_head_type")));
		assertEquals(iron.id(), generatedPart.materialId());
		assertEquals(headShape.id(), generatedPart.shapeId());
	}

	@Test
	void testGenerate_PartWithCompositeAttributes() {
		var iron = new NormalizedState.NormalizedMaterial(
				idFactory.of("forgero:iron"), "Iron", Set.of(idFactory.of("forgero:metal")),
				List.of(new AttributeDataImpl(idFactory.of("iron-durability"), idFactory.of("forgero:durability"), new ComputationData(250f, ADDITION_OPERATOR, "forgero:base"), null, idFactory.of("forgero:part-composite"))), null
		);
		var headShape = new NormalizedState.NormalizedShape(
				idFactory.of("forgero:pickaxe_head"), "pickaxe_head", Set.of(idFactory.of("forgero:pickaxe_head_shape")),
				List.of(new AttributeDataImpl(idFactory.of("shape-durability-mult"), idFactory.of("forgero:durability"), new ComputationData(1.5f, MULTIPLICATION_OPERATOR, "forgero:middle"), null, idFactory.of("forgero:part-composite"))), null
		);
		var headTemplate = new NormalizedState.NormalizedPartTemplate(
				idFactory.of("forgero:pickaxe_head_template"), "Pickaxe Head Template", Set.of(idFactory.of("forgero:parts/pickaxe_head_type")),
				new PartTemplateStructureData("forgero:{material.name}-{shape.name}",
						Map.of(
								"material", new PartTemplateStructureSlotData(idFactory.of("forgero:tool_material"), 1, null),
								"shape", new PartTemplateStructureSlotData(idFactory.of("forgero:pickaxe_head_shape"), 1, null)
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

		OpenIdentifier generatedPartId = idFactory.of("forgero:iron-pickaxe_head");
		assertTrue(generated.parts().containsKey(generatedPartId));
		var generatedPart = generated.parts().get(generatedPartId);
		assertNotNull(generatedPart.attributes());
		assertEquals(2, generatedPart.attributes().size());
	}


	@Test
	void testGenerate_ToolWithDeclarativeDefaults() {
		var iron = new NormalizedState.NormalizedMaterial(idFactory.of("forgero:iron"), "iron", Set.of(idFactory.of("forgero:metal")), List.of(), List.of());
		var diamond = new NormalizedState.NormalizedMaterial(idFactory.of("forgero:diamond"), "diamond", Set.of(idFactory.of("forgero:metal")), List.of(), List.of());

		// The default head shape must have the default tag
		var headShape = new NormalizedState.NormalizedShape(
				idFactory.of("forgero:pickaxe_head"), "pickaxe_head",
				Set.of(idFactory.of("forgero:pickaxe_head_shape"), idFactory.of("forgero:default_pickaxe_head")), // ADDED default tag
				List.of(), List.of()
		);

		// The template itself should not have the default tag
		var headTemplate = new NormalizedState.NormalizedPartTemplate(
				idFactory.of("forgero:pickaxe_head_template"), "Pickaxe Head", Set.of(idFactory.of("forgero:parts/pickaxe_head_type")),
				new PartTemplateStructureData("forgero:{material.name}-{shape.name}",
						Map.of(
								"material", new PartTemplateStructureSlotData(idFactory.of("forgero:tool_material"), 1, null),
								"shape", new PartTemplateStructureSlotData(idFactory.of("forgero:pickaxe_head_shape"), 1, null)
						)),
				List.of()
		);

		// The default handle must have the default tag
		var oakHandle = new NormalizedState.NormalizedStaticPart(
				idFactory.of("forgero:static_oak_handle"), "Oak Handle",
				Set.of(idFactory.of("forgero:parts/handle_type"), idFactory.of("forgero:default_handle")), // ADDED default tag
				List.of(), List.of(), List.of()
		);

		var pickaxeTemplate = new NormalizedState.NormalizedEquipmentTemplate(
				idFactory.of("forgero:pickaxe_template"), "Pickaxe", Set.of(idFactory.of("forgero:pickaxe")),
				new EquipmentTemplateStructureData("forgero:{head.material.name}-pickaxe",
						Map.of(
								"head", new EquipmentTemplateSlotData(idFactory.of("forgero:parts/pickaxe_head_type"), idFactory.of("forgero:default_pickaxe_head"), null),
								"handle", new EquipmentTemplateSlotData(idFactory.of("forgero:parts/handle_type"), null, idFactory.of("forgero:static_oak_handle"))
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

		GeneratedState generated = generator.generate(state, tagGraph);

		assertEquals(2, generated.equipment().size(), "Should generate one default pickaxe for each material (iron, diamond).");
		OpenIdentifier expectedIronId = idFactory.of("forgero:iron-pickaxe");
		OpenIdentifier expectedDiamondId = idFactory.of("forgero:diamond-pickaxe");
		assertTrue(generated.equipment().containsKey(expectedIronId), "Iron Pickaxe should be generated.");
		assertTrue(generated.equipment().containsKey(expectedDiamondId), "Diamond Pickaxe should be generated.");

		var ironPickaxe = generated.equipment().get(expectedIronId);
		assertEquals(idFactory.of("forgero:iron-pickaxe_head"), ironPickaxe.structure().get("head"));
		assertEquals(idFactory.of("forgero:static_oak_handle"), ironPickaxe.structure().get("handle"));

		var diamondPickaxe = generated.equipment().get(expectedDiamondId);
		assertEquals(idFactory.of("forgero:diamond-pickaxe_head"), diamondPickaxe.structure().get("head"));
		assertEquals(idFactory.of("forgero:static_oak_handle"), diamondPickaxe.structure().get("handle"));
	}

	@Test
	void testGenerate_PartVariantsAreCreatedButNotUsedInDefaultTools() {
		var iron = new NormalizedState.NormalizedMaterial(idFactory.of("forgero:iron"), "iron", Set.of(idFactory.of("forgero:metal")), List.of(), List.of());

		// The default head shape must have the default tag
		var headShape = new NormalizedState.NormalizedShape(
				idFactory.of("forgero:pickaxe_head"), "pickaxe_head",
				Set.of(idFactory.of("forgero:pickaxe_head_shape"), idFactory.of("forgero:default_pickaxe_head")), // ADDED default tag
				List.of(), List.of()
		);
		// The variant shape does not
		var castHeadShape = new NormalizedState.NormalizedShape(
				idFactory.of("forgero:pickaxe_head_cast"), "pickaxe_head_cast",
				Set.of(idFactory.of("forgero:pickaxe_head_shape")),
				List.of(), List.of()
		);

		var headTemplate = new NormalizedState.NormalizedPartTemplate(
				idFactory.of("forgero:pickaxe_head_template"), "Pickaxe Head",
				Set.of(idFactory.of("forgero:parts/pickaxe_head_type")),
				new PartTemplateStructureData("forgero:{material.name}-{shape.name}",
						Map.of(
								"material", new PartTemplateStructureSlotData(idFactory.of("forgero:tool_material"), 1, null),
								"shape", new PartTemplateStructureSlotData(idFactory.of("forgero:pickaxe_head_shape"), 1, null)
						)),
				List.of()
		);

		var oakHandle = new NormalizedState.NormalizedStaticPart(
				idFactory.of("forgero:static_oak_handle"), "Oak Handle",
				Set.of(idFactory.of("forgero:parts/handle_type"), idFactory.of("forgero:default_handle")), // ADDED default tag
				List.of(), List.of(), List.of()
		);

		var pickaxeTemplate = new NormalizedState.NormalizedEquipmentTemplate(
				idFactory.of("forgero:pickaxe_template"), "Pickaxe", Set.of(idFactory.of("forgero:pickaxe")),
				new EquipmentTemplateStructureData("forgero:{head.material.name}-pickaxe",
						Map.of(
								"head", new EquipmentTemplateSlotData(idFactory.of("forgero:parts/pickaxe_head_type"), idFactory.of("forgero:default_pickaxe_head"), null),
								"handle", new EquipmentTemplateSlotData(idFactory.of("forgero:parts/handle_type"), null, idFactory.of("forgero:static_oak_handle"))
						)), List.of()
		);

		NormalizedState state = new NormalizedState(
				Map.of(iron.id(), iron),
				Map.of(headShape.id(), headShape, castHeadShape.id(), castHeadShape),
				Map.of(),
				Map.of(headTemplate.id(), headTemplate),
				Map.of(pickaxeTemplate.id(), pickaxeTemplate),
				Map.of(oakHandle.id(), oakHandle)
		);

		GeneratedState generated = generator.generate(state, tagGraph);

		// All part variants should be generated
		assertEquals(2, generated.parts().size());
		assertTrue(generated.parts().containsKey(idFactory.of("forgero:iron-pickaxe_head")));
		assertTrue(generated.parts().containsKey(idFactory.of("forgero:iron-pickaxe_head_cast")));

		// Only the default tool should be generated
		assertEquals(1, generated.equipment().size());
		assertTrue(generated.equipment().containsKey(idFactory.of("forgero:iron-pickaxe")));
		assertFalse(generated.equipment().containsKey(idFactory.of("forgero:iron-cast-pickaxe"))); // Assuming ID would be this, which it isn't
	}
}
