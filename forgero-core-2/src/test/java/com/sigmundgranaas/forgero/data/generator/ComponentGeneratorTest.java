package com.sigmundgranaas.forgero.data.generator;

import com.sigmundgranaas.forgero.core.ForgeroTest;
import com.sigmundgranaas.forgero.core.data.definition.GeneratedState;
import com.sigmundgranaas.forgero.core.data.definition.NormalizedState;
import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.tags.engine.TagGraph;
import com.sigmundgranaas.forgero.core.tags.engine.TagGraphBuilder;
import com.sigmundgranaas.forgero.data.codec.CodecConstants;
import com.sigmundgranaas.forgero.data.dto.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.dto.attribute.AttributeDataImpl;
import com.sigmundgranaas.forgero.data.dto.attribute.ComputationData;
import com.sigmundgranaas.forgero.data.dto.template.EquipmentTemplateSlotData;
import com.sigmundgranaas.forgero.data.dto.template.EquipmentTemplateStructureData;
import com.sigmundgranaas.forgero.data.dto.template.PartTemplateStructureData;
import com.sigmundgranaas.forgero.data.dto.template.PartTemplateStructureSlotData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.sigmundgranaas.forgero.data.codec.AttributeCodecs.ADDITION_OPERATOR;
import static com.sigmundgranaas.forgero.data.codec.AttributeCodecs.MULTIPLICATION_OPERATOR;
import static org.junit.jupiter.api.Assertions.*;

class ComponentGeneratorTest extends ForgeroTest {

	private ComponentGenerator generator;
	private TagGraph tagGraph;

	@BeforeEach
	void setUp() {
		generator = new ComponentGeneratorImpl(idFactory);
		tagGraph = buildTestTagGraph();
	}

	private TagGraph buildTestTagGraph() {
		TagGraphBuilder tagBuilder = new TagGraphBuilder();
		Stream.of(
				"forgero:material", "forgero:shape", "forgero:default_pickaxe_head",
				"forgero:default_handle", "forgero:parts/pickaxe_head_type",
				"forgero:parts/handle_type", "forgero:tool"
		).forEach(tag -> tagBuilder.add(id(tag), Set.of()));

		tagBuilder.add(id("forgero:tool_material"), Set.of(id("forgero:material")));
		tagBuilder.add(id("forgero:pickaxe_head_shape"), Set.of(id("forgero:shape")));
		tagBuilder.add(id("forgero:metal"), Set.of(id("forgero:tool_material")));
		tagBuilder.add(id("forgero:wood"), Set.of(id("forgero:material")));
		tagBuilder.add(id("forgero:pickaxe"), Set.of(id("forgero:tool")));

		return tagBuilder.build();
	}

	// region Test Data Factories
	private NormalizedState.NormalizedMaterial material(String name, String... tags) {
		Set<OpenIdentifier> tagSet = Arrays.stream(tags).map(this::id).collect(Collectors.toSet());
		return new NormalizedState.NormalizedMaterial(id("forgero:" + name), name, tagSet, List.of(), List.of());
	}

	private NormalizedState.NormalizedMaterial materialWithAttributes(String name, List<AttributeData> attributes, String... tags) {
		Set<OpenIdentifier> tagSet = Arrays.stream(tags).map(this::id).collect(Collectors.toSet());
		return new NormalizedState.NormalizedMaterial(id("forgero:" + name), name, tagSet, attributes, List.of());
	}

	private NormalizedState.NormalizedShape shape(String name, String... tags) {
		Set<OpenIdentifier> tagSet = Arrays.stream(tags).map(this::id).collect(Collectors.toSet());
		return new NormalizedState.NormalizedShape(id("forgero:" + name), name, tagSet, List.of(), List.of());
	}

	private NormalizedState.NormalizedShape shapeWithAttributes(String name, List<AttributeData> attributes, String... tags) {
		Set<OpenIdentifier> tagSet = Arrays.stream(tags).map(this::id).collect(Collectors.toSet());
		return new NormalizedState.NormalizedShape(id("forgero:" + name), name, tagSet, attributes, List.of());
	}

