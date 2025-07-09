package com.sigmundgranaas.forgero.data.generation;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.sigmundgranaas.forgero.core.ForgeroTest;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.tags.engine.TagGraph;
import com.sigmundgranaas.forgero.common.tags.engine.TagGraphBuilder;
import com.sigmundgranaas.forgero.data.Utils;
import com.sigmundgranaas.forgero.data.generation.api.ComponentGenerator;
import com.sigmundgranaas.forgero.data.generation.api.GeneratedState;
import com.sigmundgranaas.forgero.data.generation.impl.ComponentGeneratorImpl;
import com.sigmundgranaas.forgero.data.loading.api.data.template.EquipmentTemplateSlotData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.EquipmentTemplateStructureData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.PartTemplateStructureData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.PartTemplateStructureSlotData;
import com.sigmundgranaas.forgero.data.processing.api.NormalizedState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.sigmundgranaas.forgero.data.Utils.id;
import static org.junit.jupiter.api.Assertions.*;

class ComponentGeneratorTest extends ForgeroTest {

	private ComponentGenerator generator;
	private TagGraph tagGraph;

	private static final String ATTRIBUTES_KEY = "forgero:attributes";
	private static final String FEATURES_KEY = "forgero:features";

	@BeforeEach
	void setUp() {
		generator = new ComponentGeneratorImpl(idFactory);
		tagGraph = buildTestTagGraph();
	}

	// Helper to create a JSON representation of an attribute
	private JsonObject attributeJson(String id, String type, float value) {
		JsonObject attr = new JsonObject();
		attr.addProperty("id", "forgero:" + id);
		attr.addProperty("type", "forgero:" + type);
		// Changed to reflect how ComputationData is encoded (as an object)
		JsonObject computationObj = new JsonObject();
		computationObj.addProperty("value", value);
		computationObj.addProperty("operator", "forgero:addition");
		computationObj.addProperty("order", "forgero:base");
		attr.add("computation", computationObj);
		return attr;
	}

	// Helper to create a JSON representation of a feature
	private JsonObject featureJson(String type, String title) {
		JsonObject feature = new JsonObject();
		feature.addProperty("type", "forgero:" + type);
		feature.addProperty("title", title);
		feature.addProperty("description", "desc");
		// Simplified selector for testing
		JsonObject selector = new JsonObject();
		selector.addProperty("type", "forgero:radius");
		selector.addProperty("radius", 1);
		selector.addProperty("tag", "forgero:tag");
		feature.add("selector", selector);
		return feature;
	}

	@Test
	void generatedPartMergesPropertiesFromAllSources() {
		// Setup
		JsonArray matAttrs = new JsonArray();
		matAttrs.add(attributeJson("mat_attr", "durability", 100));
		var materialProps = Map.of(
				ATTRIBUTES_KEY, (JsonElement) matAttrs,
				"custom:material_prop", new JsonPrimitive("mat_val")
		);

		JsonArray shapeAttrs = new JsonArray();
		shapeAttrs.add(attributeJson("shape_attr", "mining_speed", 5));
		var shapeProps = Map.of(
				ATTRIBUTES_KEY, (JsonElement) shapeAttrs,
				"custom:shape_prop", new JsonPrimitive("shape_val")
		);

		JsonArray templateFeatures = new JsonArray();
		templateFeatures.add(featureJson("vein_mining", "Vein Miner"));
		var templateProps = Map.of(
				FEATURES_KEY, (JsonElement) templateFeatures,
				"custom:template_prop", new JsonPrimitive("template_val")
		);

		var iron = materialWithProperties("iron", materialProps, "forgero:metal");
		var headShape = shapeWithProperties("pickaxe_head", shapeProps, "forgero:pickaxe_head_shape");
		var headTemplate = partTemplateWithProperties("pickaxe_head_template", "forgero:parts/pickaxe_head_type", "forgero:tool_material", "forgero:pickaxe_head_shape", templateProps);

		var state = new NormalizedState(Map.of(iron.id(), iron), Map.of(headShape.id(), headShape), Map.of(), Map.of(headTemplate.id(), headTemplate), Map.of(), Map.of());

		// Act
		GeneratedState generated = generator.generate(state, tagGraph);

		// Assert
		var generatedPart = generated.parts().get(id("forgero:iron-pickaxe_head"));
		assertNotNull(generatedPart, "Generated part should exist.");
		var finalProps = generatedPart.properties();
		assertNotNull(finalProps);

		// Assert custom props are merged (template overrides)
		assertEquals("mat_val", finalProps.get("custom:material_prop").getAsString());
		assertEquals("shape_val", finalProps.get("custom:shape_prop").getAsString());
		assertEquals("template_val", finalProps.get("custom:template_prop").getAsString());

		// Assert attributes are merged
		assertTrue(finalProps.containsKey(ATTRIBUTES_KEY));
		JsonArray finalAttrs = finalProps.get(ATTRIBUTES_KEY).getAsJsonArray();
		assertEquals(2, finalAttrs.size(), "Should have one attribute from material and one from shape.");

		// Assert features are present
		assertTrue(finalProps.containsKey(FEATURES_KEY));
		JsonArray finalFeatures = finalProps.get(FEATURES_KEY).getAsJsonArray();
		assertEquals(1, finalFeatures.size());
	}

