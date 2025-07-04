package com.sigmundgranaas.forgero.data.generator;

import com.sigmundgranaas.forgero.core.ForgeroTest;
import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.tags.engine.TagGraph;
import com.sigmundgranaas.forgero.core.tags.engine.TagGraphBuilder;
import com.sigmundgranaas.forgero.data.v3.dto.IdentifiedTopLevelData;
import com.sigmundgranaas.forgero.data.v3.dto.MaterialData;
import com.sigmundgranaas.forgero.data.v3.dto.PartTemplateData;
import com.sigmundgranaas.forgero.data.v3.dto.PartTemplateNamingData;
import com.sigmundgranaas.forgero.data.v3.dto.PartTemplateStructureData;
import com.sigmundgranaas.forgero.data.v3.dto.PartTemplateStructureMaterialData;
import com.sigmundgranaas.forgero.data.v3.dto.SchematicData;
import com.sigmundgranaas.forgero.data.v3.dto.StaticPartData;
import com.sigmundgranaas.forgero.data.v3.dto.ToolTemplateData;
import com.sigmundgranaas.forgero.data.v3.dto.ToolTemplateSlotData;
import com.sigmundgranaas.forgero.data.v3.dto.ToolTemplateStructureData;
import com.sigmundgranaas.forgero.data.v3.dto.TopLevelData;
import com.sigmundgranaas.forgero.data.v3.dto.attribute.AttributeData;
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

	// Helper to create an IdentifiedTopLevelData from a raw DTO
	// This uses the actual production IdentifiedTopLevelData
	private IdentifiedTopLevelData id(String idPath, Object data) {
		return new IdentifiedTopLevelData(idFactory.of(idPath), data);
	}

	@BeforeEach
	void setUp() {
		generator = new ComponentGeneratorImpl(idFactory);

		// Setup a basic tag graph for material compatibility
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
	void testGenerate_StaticDataPassthrough() {
		// Use actual StaticPartData
		StaticPartData staticPart = new StaticPartData(idFactory.of("forgero:static_part"), "Static Hammer", null, List.of(idFactory.of("tool")), null, null);
		// Use actual SchematicData
		SchematicData schematic = new SchematicData(idFactory.of("forgero:schematic"), "Pickaxe Schematic", null, null, idFactory.of("forgero:pickaxe_head"), "minecraft:paper");


		Map<OpenIdentifier, TopLevelData> normalizedData = Map.of(
				id("static_hammer", staticPart).id(), id("static_hammer", staticPart),
				id("pickaxe_schematic", schematic).id(), id("pickaxe_schematic", schematic)
		);

		Map<OpenIdentifier, TopLevelData> generated = generator.generate(normalizedData, tagGraph);

		assertEquals(2, generated.size());
		assertTrue(generated.containsKey(idFactory.of("static_hammer")));
		assertTrue(generated.containsKey(idFactory.of("pickaxe_schematic")));

		// Assert that returned objects are new IdentifiedTopLevelData wrappers, but the *wrapped data* is the original.
		assertInstanceOf(IdentifiedTopLevelData.class, generated.get(idFactory.of("static_hammer")));
		assertSame(staticPart, generated.get(idFactory.of("static_hammer")).unwrapAs(StaticPartData.class));
		assertInstanceOf(IdentifiedTopLevelData.class, generated.get(idFactory.of("pickaxe_schematic")));
		assertSame(schematic, generated.get(idFactory.of("pickaxe_schematic")).unwrapAs(SchematicData.class));
	}

	@Test
	void testGenerate_SimplePartCombination() {
		// Use actual MaterialData
		MaterialData iron = new MaterialData(idFactory.of("forgero:material"), "Iron", null, List.of(idFactory.of("forgero:metal")), null, null);
		// Use actual PartTemplateData
		PartTemplateData pickaxeHeadTemplate = new PartTemplateData(
				idFactory.of("forgero:part_template"),
				"Pickaxe Head",
				null, List.of(idFactory.of("pickaxe_head_part")),
				new PartTemplateStructureData(new PartTemplateStructureMaterialData(idFactory.of("forgero:tool_material"), 1, null)),
				null, null, null, null
		);

		Map<OpenIdentifier, TopLevelData> normalizedData = Map.of(
				id("iron", iron).id(), id("iron", iron),
				id("pickaxe_head_template", pickaxeHeadTemplate).id(), id("pickaxe_head_template", pickaxeHeadTemplate)
		);

		// Add a tag for forgero:tool_material that iron is tagged with
		TagGraphBuilder tagBuilder = new TagGraphBuilder();
		tagBuilder.add(idFactory.of("forgero:metal"), Set.of(idFactory.of("forgero:tool_material")));
		TagGraph testTagGraph = tagBuilder.build();


		Map<OpenIdentifier, TopLevelData> generated = generator.generate(normalizedData, testTagGraph);

		assertEquals(1, generated.size(), "Should generate one part");
		OpenIdentifier generatedPartId = idFactory.of("iron-pickaxe_head");
		assertTrue(generated.containsKey(generatedPartId));
		TopLevelData generatedPart = generated.get(generatedPartId);

		assertEquals("Iron Pickaxe Head", generatedPart.name());
		assertEquals(idFactory.of("forgero:part_template"), generatedPart.type());
		assertNotNull(generatedPart.tags());
		assertTrue(generatedPart.tags().contains(idFactory.of("forgero:metal"))); // From material
		assertTrue(generatedPart.tags().contains(idFactory.of("pickaxe_head_part"))); // From template
		assertNull(generatedPart.attributes()); // No attributes defined in source
		assertNull(generatedPart.features());
		// Verify the underlying DTO type is PartTemplateData
		assertInstanceOf(PartTemplateData.class, generatedPart.unwrapAs(Object.class));
	}

	@Test
	void testGenerate_PartWithCompositeAttributes() {
		// Use actual MaterialData and AttributeDataImpl
		MaterialData iron = new MaterialData(idFactory.of("forgero:material"), "Iron",
				null, List.of(idFactory.of("forgero:metal")),
				List.of(
						new AttributeDataImpl(idFactory.of("iron-durability"), idFactory.of("forgero:durability"), new ComputationData(5f, ADDITION_OPERATOR, "forgero:base"), null, null),
						new AttributeDataImpl(idFactory.of("iron-mining-speed-composite"), idFactory.of("forgero:mining_speed"), new ComputationData(8f, ADDITION_OPERATOR, "forgero:base"), null, idFactory.of("material-mining-speed"))
				), null);

		// Use actual PartTemplateData and AttributeDataImpl
		PartTemplateData pickaxeHeadTemplate = new PartTemplateData(
				idFactory.of("forgero:part_template"),
				"Pickaxe Head",
				null, List.of(idFactory.of("pickaxe_head_part")),
				new PartTemplateStructureData(new PartTemplateStructureMaterialData(idFactory.of("forgero:tool_material"), 1, null)),
				null,
				new PartTemplateNamingData("{material_name} {part_template_name}"),
				List.of(
						new AttributeDataImpl(idFactory.of("template-mining-speed-composite"), idFactory.of("forgero:mining_speed"), new ComputationData(1.5f, MULTIPLICATION_OPERATOR, "forgero:base"), null, idFactory.of("material-mining-speed")),
						new AttributeDataImpl(idFactory.of("template-efficiency"), idFactory.of("forgero:efficiency"), new ComputationData(0.1f, ADDITION_OPERATOR, "forgero:base"), null, null)
				),
				null
		);
		Map<OpenIdentifier, TopLevelData> normalizedData = Map.of(
				id("iron", iron).id(), id("iron", iron),
				id("pickaxe_head_template", pickaxeHeadTemplate).id(), id("pickaxe_head_template", pickaxeHeadTemplate)
		);

		// Set up tag graph for compatibility
		TagGraphBuilder tagBuilder = new TagGraphBuilder();
		tagBuilder.add(idFactory.of("forgero:metal"), Set.of(idFactory.of("forgero:tool_material")));
		TagGraph testTagGraph = tagBuilder.build();

		Map<OpenIdentifier, TopLevelData> generated = generator.generate(normalizedData, testTagGraph);
		TopLevelData generatedPart = generated.get(idFactory.of("iron-pickaxe_head"));

		assertNotNull(generatedPart.attributes());
		assertEquals(3, generatedPart.attributes().size(), "Should have 3 attributes: durability, resolved mining_speed, and efficiency");

		Map<OpenIdentifier, AttributeData> attrs = generatedPart.getAttributesMap();
		assertTrue(attrs.containsKey(idFactory.of("iron-durability"))); // Non-composite from material
		assertTrue(attrs.containsKey(idFactory.of("template-efficiency"))); // Non-composite from template

		// Check resolved composite attribute
		AttributeData resolvedMiningSpeed = attrs.get(idFactory.of("template-mining-speed-composite"));
		assertNotNull(resolvedMiningSpeed);
		assertNull(resolvedMiningSpeed.composite(), "Resolved attribute should not be composite");
		assertEquals(idFactory.of("forgero:mining_speed"), resolvedMiningSpeed.type());
		// Original material value (8) * template multiplier (1.5) = 12
		assertEquals(12f, resolvedMiningSpeed.computation().value(), 0.001f, "Composite mining speed should be resolved correctly (8 * 1.5)");
		assertInstanceOf(PartTemplateData.class, generatedPart.unwrapAs(Object.class));
	}

	@Test
	void testGenerate_PartNamingPattern() {
		// Use actual MaterialData
		MaterialData wood = new MaterialData(idFactory.of("forgero:material"), "Oak", null, List.of(idFactory.of("forgero:wood")), null, null);
		// Use actual PartTemplateData
		PartTemplateData handleTemplate = new PartTemplateData(
				idFactory.of("forgero:part_template"),
				"Handle",
				null, null,
				new PartTemplateStructureData(new PartTemplateStructureMaterialData(idFactory.of("forgero:handle_material"), 1, null)),
				null,
				new PartTemplateNamingData("{material_name} {part_template_name} of Power"),
				null, null
		);
		Map<OpenIdentifier, TopLevelData> normalizedData = Map.of(
				id("oak", wood).id(), id("oak", wood),
				id("handle_template", handleTemplate).id(), id("handle_template", handleTemplate)
		);

		// Add tag for handle_material
		TagGraphBuilder tagBuilder = new TagGraphBuilder();
		tagBuilder.add(idFactory.of("forgero:wood"), Set.of(idFactory.of("forgero:handle_material")));
		TagGraph testTagGraph = tagBuilder.build();

		Map<OpenIdentifier, TopLevelData> generated = generator.generate(normalizedData, testTagGraph);
		TopLevelData generatedPart = generated.get(idFactory.of("oak-handle"));

		assertEquals("Oak Handle of Power", generatedPart.name());
		assertInstanceOf(PartTemplateData.class, generatedPart.unwrapAs(Object.class));
	}

	@Test
	void testGenerate_PartIncludesExcludedMaterialTypes() {
		// Use actual MaterialData
		MaterialData iron = new MaterialData(idFactory.of("forgero:material"), "Iron", null, List.of(idFactory.of("forgero:metal")), null, null);
		MaterialData wood = new MaterialData(idFactory.of("forgero:material"), "Oak", null, List.of(idFactory.of("forgero:wood")), null, null); // Not a tool_material

		// Use actual PartTemplateData
		PartTemplateData pickaxeHeadTemplate = new PartTemplateData(
				idFactory.of("forgero:part_template"),
				"Pickaxe Head",
				null, null,
				new PartTemplateStructureData(new PartTemplateStructureMaterialData(idFactory.of("forgero:tool_material"), 1, null)),
				null, null, null, null
		);

		Map<OpenIdentifier, TopLevelData> normalizedData = Map.of(
				id("iron", iron).id(), id("iron", iron),
				id("oak", wood).id(), id("oak", wood),
				id("pickaxe_head_template", pickaxeHeadTemplate).id(), id("pickaxe_head_template", pickaxeHeadTemplate)
		);

		// Set up tag graph: only metal is tool_material
		TagGraphBuilder tagBuilder = new TagGraphBuilder();
		tagBuilder.add(idFactory.of("forgero:metal"), Set.of(idFactory.of("forgero:tool_material")));
		tagBuilder.add(idFactory.of("forgero:wood"), Set.of()); // Wood is not a tool_material
		TagGraph testTagGraph = tagBuilder.build();

		Map<OpenIdentifier, TopLevelData> generated = generator.generate(normalizedData, testTagGraph);

		assertEquals(1, generated.size(), "Only Iron Pickaxe Head should be generated");
		assertTrue(generated.containsKey(idFactory.of("iron-pickaxe_head")));
		assertFalse(generated.containsKey(idFactory.of("oak-pickaxe_head")));
		assertInstanceOf(PartTemplateData.class, generated.get(idFactory.of("iron-pickaxe_head")).unwrapAs(Object.class));
	}

	@Test
	void testGenerate_ToolWithAllDefaultSlots() {
		// Use actual StaticPartData
		StaticPartData ironHead = new StaticPartData(idFactory.of("forgero:static_part"), "Iron Pickaxe Head", null, List.of(idFactory.of("forgero:pickaxe_head_type")), null, null);
		StaticPartData oakHandle = new StaticPartData(idFactory.of("forgero:static_part"), "Oak Handle", null, List.of(idFactory.of("forgero:handle_type")), null, null);

		Map<String, ToolTemplateSlotData> slots = Map.of(
				"head", new ToolTemplateSlotData(idFactory.of("forgero:pickaxe_head_type"), idFactory.of("iron_pickaxe_head")),
				"handle", new ToolTemplateSlotData(idFactory.of("forgero:handle_type"), idFactory.of("oak_handle"))
		);
		ToolTemplateStructureData structure = new ToolTemplateStructureData(slots);
		// Use actual ToolTemplateData
		ToolTemplateData pickaxeTemplate = new ToolTemplateData(idFactory.of("forgero:tool_template"), "Pickaxe", null, List.of(idFactory.of("pickaxe_tool")), structure, null, null, null);

		Map<OpenIdentifier, TopLevelData> normalizedData = Map.of(
				id("iron_pickaxe_head", ironHead).id(), id("iron_pickaxe_head", ironHead),
				id("oak_handle", oakHandle).id(), id("oak_handle", oakHandle),
				id("pickaxe_template", pickaxeTemplate).id(), id("pickaxe_template", pickaxeTemplate)
		);

		Map<OpenIdentifier, TopLevelData> generated = generator.generate(normalizedData, tagGraph);

		// FIX: Expect 3 items (2 static parts + 1 generated tool)
		assertEquals(3, generated.size(), "Should generate only one tool: pickaxe-iron_pickaxe_head-oak_handle, plus the 2 static parts.");
		OpenIdentifier generatedToolId = idFactory.of("pickaxe-iron_pickaxe_head-oak_handle");
		assertTrue(generated.containsKey(generatedToolId));
		TopLevelData generatedTool = generated.get(generatedToolId);

		assertEquals("Pickaxe-Iron Pickaxe Head-Oak Handle", generatedTool.name());
		assertTrue(generatedTool.tags().contains(idFactory.of("pickaxe_tool")));
		assertTrue(generatedTool.tags().contains(idFactory.of("forgero:pickaxe_head_type"))); // From part
		assertTrue(generatedTool.tags().contains(idFactory.of("forgero:handle_type"))); // From part
		assertInstanceOf(ToolTemplateData.class, generatedTool.unwrapAs(Object.class));
	}

	@Test
	void testGenerate_ToolWithOneCombinatorialSlot() {
		// Use actual StaticPartData
		StaticPartData ironHead = new StaticPartData(idFactory.of("forgero:static_part"), "Iron Pickaxe Head", null, List.of(idFactory.of("forgero:pickaxe_head_type")), null, null);
		StaticPartData diamondHead = new StaticPartData(idFactory.of("forgero:static_part"), "Diamond Pickaxe Head", null, List.of(idFactory.of("forgero:pickaxe_head_type")), null, null);
		StaticPartData oakHandle = new StaticPartData(idFactory.of("forgero:static_part"), "Oak Handle", null, List.of(idFactory.of("forgero:handle_type")), null, null);

		Map<String, ToolTemplateSlotData> slots = Map.of(
				"head", new ToolTemplateSlotData(idFactory.of("forgero:pickaxe_head_type"), null), // Combinatorial
				"handle", new ToolTemplateSlotData(idFactory.of("forgero:handle_type"), idFactory.of("oak_handle")) // Default
		);
		ToolTemplateStructureData structure = new ToolTemplateStructureData(slots);
		// Use actual ToolTemplateData
		ToolTemplateData pickaxeTemplate = new ToolTemplateData(idFactory.of("forgero:tool_template"), "Pickaxe", null, List.of(idFactory.of("pickaxe_tool")), structure, null, null, null);

		Map<OpenIdentifier, TopLevelData> normalizedData = Map.of(
				id("iron_pickaxe_head", ironHead).id(), id("iron_pickaxe_head", ironHead),
				id("diamond_pickaxe_head", diamondHead).id(), id("diamond_pickaxe_head", diamondHead),
				id("oak_handle", oakHandle).id(), id("oak_handle", oakHandle),
				id("pickaxe_template", pickaxeTemplate).id(), id("pickaxe_template", pickaxeTemplate)
		);

		Map<OpenIdentifier, TopLevelData> generated = generator.generate(normalizedData, tagGraph);

		// FIX: Expect 5 items (3 static parts + 2 generated tools)
		assertEquals(5, generated.size(), "Should generate two tools: iron-pickaxe, diamond-pickaxe, plus the 3 static parts.");
		assertTrue(generated.containsKey(idFactory.of("pickaxe-diamond_pickaxe_head-oak_handle")));
		assertTrue(generated.containsKey(idFactory.of("pickaxe-iron_pickaxe_head-oak_handle")));
		assertInstanceOf(ToolTemplateData.class, generated.get(idFactory.of("pickaxe-diamond_pickaxe_head-oak_handle")).unwrapAs(Object.class));
	}

	@Test
	void testGenerate_ToolWithMultipleCombinatorialSlots() {
		// Use actual StaticPartData
		StaticPartData ironHead = new StaticPartData(idFactory.of("forgero:static_part"), "Iron Pickaxe Head", null, List.of(idFactory.of("forgero:pickaxe_head_type")), null, null);
		StaticPartData diamondHead = new StaticPartData(idFactory.of("forgero:static_part"), "Diamond Pickaxe Head", null, List.of(idFactory.of("forgero:pickaxe_head_type")), null, null);
		StaticPartData oakHandle = new StaticPartData(idFactory.of("forgero:static_part"), "Oak Handle", null, List.of(idFactory.of("forgero:handle_type")), null, null);
		StaticPartData birchHandle = new StaticPartData(idFactory.of("forgero:static_part"), "Birch Handle", null, List.of(idFactory.of("forgero:handle_type")), null, null);


		Map<String, ToolTemplateSlotData> slots = Map.of(
				"head", new ToolTemplateSlotData(idFactory.of("forgero:pickaxe_head_type"), null), // Combinatorial
				"handle", new ToolTemplateSlotData(idFactory.of("forgero:handle_type"), null) // Combinatorial
		);
		ToolTemplateStructureData structure = new ToolTemplateStructureData(slots);
		// Use actual ToolTemplateData
		ToolTemplateData pickaxeTemplate = new ToolTemplateData(idFactory.of("forgero:tool_template"), "Pickaxe", null, List.of(idFactory.of("pickaxe_tool")), structure, null, null, null);

		Map<OpenIdentifier, TopLevelData> normalizedData = Map.of(
				id("iron_pickaxe_head", ironHead).id(), id("iron_pickaxe_head", ironHead),
				id("diamond_pickaxe_head", diamondHead).id(), id("diamond_pickaxe_head", diamondHead),
				id("oak_handle", oakHandle).id(), id("oak_handle", oakHandle),
				id("birch_handle", birchHandle).id(), id("birch_handle", birchHandle),
				id("pickaxe_template", pickaxeTemplate).id(), id("pickaxe_template", pickaxeTemplate)
		);

		Map<OpenIdentifier, TopLevelData> generated = generator.generate(normalizedData, tagGraph);

		// FIX: Expect 8 items (4 static parts + 4 generated tools)
		assertEquals(8, generated.size(), "Should generate 2 heads * 2 handles = 4 tools, plus the 4 static parts.");
		// FIX: Update expected IDs to match alphabetical sorting of part names in the ID suffix
		assertTrue(generated.containsKey(idFactory.of("pickaxe-birch_handle-diamond_pickaxe_head")), generated.keySet().toString());
		assertTrue(generated.containsKey(idFactory.of("pickaxe-birch_handle-iron_pickaxe_head")), generated.keySet().toString());
		assertTrue(generated.containsKey(idFactory.of("pickaxe-diamond_pickaxe_head-oak_handle")), generated.keySet().toString());
		assertTrue(generated.containsKey(idFactory.of("pickaxe-iron_pickaxe_head-oak_handle")), generated.keySet().toString());

		// Verify that at least one of the generated tools has the correct type (arbitrarily picking one)
		assertInstanceOf(ToolTemplateData.class, generated.get(idFactory.of("pickaxe-birch_handle-diamond_pickaxe_head")).unwrapAs(Object.class));
	}

	@Test
	void testGenerate_MissingDefaultComponent() {
		// Use actual StaticPartData
		StaticPartData ironHead = new StaticPartData(idFactory.of("forgero:static_part"), "Iron Pickaxe Head", null, List.of(idFactory.of("forgero:pickaxe_head_type")), null, null);
		// oakHandle is NOT in normalizedData
		// StaticPartData oakHandle = new StaticPartData(idFactory.of("forgero:static_part"), "Oak Handle", null, List.of(idFactory.of("forgero:handle_type")), null, null);

		Map<String, ToolTemplateSlotData> slots = Map.of(
				"head", new ToolTemplateSlotData(idFactory.of("forgero:pickaxe_head_type"), idFactory.of("iron_pickaxe_head")),
				"handle", new ToolTemplateSlotData(idFactory.of("forgero:handle_type"), idFactory.of("non_existent_handle")) // Points to non-existent
		);
		ToolTemplateStructureData structure = new ToolTemplateStructureData(slots);
		// Use actual ToolTemplateData
		ToolTemplateData pickaxeTemplate = new ToolTemplateData(idFactory.of("forgero:tool_template"), "Pickaxe", null, List.of(idFactory.of("pickaxe_tool")), structure, null, null, null);

		Map<OpenIdentifier, TopLevelData> normalizedData = Map.of(
				id("iron_pickaxe_head", ironHead).id(), id("iron_pickaxe_head", ironHead),
				id("pickaxe_template", pickaxeTemplate).id(), id("pickaxe_template", pickaxeTemplate)
		);

		Map<OpenIdentifier, TopLevelData> generated = generator.generate(normalizedData, tagGraph);

		// FIX: Expect 1 item (1 static part, 0 tools)
		assertEquals(1, generated.size(), "No tools should be generated if a default component is missing for a required slot, but the 1 static part should still be present.");
	}

	@Test
	void testGenerate_MissingMatchingPartsForCombinatorialSlot() {
		// Use actual StaticPartData
		StaticPartData ironHead = new StaticPartData(idFactory.of("forgero:static_part"), "Iron Pickaxe Head", null, List.of(idFactory.of("forgero:pickaxe_head_type")), null, null);
		// No parts for handle_type available
		// StaticPartData oakHandle = new StaticPartData(idFactory.of("forgero:static_part"), "Oak Handle", null, List.of(idFactory.of("forgero:handle_type")), null, null);

		Map<String, ToolTemplateSlotData> slots = Map.of(
				"head", new ToolTemplateSlotData(idFactory.of("forgero:pickaxe_head_type"), null),
				"handle", new ToolTemplateSlotData(idFactory.of("forgero:handle_type"), null)
		);
		ToolTemplateStructureData structure = new ToolTemplateStructureData(slots);
		// Use actual ToolTemplateData
		ToolTemplateData pickaxeTemplate = new ToolTemplateData(idFactory.of("forgero:tool_template"), "Pickaxe", null, List.of(idFactory.of("pickaxe_tool")), structure, null, null, null);

		Map<OpenIdentifier, TopLevelData> normalizedData = Map.of(
				id("iron_pickaxe_head", ironHead).id(), id("iron_pickaxe_head", ironHead),
				id("pickaxe_template", pickaxeTemplate).id(), id("pickaxe_template", pickaxeTemplate)
		);

		Map<OpenIdentifier, TopLevelData> generated = generator.generate(normalizedData, tagGraph);

		// FIX: Expect 1 item (1 static part, 0 tools)
		assertEquals(1, generated.size(), "No tools should be generated if a combinatorial slot has no matching parts, but the 1 static part should still be present.");
	}
}
