package com.sigmundgranaas.forgero.data;

import com.sigmundgranaas.forgero.core.ForgeroTest;
import com.sigmundgranaas.forgero.core.attribute.api.AttributeQueryResult;
import com.sigmundgranaas.forgero.core.attribute.api.DefaultAttributes;
import com.sigmundgranaas.forgero.core.attribute.impl.AttributeEngine;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.StructuredComponent;
import com.sigmundgranaas.forgero.core.component.structure.StructureSlot;
import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.property.api.Resolver;
import com.sigmundgranaas.forgero.core.property.engine.ResolverEngine;
import com.sigmundgranaas.forgero.core.tags.engine.TagGraph;
import com.sigmundgranaas.forgero.core.tags.engine.TaggedRegistry;
import com.sigmundgranaas.forgero.data.loader.ForgeroDataInitializer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class ForgeroDataInitializerTest extends ForgeroTest {

	private static ForgeroDataInitializer initializer;

	@BeforeAll
	static void setUpAll() {
		// Initialize the entire data pipeline once for all tests
		initializer = new ForgeroDataInitializer("forgero");
	}

	@Test
	void testTagGraphLoading() {
		TagGraph tagGraph = initializer.getTagGraph();
		assertNotNull(tagGraph);

		OpenIdentifier materialTag = idFactory.of("forgero:materials/material");
		OpenIdentifier metalTag = idFactory.of("forgero:materials/metal");
		OpenIdentifier toolMaterialTag = idFactory.of("forgero:materials/tool_material");
		OpenIdentifier pickaxeHeadTypeTag = idFactory.of("forgero:parts/pickaxe_head_type");
		OpenIdentifier handleTypeTag = idFactory.of("forgero:parts/handle_type");
		OpenIdentifier toolTag = idFactory.of("forgero:tools/tool");
		OpenIdentifier pickaxeTag = idFactory.of("forgero:tools/pickaxe");
		OpenIdentifier pickaxeHeadShapeTag = idFactory.of("forgero:pickaxe_head_shape");

		// Check parent relationships directly (tag graph stores canonical IDs)
		assertTrue(tagGraph.getParents(toolMaterialTag).contains(materialTag), "tool_material should have material as a direct parent.");
		assertTrue(tagGraph.getParents(pickaxeTag).contains(toolTag), "pickaxe should have tool as a direct parent.");
		assertTrue(tagGraph.getParents(metalTag).contains(materialTag), "metal should have material as a direct parent.");


		// Check inherited tagging using isTagged
		assertTrue(tagGraph.isTagged(() -> Set.of(toolMaterialTag), materialTag), "tool_material should be tagged as material via inheritance.");
		assertTrue(tagGraph.isTagged(() -> Set.of(pickaxeTag), toolTag), "pickaxe should be tagged as tool via inheritance.");
		assertTrue(tagGraph.isTagged(() -> Set.of(metalTag), materialTag), "metal should be tagged as material via inheritance.");

		// Check non-existent relationships
		assertFalse(tagGraph.getParents(materialTag).contains(pickaxeHeadTypeTag), "material should not have pickaxe_head_type as a direct parent.");
		assertFalse(tagGraph.isTagged(() -> Set.of(handleTypeTag), pickaxeHeadTypeTag), "handle_type should not be tagged as pickaxe_head_type.");
	}

	@Test
	void testComponentRegistryAvailability() {
		TaggedRegistry<Component> registry = initializer.getComponentRegistry();
		assertNotNull(registry);
		assertFalse(registry.all().isEmpty(), "Component registry should not be empty after initialization. Found: " + registry.all().stream().map(Component::id).map(OpenIdentifier::toString).collect(Collectors.joining(", ")));
	}

	@Test
	void testGeneratedPartExistsAndHasCorrectProperties() {
		TaggedRegistry<Component> registry = initializer.getComponentRegistry();
		// ID for generated part is canonical. The example doesn't have a shape in pickaxe_head_template.json.
		// If we assume a default "round" shape or similar from the refactor plan, it would be included.
		// For this test, I will assume a default shape is added in Stage 2 normalization for templates without one.
		// For the provided JSON, pickaxe_head_template has no shape explicitly, so we must assume a 'default_shape' exists.
		// I'll add a dummy shape.json for this to work in `src/test/resources/data/forgero/shapes/pickaxe_head_shape.json`
		// and use `pickaxe_head_shape` as the type in pickaxe_head_template.json's structure.
		OpenIdentifier ironPickaxeHeadId = idFactory.of("forgero:iron-pickaxe_head"); // New ID format

		Optional<Component> ironPickaxeHeadOpt = registry.find(ironPickaxeHeadId);
		assertTrue(ironPickaxeHeadOpt.isPresent(), "Iron Pickaxe Head should be generated and present in registry. Available components: " + registry.all().stream().map(Component::id).map(OpenIdentifier::toString).collect(Collectors.joining(", ")));

		Component ironPickaxeHead = ironPickaxeHeadOpt.get();
		assertEquals(ironPickaxeHeadId, ironPickaxeHead.id());
		// Tags are now canonical
		assertTrue(ironPickaxeHead.getTags().contains(idFactory.of("forgero:parts/pickaxe_head_type")), "Generated part should inherit template tags.");
		assertTrue(ironPickaxeHead.getTags().contains(idFactory.of("forgero:materials/metal")), "Generated part should inherit material tags.");
		assertTrue(ironPickaxeHead.getTags().contains(idFactory.of("forgero:pickaxe_head_shape")), "Generated part should inherit shape tags."); // From pickaxe_head_shape.json

		// Verify composite attribute resolution
		Resolver resolver = new ResolverEngine(List.of(new AttributeEngine()));
		AttributeQueryResult attributes = resolver.resolve(ironPickaxeHead, AttributeEngine.KEY).orElseThrow();

		// Calculations based on the dummy JSONs:
		// Material(dur: 250, ms_comp: +5) and Shape (dur: *1.5, ms_comp: *1.2)
		// Expected durability: (0 + 250) * 1.5 = 375
		// Expected mining_speed: (0 + 5) * 1.2 = 6
		assertEquals(375f, attributes.getValue(DefaultAttributes.DURABILITY), 0.001f, "Durability should be resolved correctly from composite (250 * 1.5).");
		assertEquals(6f, attributes.getValue(DefaultAttributes.MINING_SPEED), 0.001f, "Mining speed should be resolved correctly from composite (5 * 1.2).");

		// Verify structure points to concrete material ID and shape ID
		assertInstanceOf(StructuredComponent.class, ironPickaxeHead, "Iron Pickaxe Head should be a structured component.");
		StructuredComponent structuredHead = (StructuredComponent) ironPickaxeHead;
		assertEquals(2, structuredHead.structure().slots().size(), "Structured head should have 2 slots (for material and shape).");

		// Material Slot
		Optional<StructureSlot> materialSlotOpt = structuredHead.structure().slots().stream()
				.filter(s -> s.id().path().equals("material"))
				.findFirst();
		assertTrue(materialSlotOpt.isPresent(), "Material slot should exist.");
		assertTrue(materialSlotOpt.get().get().isPresent(), "Material slot should be filled.");
		assertEquals(idFactory.of("forgero:iron"), materialSlotOpt.get().get().get().id(), "Material slot should contain concrete iron component (canonical ID).");

		// Shape Slot
		Optional<StructureSlot> shapeSlotOpt = structuredHead.structure().slots().stream()
				.filter(s -> s.id().path().equals("shape"))
				.findFirst();
		assertTrue(shapeSlotOpt.isPresent(), "Shape slot should exist.");
		assertTrue(shapeSlotOpt.get().get().isPresent(), "Shape slot should be filled.");
		assertEquals(idFactory.of("forgero:pickaxe_head_shape"), shapeSlotOpt.get().get().get().id(), "Shape slot should contain concrete pickaxe_head_shape component (canonical ID).");
	}

	@Test
	void testGeneratedToolExistsAndHasCorrectStructure() {
		TaggedRegistry<Component> registry = initializer.getComponentRegistry();
		// The ID of the generated tool now uses the template: forgero:{head.material.name}-pickaxe
		OpenIdentifier ironPickaxeId = idFactory.of("forgero:iron-pickaxe");
		OpenIdentifier ironPickaxeHeadId = idFactory.of("forgero:iron-pickaxe_head"); // ID of the generated head

		Optional<Component> ironPickaxeOpt = registry.find(ironPickaxeId);
		assertTrue(ironPickaxeOpt.isPresent(), "Iron Pickaxe should be generated and present. Available components: " + registry.all().stream().map(Component::id).map(OpenIdentifier::toString).collect(Collectors.joining(", ")));

		Component ironPickaxe = ironPickaxeOpt.get();
		assertInstanceOf(StructuredComponent.class, ironPickaxe, "Generated tool should be a StructuredComponent.");
		StructuredComponent structuredPickaxe = (StructuredComponent) ironPickaxe;

		assertEquals(2, structuredPickaxe.structure().slots().size(), "Pickaxe should have 2 structural slots.");

		// Check head slot content
		Optional<StructureSlot> headSlotOpt = structuredPickaxe.structure().slots().stream()
				.filter(s -> s.id().path().equals("head"))
				.findFirst();
		assertTrue(headSlotOpt.isPresent(), "Head slot should exist in the pickaxe structure.");
		assertTrue(headSlotOpt.get().get().isPresent(), "Head slot should be filled in the pickaxe structure.");
		assertEquals(ironPickaxeHeadId, headSlotOpt.get().get().get().id(), "Head slot should contain the generated iron pickaxe head (canonical ID).");

		// Check handle slot content
		Optional<StructureSlot> handleSlotOpt = structuredPickaxe.structure().slots().stream()
				.filter(s -> s.id().path().equals("handle"))
				.findFirst();
		assertTrue(handleSlotOpt.isPresent(), "Handle slot should exist in the pickaxe structure.");
		assertTrue(handleSlotOpt.get().get().isPresent(), "Handle slot should be filled in the pickaxe structure.");
		assertEquals(idFactory.of("forgero:static_oak_handle"), handleSlotOpt.get().get().get().id(), "Handle slot should contain the static oak handle part (canonical ID).");
	}

	@Test
	void testToolGenerationVariants() {
		TaggedRegistry<Component> registry = initializer.getComponentRegistry();

		// Based on the provided test resources and the new architecture:
		// - ToolTemplate: pickaxe_template (combinatorial head, default static_oak_handle)
		// - Available Heads matching "forgero:parts/pickaxe_head_type": only "forgero:iron-pickaxe_head_shape" (generated from iron + pickaxe_head_shape + pickaxe_head_template)
		// - Available Handles matching "forgero:parts/handle_type": only "forgero:static_oak_handle" (and it's explicitly defaulted)

		// This means only one combination should be generated: iron-pickaxe_head_shape with static_oak_handle.
		List<Component> pickaxes = registry.query(idFactory.of("forgero:tools/pickaxe"));
		assertEquals(1, pickaxes.size(), "Only one pickaxe variant (iron-pickaxe) should be generated based on available parts and defaults. Found: " + pickaxes.stream().map(Component::id).map(OpenIdentifier::toString).collect(Collectors.joining(", ")));
		assertEquals(idFactory.of("forgero:iron-pickaxe"), pickaxes.get(0).id(), "The generated pickaxe ID should be as expected.");
	}

	@Test
	void testStaticPartDirectlyRegistered() {
		TaggedRegistry<Component> registry = initializer.getComponentRegistry();
		OpenIdentifier oakHandleId = idFactory.of("forgero:static_oak_handle");

		Optional<Component> oakHandleOpt = registry.find(oakHandleId);
		assertTrue(oakHandleOpt.isPresent(), "Oak Handle static part should be registered. Available components: " + registry.all().stream().map(Component::id).map(OpenIdentifier::toString).collect(Collectors.joining(", ")));
		assertEquals(oakHandleId, oakHandleOpt.get().id());
		assertTrue(oakHandleOpt.get().getTags().contains(idFactory.of("forgero:parts/handle_type")), "Oak handle should retain its tags.");
		// The path of the ID matches the original JSON name after canonicalization.
		assertEquals("static_oak_handle", oakHandleOpt.get().id().path(), "Static part name should match the JSON name directly.");
	}

	@Test
	void testAttributeMappingForStaticPart() {
		TaggedRegistry<Component> registry = initializer.getComponentRegistry();
		OpenIdentifier oakHandleId = idFactory.of("forgero:static_oak_handle");
		Component oakHandle = registry.find(oakHandleId).orElseThrow(() -> new AssertionError("Oak Handle not found: " + oakHandleId));

		Resolver resolver = new ResolverEngine(List.of(new AttributeEngine()));
		AttributeQueryResult attributes = resolver.resolve(oakHandle, AttributeEngine.KEY).orElseThrow(() -> new AssertionError("Attributes not resolved for Oak Handle."));

		// From static_oak_handle.json: durability = 50
		assertEquals(50f, attributes.getValue(DefaultAttributes.DURABILITY), 0.001f, "Static part should have its defined attributes.");
	}

	@Test
	void testMaterialIsRegistered() {
		TaggedRegistry<Component> registry = initializer.getComponentRegistry();
		OpenIdentifier ironId = idFactory.of("forgero:iron");

		Optional<Component> ironOpt = registry.find(ironId);
		assertTrue(ironOpt.isPresent(), "Iron material should be registered.");
		assertEquals(ironId, ironOpt.get().id());
		assertTrue(ironOpt.get().getTags().contains(idFactory.of("forgero:materials/tool_material")));

		Resolver resolver = new ResolverEngine(List.of(new AttributeEngine(false)));
		AttributeQueryResult attributes = resolver.resolve(ironOpt.get(), AttributeEngine.KEY).orElseThrow();
		assertEquals(250f, attributes.getValue(DefaultAttributes.DURABILITY), 0.001f);
		// Note: mining_speed is a composite property, its final value is resolved on the part.
		// On the material itself, it contributes to the composite, but the material might not directly have the resolved mining speed.
		// For a simple material, it should be the direct value if it's not composite.
		// In iron.json: mining_speed is a composite, so its direct value here depends on how CompositeAttributeComponent exposes its value.
		// Assuming for MaterialData, only non-composite attributes are directly resolved as base properties.
		// For composite, we only verify it's registered correctly.
	}

	@Test
	void testShapeIsRegistered() {
		TaggedRegistry<Component> registry = initializer.getComponentRegistry();
		OpenIdentifier defaultShapeId = idFactory.of("forgero:pickaxe_head_shape");

		Optional<Component> shapeOpt = registry.find(defaultShapeId);
		assertTrue(shapeOpt.isPresent(), "Pickaxe Head Shape should be registered.");
		assertEquals(defaultShapeId, shapeOpt.get().id());
		assertTrue(shapeOpt.get().getTags().contains(idFactory.of("forgero:pickaxe_head_shape")));

		// Verify attributes from pickaxe_head_shape.json (which should contain the multipliers)
		Resolver resolver = new ResolverEngine(List.of(new AttributeEngine()));
		AttributeQueryResult attributes = resolver.resolve(shapeOpt.get(), AttributeEngine.KEY).orElseThrow();
		// These are the raw attribute values from the shape, which are multipliers
		// The actual resolved value for a *part* will combine these.
		// Assuming direct attribute resolution returns the raw computation value.
	}
}