	private NormalizedState.NormalizedPartTemplate partTemplate(String name, String typeTag, String materialSlotType, String shapeSlotType) {
		return new NormalizedState.NormalizedPartTemplate(
				id("forgero:" + name), name, Set.of(id(typeTag)),
				new PartTemplateStructureData("forgero:{material.name}-{shape.name}",
						Map.of(
								"material", new PartTemplateStructureSlotData(id(materialSlotType), 1, null),
								"shape", new PartTemplateStructureSlotData(id(shapeSlotType), 1, null)
						)),
				List.of()
		);
	}

	private NormalizedState.NormalizedStaticPart staticPart(String name, String... tags) {
		Set<OpenIdentifier> tagSet = Arrays.stream(tags).map(this::id).collect(Collectors.toSet());
		return new NormalizedState.NormalizedStaticPart(id("forgero:" + name), name, tagSet, List.of(), List.of(), List.of());
	}

	private NormalizedState.NormalizedEquipmentTemplate equipmentTemplate(String name, Map<String, EquipmentTemplateSlotData> slots, String... tags) {
		Set<OpenIdentifier> tagSet = Arrays.stream(tags).map(this::id).collect(Collectors.toSet());
		return new NormalizedState.NormalizedEquipmentTemplate(
				id("forgero:" + name), name, tagSet,
				new EquipmentTemplateStructureData("forgero:{head.material.name}-pickaxe", slots),
				List.of()
		);
	}
	// endregion

	@Test
	void testGenerate_SimplePartCombination() {
		var iron = material("iron", "forgero:metal");
		var headShape = shape("pickaxe_head", "forgero:pickaxe_head_shape");
		var headTemplate = partTemplate("pickaxe_head_template", "forgero:parts/pickaxe_head_type", "forgero:tool_material", "forgero:pickaxe_head_shape");

		var state = new NormalizedState(Map.of(iron.id(), iron), Map.of(headShape.id(), headShape), Map.of(), Map.of(headTemplate.id(), headTemplate), Map.of(), Map.of());
		GeneratedState generated = generator.generate(state, tagGraph);

		assertEquals(1, generated.parts().size());
		OpenIdentifier expectedId = id("forgero:iron-pickaxe_head");
		assertTrue(generated.parts().containsKey(expectedId), "Generated part ID should be " + expectedId + " but was " + generated.parts().keySet());

		var generatedPart = generated.parts().get(expectedId);
		assertNotNull(generatedPart);
		assertTrue(generatedPart.tags().containsAll(Set.of(id("forgero:metal"), id("forgero:pickaxe_head_shape"), id("forgero:parts/pickaxe_head_type"))));
		assertEquals(iron.id(), generatedPart.materialId());
		assertEquals(headShape.id(), generatedPart.shapeId());
	}

	@Test
	void testGenerate_PartWithCompositeAttributes() {
		var iron = materialWithAttributes("iron",
				List.of(new AttributeDataImpl(id("iron-durability"), id("forgero:durability"), new ComputationData(250f, ADDITION_OPERATOR, "forgero:base"), null, id("forgero:part-composite"))),
				"forgero:metal");
		var headShape = shapeWithAttributes("pickaxe_head",
				List.of(new AttributeDataImpl(id("shape-durability-mult"), id("forgero:durability"), new ComputationData(1.5f, MULTIPLICATION_OPERATOR, "forgero:middle"), null, id("forgero:part-composite"))),
				"forgero:pickaxe_head_shape");
		var headTemplate = partTemplate("pickaxe_head_template", "forgero:parts/pickaxe_head_type", "forgero:tool_material", "forgero:pickaxe_head_shape");

		var state = new NormalizedState(Map.of(iron.id(), iron), Map.of(headShape.id(), headShape), Map.of(), Map.of(headTemplate.id(), headTemplate), Map.of(), Map.of());
		GeneratedState generated = generator.generate(state, tagGraph);

		OpenIdentifier generatedPartId = id("forgero:iron-pickaxe_head");
		assertTrue(generated.parts().containsKey(generatedPartId));
		var generatedPart = generated.parts().get(generatedPartId);
		assertNotNull(generatedPart.attributes());
		assertEquals(2, generatedPart.attributes().size());
	}