	@Test
	void generatedPartAttributeOverride() {
		// Setup: Material and Shape both define the same attribute ID. Shape should win.
		JsonArray matAttrs = new JsonArray();
		matAttrs.add(attributeJson("shared_attr", "durability", 100));
		var materialProps = Map.of(ATTRIBUTES_KEY, (JsonElement) matAttrs);

		JsonArray shapeAttrs = new JsonArray();
		shapeAttrs.add(attributeJson("shared_attr", "durability", 200));
		var shapeProps = Map.of(ATTRIBUTES_KEY, (JsonElement) shapeAttrs);

		var iron = materialWithProperties("iron", materialProps, "forgero:metal");
		var headShape = shapeWithProperties("pickaxe_head", shapeProps, "forgero:pickaxe_head_shape");
		var headTemplate = partTemplate("pickaxe_head_template", "forgero:parts/pickaxe_head_type", "forgero:tool_material", "forgero:pickaxe_head_shape");

		var state = new NormalizedState(Map.of(iron.id(), iron), Map.of(headShape.id(), headShape), Map.of(), Map.of(headTemplate.id(), headTemplate), Map.of(), Map.of());

		// Act
		GeneratedState generated = generator.generate(state, tagGraph);

		// Assert
		var finalProps = generated.parts().get(id("forgero:iron-pickaxe_head")).properties();
		JsonArray finalAttrs = finalProps.get(ATTRIBUTES_KEY).getAsJsonArray();
		assertEquals(1, finalAttrs.size(), "Attributes with the same ID should be overridden, not duplicated.");
		// Access the nested 'value' inside the 'computation' object
		assertEquals(200f, finalAttrs.get(0).getAsJsonObject().get("computation").getAsJsonObject().get("value").getAsFloat(), "The shape's attribute value should override the material's.");
	}

	@Test
	void generatedEquipmentHasPropertiesOnlyFromTemplate() {
		// Setup
		Map<String, JsonElement> partProps = Map.of("custom:part_prop", new JsonPrimitive("part_val"));
		var oakHandle = staticPartWithProperties("static_oak_handle", partProps, "forgero:parts/handle_type", "forgero:default_handle");

		Map<String, JsonElement> templateProps = Map.of("custom:template_prop", new JsonPrimitive("template_val"));
		var pickaxeTemplate = equipmentTemplateWithProperties("pickaxe_template", Map.of(
				"handle", new EquipmentTemplateSlotData(id("forgero:parts/handle_type"), null, oakHandle.id())
		), templateProps, "forgero:pickaxe");

		var state = new NormalizedState(Map.of(), Map.of(), Map.of(), Map.of(), Map.of(pickaxeTemplate.id(), pickaxeTemplate), Map.of(oakHandle.id(), oakHandle));

		// Act
		GeneratedState generated = generator.generate(state, tagGraph);

		// Assert
		var generatedPickaxe = generated.equipment().values().stream().findFirst().orElseThrow();
		var finalProps = generatedPickaxe.properties();

		assertNotNull(finalProps);
		assertEquals(1, finalProps.size(), "Should only contain properties from the template.");
		assertTrue(finalProps.containsKey("custom:template_prop"), "Template property should be present.");
		assertFalse(finalProps.containsKey("custom:part_prop"), "Property from the part should NOT be merged into the equipment DTO.");
	}


	// region Test Data Factories
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
		tagBuilder.add(id("forgero:pickaxe"), Set.of(id("forgero:tool")));

		return tagBuilder.build();
	}


	private NormalizedState.NormalizedMaterial materialWithProperties(String name, Map<String, JsonElement> properties, String... tags) {
		Set<OpenIdentifier> tagSet = Arrays.stream(tags).map(Utils::id).collect(Collectors.toSet());
		return new NormalizedState.NormalizedMaterial(id("forgero:" + name), name, tagSet, properties);
	}

	private NormalizedState.NormalizedShape shapeWithProperties(String name, Map<String, JsonElement> properties, String... tags) {
		Set<OpenIdentifier> tagSet = Arrays.stream(tags).map(Utils::id).collect(Collectors.toSet());
		return new NormalizedState.NormalizedShape(id("forgero:" + name), name, tagSet, properties);
	}

	private NormalizedState.NormalizedPartTemplate partTemplate(String name, String typeTag, String materialSlotType, String shapeSlotType) {
		return partTemplateWithProperties(name, typeTag, materialSlotType, shapeSlotType, null);
	}

	private NormalizedState.NormalizedPartTemplate partTemplateWithProperties(String name, String typeTag, String materialSlotType, String shapeSlotType, Map<String, JsonElement> properties) {
		return new NormalizedState.NormalizedPartTemplate(
				id("forgero:" + name), name, Set.of(id(typeTag)),
				new PartTemplateStructureData("forgero:{material.name}-{shape.name}",
						Map.of(
								"material", new PartTemplateStructureSlotData(id(materialSlotType), 1, null),
								"shape", new PartTemplateStructureSlotData(id(shapeSlotType), 1, null)
						)),
				List.of(), properties
		);
	}

	private NormalizedState.NormalizedStaticPart staticPartWithProperties(String name, Map<String, JsonElement> properties, String... tags) {
		Set<OpenIdentifier> tagSet = Arrays.stream(tags).map(Utils::id).collect(Collectors.toSet());
		return new NormalizedState.NormalizedStaticPart(id("forgero:" + name), name, tagSet, List.of(), properties);
	}

	private NormalizedState.NormalizedEquipmentTemplate equipmentTemplateWithProperties(String name, Map<String, EquipmentTemplateSlotData> slots, Map<String, JsonElement> properties, String... tags) {
		Set<OpenIdentifier> tagSet = Arrays.stream(tags).map(Utils::id).collect(Collectors.toSet());
		return new NormalizedState.NormalizedEquipmentTemplate(
				id("forgero:" + name), name, tagSet,
				new EquipmentTemplateStructureData("forgero:{handle.name}-pickaxe", slots),
				List.of(), properties
		);
	}
	// endregion
}
