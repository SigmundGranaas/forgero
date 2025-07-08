package com.sigmundgranaas.forgero.data.generator;

import com.sigmundgranaas.forgero.core.ForgeroTest;
import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.tags.engine.TagGraph;
import com.sigmundgranaas.forgero.core.tags.engine.TagGraphBuilder;
import com.sigmundgranaas.forgero.data.v3.dto.*;
import com.sigmundgranaas.forgero.data.v3.dto.attribute.AttributeDataImpl;
import com.sigmundgranaas.forgero.data.v3.dto.attribute.ComputationData;
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

	private IdentifiedTopLevelData id(String idPath, Object data) {
		return new IdentifiedTopLevelData(idFactory.of(idPath), data);
	}

	@BeforeEach
	void setUp() {
		generator = new ComponentGeneratorImpl(idFactory);

		TagGraphBuilder tagBuilder = new TagGraphBuilder();
		tagBuilder.add(idFactory.of("forgero:metal"), Set.of(idFactory.of("forgero:tool_material")));
		tagBuilder.add(idFactory.of("forgero:wood"), Set.of(idFactory.of("forgero:handle_material")));
		tagBuilder.add(idFactory.of("forgero:pickaxe_head_type"), Set.of());
		tagBuilder.add(idFactory.of("forgero:handle_type"), Set.of());
		tagBuilder.add(idFactory.of("forgero:tool_material"), Set.of());
		tagBuilder.add(idFactory.of("forgero:handle_material"), Set.of());
		tagGraph = tagBuilder.build();
	}

	@Test
	void testGenerate_SimplePartCombination() {
		MaterialData iron = new MaterialData(idFactory.of("forgero:material"), "Iron", null, List.of(idFactory.of("forgero:metal")), null, null);
		PartTemplateData pickaxeHeadTemplate = new PartTemplateData(
				idFactory.of("forgero:part_template"), "Pickaxe Head",
				null, List.of(idFactory.of("pickaxe_head_part")),
				new PartTemplateStructureData(new PartTemplateStructureMaterialData(idFactory.of("forgero:tool_material"), 1, null)),
				null, null, null, null
		);

		Map<OpenIdentifier, TopLevelData> normalizedData = Map.of(
				id("iron", iron).id(), id("iron", iron),
				id("pickaxe_head_template", pickaxeHeadTemplate).id(), id("pickaxe_head_template", pickaxeHeadTemplate)
		);

		Map<OpenIdentifier, TopLevelData> generated = generator.generate(normalizedData, tagGraph);

		assertEquals(1, generated.size());
		TopLevelData generatedPart = generated.get(idFactory.of("iron-pickaxe_head"));
		assertNotNull(generatedPart);
		assertNotNull(generatedPart.tags());
		assertTrue(generatedPart.tags().contains(idFactory.of("forgero:metal"))); // From material
		assertTrue(generatedPart.tags().contains(idFactory.of("pickaxe_head_part"))); // From template
	}

	@Test
	void testGenerate_ToolWithAllDefaultSlots() {
		StaticPartData ironHead = new StaticPartData(idFactory.of("forgero:static_part"), "Iron Pickaxe Head", null, List.of(idFactory.of("forgero:pickaxe_head_type")), null, null);
		StaticPartData oakHandle = new StaticPartData(idFactory.of("forgero:static_part"), "Oak Handle", null, List.of(idFactory.of("forgero:handle_type")), null, null);
		ToolTemplateData pickaxeTemplate = new ToolTemplateData(
				idFactory.of("forgero:tool_template"), "Pickaxe",
				null, List.of(idFactory.of("pickaxe_tool")),
				new ToolTemplateStructureData(Map.of(
						"head", new ToolTemplateSlotData(idFactory.of("forgero:pickaxe_head_type"), idFactory.of("iron_pickaxe_head")),
						"handle", new ToolTemplateSlotData(idFactory.of("forgero:handle_type"), idFactory.of("oak_handle"))
				)), null, null, null);

		Map<OpenIdentifier, TopLevelData> normalizedData = Map.of(
				id("iron_pickaxe_head", ironHead).id(), id("iron_pickaxe_head", ironHead),
				id("oak_handle", oakHandle).id(), id("oak_handle", oakHandle),
				id("pickaxe_template", pickaxeTemplate).id(), id("pickaxe_template", pickaxeTemplate)
		);

		Map<OpenIdentifier, TopLevelData> generated = generator.generate(normalizedData, tagGraph);

		assertEquals(3, generated.size());
		OpenIdentifier generatedToolId = idFactory.of("pickaxe-iron_pickaxe_head-oak_handle");
		assertTrue(generated.containsKey(generatedToolId));
		TopLevelData generatedTool = generated.get(generatedToolId);

		assertEquals("Pickaxe-Iron Pickaxe Head-Oak Handle", generatedTool.name());
		assertNotNull(generatedTool.tags());
		// A tool ONLY has tags from its template. Part tags are NOT merged.
		assertTrue(generatedTool.tags().contains(idFactory.of("pickaxe_tool")));
		assertFalse(generatedTool.tags().contains(idFactory.of("forgero:pickaxe_head_type")));
		assertFalse(generatedTool.tags().contains(idFactory.of("forgero:handle_type")));
		assertInstanceOf(GeneratedEquipmentData.class, generatedTool.unwrapAs(Object.class));
	}

	@Test
	void testGenerate_StaticDataPassthrough() {
		StaticPartData staticPart = new StaticPartData(idFactory.of("forgero:static_part"), "Static Hammer", null, List.of(idFactory.of("tool")), null, null);
		SchematicData schematic = new SchematicData(idFactory.of("forgero:schematic"), "Pickaxe Schematic", null, null, idFactory.of("forgero:pickaxe_head"), "minecraft:paper");

		Map<OpenIdentifier, TopLevelData> normalizedData = Map.of(
				id("static_hammer", staticPart).id(), id("static_hammer", staticPart),
				id("pickaxe_schematic", schematic).id(), id("pickaxe_schematic", schematic)
		);

		Map<OpenIdentifier, TopLevelData> generated = generator.generate(normalizedData, tagGraph);

		assertEquals(2, generated.size());
		assertTrue(generated.containsKey(idFactory.of("static_hammer")));
		assertTrue(generated.containsKey(idFactory.of("pickaxe_schematic")));
		assertSame(staticPart, generated.get(idFactory.of("static_hammer")).unwrapAs(StaticPartData.class));
		assertSame(schematic, generated.get(idFactory.of("pickaxe_schematic")).unwrapAs(SchematicData.class));
	}

	@Test
	void testGenerate_PartWithCompositeAttributes() {
		MaterialData iron = new MaterialData(idFactory.of("forgero:material"), "Iron",
				null, List.of(idFactory.of("forgero:metal")),
				List.of(
						new AttributeDataImpl(idFactory.of("iron-durability"), idFactory.of("forgero:durability"), new ComputationData(5f, ADDITION_OPERATOR, "forgero:base"), null, null),
						new AttributeDataImpl(idFactory.of("iron-mining-speed"), idFactory.of("forgero:mining_speed"), new ComputationData(8f, ADDITION_OPERATOR, "forgero:base"), null, null)
				), null);

		PartTemplateData pickaxeHeadTemplate = new PartTemplateData(
				idFactory.of("forgero:part_template"), "Pickaxe Head",
				null, List.of(idFactory.of("pickaxe_head_part")),
				new PartTemplateStructureData(new PartTemplateStructureMaterialData(idFactory.of("forgero:tool_material"), 1, null)),
				null, new PartTemplateNamingData("{material_name} {part_template_name}"),
				List.of(
						new AttributeDataImpl(idFactory.of("template-mining-speed"), idFactory.of("forgero:mining_speed"), new ComputationData(1.5f, MULTIPLICATION_OPERATOR, "forgero:middle"), null, idFactory.of("material-mining-speed")),
						new AttributeDataImpl(idFactory.of("template-efficiency"), idFactory.of("forgero:efficiency"), new ComputationData(0.1f, ADDITION_OPERATOR, "forgero:base"), null, null)
				), null);

		Map<OpenIdentifier, TopLevelData> normalizedData = Map.of(
				id("iron", iron).id(), id("iron", iron),
				id("pickaxe_head_template", pickaxeHeadTemplate).id(), id("pickaxe_head_template", pickaxeHeadTemplate)
		);

		TagGraphBuilder tagBuilder = new TagGraphBuilder();
		tagBuilder.add(idFactory.of("forgero:metal"), Set.of(idFactory.of("forgero:tool_material")));

		Map<OpenIdentifier, TopLevelData> generated = generator.generate(normalizedData, tagBuilder.build());
		TopLevelData generatedPart = generated.get(idFactory.of("iron-pickaxe_head"));

		assertNotNull(generatedPart.attributes());
		assertEquals(4, generatedPart.attributes().size());
		assertInstanceOf(GeneratedPartData.class, generatedPart.unwrapAs(Object.class));
	}

	@Test
	void testGenerate_PartNamingPattern() {
		MaterialData wood = new MaterialData(idFactory.of("forgero:material"), "Oak", null, List.of(idFactory.of("forgero:wood")), null, null);
		PartTemplateData handleTemplate = new PartTemplateData(
				idFactory.of("forgero:part_template"), "Handle",
				null, null,
				new PartTemplateStructureData(new PartTemplateStructureMaterialData(idFactory.of("forgero:handle_material"), 1, null)),
				null, new PartTemplateNamingData("{material_name} {part_template_name} of Power"),
				null, null
		);

		Map<OpenIdentifier, TopLevelData> normalizedData = Map.of(
				id("oak", wood).id(), id("oak", wood),
				id("handle_template", handleTemplate).id(), id("handle_template", handleTemplate)
		);

		TagGraphBuilder tagBuilder = new TagGraphBuilder();
		tagBuilder.add(idFactory.of("forgero:wood"), Set.of(idFactory.of("forgero:handle_material")));

		Map<OpenIdentifier, TopLevelData> generated = generator.generate(normalizedData, tagBuilder.build());
		TopLevelData generatedPart = generated.get(idFactory.of("oak-handle"));

		assertEquals("Oak Handle of Power", generatedPart.name());
		assertInstanceOf(GeneratedPartData.class, generatedPart.unwrapAs(Object.class));
	}

	@Test
	void testGenerate_PartIncludesExcludedMaterialTypes() {
		MaterialData iron = new MaterialData(idFactory.of("forgero:material"), "Iron", null, List.of(idFactory.of("forgero:metal")), null, null);
		MaterialData wood = new MaterialData(idFactory.of("forgero:material"), "Oak", null, List.of(idFactory.of("forgero:wood")), null, null);
		PartTemplateData pickaxeHeadTemplate = new PartTemplateData(
				idFactory.of("forgero:part_template"), "Pickaxe Head",
				null, null,
				new PartTemplateStructureData(new PartTemplateStructureMaterialData(idFactory.of("forgero:tool_material"), 1, null)),
				null, null, null, null
		);
		Map<OpenIdentifier, TopLevelData> normalizedData = Map.of(
				id("iron", iron).id(), id("iron", iron),
				id("oak", wood).id(), id("oak", wood),
				id("pickaxe_head_template", pickaxeHeadTemplate).id(), id("pickaxe_head_template", pickaxeHeadTemplate)
		);

		TagGraphBuilder tagBuilder = new TagGraphBuilder();
		tagBuilder.add(idFactory.of("forgero:metal"), Set.of(idFactory.of("forgero:tool_material")));
		tagBuilder.add(idFactory.of("forgero:wood"), Set.of());

		Map<OpenIdentifier, TopLevelData> generated = generator.generate(normalizedData, tagBuilder.build());

		assertEquals(1, generated.size());
		assertTrue(generated.containsKey(idFactory.of("iron-pickaxe_head")));
		assertFalse(generated.containsKey(idFactory.of("oak-pickaxe_head")));
		assertInstanceOf(GeneratedPartData.class, generated.get(idFactory.of("iron-pickaxe_head")).unwrapAs(Object.class));
	}


	@Test
	void testGenerate_ToolWithOneCombinatorialSlot() {
		StaticPartData ironHead = new StaticPartData(idFactory.of("forgero:static_part"), "Iron Pickaxe Head", null, List.of(idFactory.of("forgero:pickaxe_head_type")), null, null);
		StaticPartData diamondHead = new StaticPartData(idFactory.of("forgero:static_part"), "Diamond Pickaxe Head", null, List.of(idFactory.of("forgero:pickaxe_head_type")), null, null);
		StaticPartData oakHandle = new StaticPartData(idFactory.of("forgero:static_part"), "Oak Handle", null, List.of(idFactory.of("forgero:handle_type")), null, null);
		ToolTemplateData pickaxeTemplate = new ToolTemplateData(
				idFactory.of("forgero:tool_template"), "Pickaxe",
				null, List.of(idFactory.of("pickaxe_tool")),
				new ToolTemplateStructureData(Map.of(
						"head", new ToolTemplateSlotData(idFactory.of("forgero:pickaxe_head_type"), null),
						"handle", new ToolTemplateSlotData(idFactory.of("forgero:handle_type"), idFactory.of("oak_handle"))
				)), null, null, null);

		Map<OpenIdentifier, TopLevelData> normalizedData = Map.of(
				id("iron_pickaxe_head", ironHead).id(), id("iron_pickaxe_head", ironHead),
				id("diamond_pickaxe_head", diamondHead).id(), id("diamond_pickaxe_head", diamondHead),
				id("oak_handle", oakHandle).id(), id("oak_handle", oakHandle),
				id("pickaxe_template", pickaxeTemplate).id(), id("pickaxe_template", pickaxeTemplate)
		);

		Map<OpenIdentifier, TopLevelData> generated = generator.generate(normalizedData, tagGraph);

		assertEquals(5, generated.size());
		assertTrue(generated.containsKey(idFactory.of("pickaxe-diamond_pickaxe_head-oak_handle")));
		assertTrue(generated.containsKey(idFactory.of("pickaxe-iron_pickaxe_head-oak_handle")));
		assertInstanceOf(GeneratedEquipmentData.class, generated.get(idFactory.of("pickaxe-diamond_pickaxe_head-oak_handle")).unwrapAs(Object.class));
	}

	@Test
	void testGenerate_ToolWithMultipleCombinatorialSlots() {
		StaticPartData ironHead = new StaticPartData(idFactory.of("forgero:static_part"), "Iron Pickaxe Head", null, List.of(idFactory.of("forgero:pickaxe_head_type")), null, null);
		StaticPartData diamondHead = new StaticPartData(idFactory.of("forgero:static_part"), "Diamond Pickaxe Head", null, List.of(idFactory.of("forgero:pickaxe_head_type")), null, null);
		StaticPartData oakHandle = new StaticPartData(idFactory.of("forgero:static_part"), "Oak Handle", null, List.of(idFactory.of("forgero:handle_type")), null, null);
		StaticPartData birchHandle = new StaticPartData(idFactory.of("forgero:static_part"), "Birch Handle", null, List.of(idFactory.of("forgero:handle_type")), null, null);
		ToolTemplateData pickaxeTemplate = new ToolTemplateData(
				idFactory.of("forgero:tool_template"), "Pickaxe",
				null, List.of(idFactory.of("pickaxe_tool")),
				new ToolTemplateStructureData(Map.of(
						"head", new ToolTemplateSlotData(idFactory.of("forgero:pickaxe_head_type"), null),
						"handle", new ToolTemplateSlotData(idFactory.of("forgero:handle_type"), null)
				)), null, null, null);

		Map<OpenIdentifier, TopLevelData> normalizedData = Map.of(
				id("iron_pickaxe_head", ironHead).id(), id("iron_pickaxe_head", ironHead),
				id("diamond_pickaxe_head", diamondHead).id(), id("diamond_pickaxe_head", diamondHead),
				id("oak_handle", oakHandle).id(), id("oak_handle", oakHandle),
				id("birch_handle", birchHandle).id(), id("birch_handle", birchHandle),
				id("pickaxe_template", pickaxeTemplate).id(), id("pickaxe_template", pickaxeTemplate)
		);

		Map<OpenIdentifier, TopLevelData> generated = generator.generate(normalizedData, tagGraph);

		assertEquals(8, generated.size());
		assertTrue(generated.containsKey(idFactory.of("pickaxe-birch_handle-diamond_pickaxe_head")));
		assertTrue(generated.containsKey(idFactory.of("pickaxe-birch_handle-iron_pickaxe_head")));
		assertTrue(generated.containsKey(idFactory.of("pickaxe-diamond_pickaxe_head-oak_handle")));
		assertTrue(generated.containsKey(idFactory.of("pickaxe-iron_pickaxe_head-oak_handle")));
		assertInstanceOf(GeneratedEquipmentData.class, generated.get(idFactory.of("pickaxe-birch_handle-diamond_pickaxe_head")).unwrapAs(Object.class));
	}

	@Test
	void testGenerate_MissingDefaultComponent() {
		StaticPartData ironHead = new StaticPartData(idFactory.of("forgero:static_part"), "Iron Pickaxe Head", null, List.of(idFactory.of("forgero:pickaxe_head_type")), null, null);
		ToolTemplateData pickaxeTemplate = new ToolTemplateData(
				idFactory.of("forgero:tool_template"), "Pickaxe",
				null, List.of(idFactory.of("pickaxe_tool")),
				new ToolTemplateStructureData(Map.of(
						"head", new ToolTemplateSlotData(idFactory.of("forgero:pickaxe_head_type"), idFactory.of("iron_pickaxe_head")),
						"handle", new ToolTemplateSlotData(idFactory.of("forgero:handle_type"), idFactory.of("non_existent_handle"))
				)), null, null, null);

		Map<OpenIdentifier, TopLevelData> normalizedData = Map.of(
				id("iron_pickaxe_head", ironHead).id(), id("iron_pickaxe_head", ironHead),
				id("pickaxe_template", pickaxeTemplate).id(), id("pickaxe_template", pickaxeTemplate)
		);

		Map<OpenIdentifier, TopLevelData> generated = generator.generate(normalizedData, tagGraph);

		assertEquals(1, generated.size());
	}

	@Test
	void testGenerate_MissingMatchingPartsForCombinatorialSlot() {
		StaticPartData ironHead = new StaticPartData(idFactory.of("forgero:static_part"), "Iron Pickaxe Head", null, List.of(idFactory.of("forgero:pickaxe_head_type")), null, null);
		ToolTemplateData pickaxeTemplate = new ToolTemplateData(
				idFactory.of("forgero:tool_template"), "Pickaxe",
				null, List.of(idFactory.of("pickaxe_tool")),
				new ToolTemplateStructureData(Map.of(
						"head", new ToolTemplateSlotData(idFactory.of("forgero:pickaxe_head_type"), null),
						"handle", new ToolTemplateSlotData(idFactory.of("forgero:handle_type"), null)
				)), null, null, null);

		Map<OpenIdentifier, TopLevelData> normalizedData = Map.of(
				id("iron_pickaxe_head", ironHead).id(), id("iron_pickaxe_head", ironHead),
				id("pickaxe_template", pickaxeTemplate).id(), id("pickaxe_template", pickaxeTemplate)
		);

		Map<OpenIdentifier, TopLevelData> generated = generator.generate(normalizedData, tagGraph);

		assertEquals(1, generated.size());
	}
}