	@Test
	void testGenerate_ToolWithDeclarativeDefaults() {
		var iron = material("iron", "forgero:metal");
		var diamond = material("diamond", "forgero:metal");
		var headShape = shape("pickaxe_head", "forgero:pickaxe_head_shape", "forgero:default_pickaxe_head");
		var headTemplate = partTemplate("pickaxe_head_template", "forgero:parts/pickaxe_head_type", "forgero:tool_material", "forgero:pickaxe_head_shape");
		var oakHandle = staticPart("static_oak_handle", "forgero:parts/handle_type", "forgero:default_handle");

		var pickaxeTemplate = equipmentTemplate("pickaxe_template", Map.of(
				"head", new EquipmentTemplateSlotData(id("forgero:parts/pickaxe_head_type"), id("forgero:default_pickaxe_head"), null),
				"handle", new EquipmentTemplateSlotData(id("forgero:parts/handle_type"), null, id("forgero:static_oak_handle"))
		), "forgero:pickaxe");

		var state = new NormalizedState(
				Map.of(iron.id(), iron, diamond.id(), diamond), Map.of(headShape.id(), headShape), Map.of(),
				Map.of(headTemplate.id(), headTemplate), Map.of(pickaxeTemplate.id(), pickaxeTemplate), Map.of(oakHandle.id(), oakHandle)
		);

		GeneratedState generated = generator.generate(state, tagGraph);

		assertEquals(2, generated.equipment().size(), "Should generate one default pickaxe for each material (iron, diamond).");
		assertTrue(generated.equipment().containsKey(id("forgero:iron-pickaxe")), "Iron Pickaxe should be generated.");
		assertTrue(generated.equipment().containsKey(id("forgero:diamond-pickaxe")), "Diamond Pickaxe should be generated.");

		var ironPickaxe = generated.equipment().get(id("forgero:iron-pickaxe"));
		assertEquals(id("forgero:iron-pickaxe_head"), ironPickaxe.structure().get("head"));
		assertEquals(id("forgero:static_oak_handle"), ironPickaxe.structure().get("handle"));

		var diamondPickaxe = generated.equipment().get(id("forgero:diamond-pickaxe"));
		assertEquals(id("forgero:diamond-pickaxe_head"), diamondPickaxe.structure().get("head"));
		assertEquals(id("forgero:static_oak_handle"), diamondPickaxe.structure().get("handle"));
	}

	@Test
	void testGenerate_PartVariantsAreCreatedButNotUsedInDefaultTools() {
		var iron = material("iron", "forgero:metal");
		var headShape = shape("pickaxe_head", "forgero:pickaxe_head_shape", "forgero:default_pickaxe_head");
		var castHeadShape = shape("pickaxe_head_cast", "forgero:pickaxe_head_shape");
		var headTemplate = partTemplate("pickaxe_head_template", "forgero:parts/pickaxe_head_type", "forgero:tool_material", "forgero:pickaxe_head_shape");
		var oakHandle = staticPart("static_oak_handle", "forgero:parts/handle_type", "forgero:default_handle");

		var pickaxeTemplate = equipmentTemplate("pickaxe_template", Map.of(
				"head", new EquipmentTemplateSlotData(id("forgero:parts/pickaxe_head_type"), id("forgero:default_pickaxe_head"), null),
				"handle", new EquipmentTemplateSlotData(id("forgero:parts/handle_type"), null, id("forgero:static_oak_handle"))
		), "forgero:pickaxe");

		var state = new NormalizedState(
				Map.of(iron.id(), iron), Map.of(headShape.id(), headShape, castHeadShape.id(), castHeadShape), Map.of(),
				Map.of(headTemplate.id(), headTemplate), Map.of(pickaxeTemplate.id(), pickaxeTemplate), Map.of(oakHandle.id(), oakHandle)
		);

		GeneratedState generated = generator.generate(state, tagGraph);

		assertEquals(2, generated.parts().size(), "All part variants should be generated.");
		assertTrue(generated.parts().containsKey(id("forgero:iron-pickaxe_head")));
		assertTrue(generated.parts().containsKey(id("forgero:iron-pickaxe_head_cast")));

		assertEquals(1, generated.equipment().size(), "Only the default tool should be generated.");
		assertTrue(generated.equipment().containsKey(id("forgero:iron-pickaxe")));
		assertFalse(generated.equipment().containsKey(id("forgero:iron-cast-pickaxe")), "A tool using a non-default part should not be generated.");
	}

	private OpenIdentifier id(String id) {
		return CodecConstants.IDENTIFIER_FACTORY.of(id);
	}
}
