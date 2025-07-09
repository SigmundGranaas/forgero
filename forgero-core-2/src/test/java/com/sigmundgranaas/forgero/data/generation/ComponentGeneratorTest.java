package com.sigmundgranaas.forgero.data.generation;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.tags.engine.TagGraph;
import com.sigmundgranaas.forgero.common.tags.engine.TagGraphBuilder;
import com.sigmundgranaas.forgero.core.ForgeroTest;
import com.sigmundgranaas.forgero.data.Utils;
import com.sigmundgranaas.forgero.data.generation.api.ComponentGenerator;
import com.sigmundgranaas.forgero.data.generation.api.GeneratedState;
import com.sigmundgranaas.forgero.data.generation.impl.ComponentGeneratorImpl;
import com.sigmundgranaas.forgero.data.loading.api.data.PropertyData;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeDataImpl;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.ComputationData;
import com.sigmundgranaas.forgero.data.loading.api.data.feature.FeatureData;
import com.sigmundgranaas.forgero.data.loading.api.data.feature.VeinMiningFeatureData;
import com.sigmundgranaas.forgero.data.loading.api.data.feature.VeinMiningSelectorData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.EquipmentTemplateSlotData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.EquipmentTemplateStructureData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.PartTemplateStructureData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.PartTemplateStructureSlotData;
import com.sigmundgranaas.forgero.data.processing.api.NormalizedState;
import org.jetbrains.annotations.Nullable;
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
		// Ensure property codecs are registered for the test environment
		com.sigmundgranaas.forgero.core.property.api.PropertyRegistry.getInstance().reset();
		com.sigmundgranaas.forgero.data.loading.impl.codec.FeatureCodecs.VEIN_MINING_FEATURE_CODEC.toString(); // Initialize static block

		generator = new ComponentGeneratorImpl(idFactory);
		tagGraph = buildTestTagGraph();
	}

	// Helper to create an AttributeData object
	private AttributeData createAttributeData(String id, String type, float value) {
		return new AttributeDataImpl(
				id("forgero:" + id),
				id("forgero:" + type),
				new ComputationData(value, "forgero:addition", "forgero:base"),
				null, null
		);
	}

	// Helper to create a FeatureData object
	private FeatureData createFeatureData(String type, String title) {
		return new VeinMiningFeatureData(
				id("forgero:" + type),
				title,
				"desc",
				new VeinMiningSelectorData(id("forgero:radius"), 1, id("forgero:tag")),
				null
		);
	}

	@Test
	void generatedPartMergesPropertiesFromAllSources() {
		// Setup
		Map<String, List<PropertyData>> materialProps = Map.of(
				ATTRIBUTES_KEY,  List.of(createAttributeData("mat_attr", "durability", 100))
		);

		Map<String, List<PropertyData>> shapeProps = Map.of(
				ATTRIBUTES_KEY, List.of(createAttributeData("shape_attr", "mining_speed", 5))
		);

		Map<String, List<PropertyData>> templateProps = Map.of(
				FEATURES_KEY, List.of(createFeatureData("vein_mining", "Vein Miner"))
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

		// Assert attributes are merged
		assertTrue(finalProps.containsKey(ATTRIBUTES_KEY));
		List<PropertyData> finalAttrs = finalProps.get(ATTRIBUTES_KEY);
		assertEquals(2, finalAttrs.size(), "Should have one attribute from material and one from shape.");

		// Assert features are present
		assertTrue(finalProps.containsKey(FEATURES_KEY));
		List<PropertyData> finalFeatures = finalProps.get(FEATURES_KEY);
		assertEquals(1, finalFeatures.size());
	}

	@Test
	void generatedPartAttributeOverride() {
		// Setup: Material and Shape both define the same attribute ID. Shape should win.
		Map<String, List<PropertyData>> materialProps = Map.of(
				ATTRIBUTES_KEY, List.of(createAttributeData("shared_attr", "durability", 100))
		);

		Map<String, List<PropertyData>> shapeProps = Map.of(
				ATTRIBUTES_KEY, List.of(createAttributeData("shared_attr", "durability", 200))
		);

		var iron = materialWithProperties("iron", materialProps, "forgero:metal");
		var headShape = shapeWithProperties("pickaxe_head", shapeProps, "forgero:pickaxe_head_shape");
		var headTemplate = partTemplate("pickaxe_head_template", "forgero:parts/pickaxe_head_type", "forgero:tool_material", "forgero:pickaxe_head_shape");

		var state = new NormalizedState(Map.of(iron.id(), iron), Map.of(headShape.id(), headShape), Map.of(), Map.of(headTemplate.id(), headTemplate), Map.of(), Map.of());

		// Act
		GeneratedState generated = generator.generate(state, tagGraph);

		// Assert
		var finalProps = generated.parts().get(id("forgero:iron-pickaxe_head")).properties();
		assertNotNull(finalProps);
		List<AttributeData> finalAttrs = finalProps.get(ATTRIBUTES_KEY).stream().map(AttributeData.class::cast).toList();
		assertEquals(1, finalAttrs.size(), "Attributes with the same ID should be overridden, not duplicated.");
		assertEquals(200f, finalAttrs.get(0).computation().value(), "The shape's attribute value should override the material's.");
	}

	@Test
	void generatedEquipmentHasPropertiesOnlyFromTemplate() {
		// Setup
		var oakHandle = staticPartWithProperties("static_oak_handle", null, "forgero:parts/handle_type", "forgero:default_handle");

		Map<String, List<PropertyData>> templateProps = Map.of(
				FEATURES_KEY, List.of(createFeatureData("template_feature", "Template Feature"))
		);

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
		assertTrue(finalProps.containsKey(FEATURES_KEY), "Template feature should be present.");
		assertEquals(1, finalProps.get(FEATURES_KEY).size());
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


	private NormalizedState.NormalizedMaterial materialWithProperties(String name, Map<String, List<PropertyData>> properties, String... tags) {
		Set<OpenIdentifier> tagSet = Arrays.stream(tags).map(Utils::id).collect(Collectors.toSet());
		return new NormalizedState.NormalizedMaterial(id("forgero:" + name), name, tagSet, properties);
	}

	private NormalizedState.NormalizedShape shapeWithProperties(String name, Map<String, List<PropertyData>> properties, String... tags) {
		Set<OpenIdentifier> tagSet = Arrays.stream(tags).map(Utils::id).collect(Collectors.toSet());
		return new NormalizedState.NormalizedShape(id("forgero:" + name), name, tagSet, properties);
	}

	private NormalizedState.NormalizedPartTemplate partTemplate(String name, String typeTag, String materialSlotType, String shapeSlotType) {
		return partTemplateWithProperties(name, typeTag, materialSlotType, shapeSlotType, null);
	}

	private NormalizedState.NormalizedPartTemplate partTemplateWithProperties(String name, String typeTag, String materialSlotType, String shapeSlotType, @Nullable Map<String, List<PropertyData>> properties) {
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

	private NormalizedState.NormalizedStaticPart staticPartWithProperties(String name, @Nullable Map<String, List<PropertyData>> properties, String... tags) {
		Set<OpenIdentifier> tagSet = Arrays.stream(tags).map(Utils::id).collect(Collectors.toSet());
		return new NormalizedState.NormalizedStaticPart(id("forgero:" + name), name, tagSet, List.of(), properties);
	}

	private NormalizedState.NormalizedEquipmentTemplate equipmentTemplateWithProperties(String name, Map<String, EquipmentTemplateSlotData> slots, @Nullable Map<String, List<PropertyData>> properties, String... tags) {
		Set<OpenIdentifier> tagSet = Arrays.stream(tags).map(Utils::id).collect(Collectors.toSet());
		return new NormalizedState.NormalizedEquipmentTemplate(
				id("forgero:" + name), name, tagSet,
				new EquipmentTemplateStructureData("forgero:{handle.name}-pickaxe", slots),
				List.of(), properties
		);
	}
	// endregion
}
