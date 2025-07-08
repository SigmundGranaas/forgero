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
import com.sigmundgranaas.forgero.data.v3.dto.ForgeroDataInitializer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
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

		// Assert some basic tag relationships from test resources
		// Tag IDs are now canonical: "forgero:material" instead of "forgero:materials/material"
		OpenIdentifier materialTag = idFactory.of("forgero:material");
		OpenIdentifier toolMaterialTag = idFactory.of("forgero:tool_material");
		OpenIdentifier pickaxeHeadTypeTag = idFactory.of("forgero:pickaxe_head_type");
		OpenIdentifier handleTypeTag = idFactory.of("forgero:handle_type");
		OpenIdentifier toolTag = idFactory.of("forgero:tool");
		OpenIdentifier pickaxeTag = idFactory.of("forgero:pickaxe");


		// Check parent relationships directly (tag graph stores canonical IDs)
		assertTrue(tagGraph.getParents(toolMaterialTag).contains(materialTag), "tool_material should have material as a direct parent.");
		assertTrue(tagGraph.getParents(pickaxeTag).contains(toolTag), "pickaxe should have tool as a direct parent.");

		// Check inherited tagging using isTagged
		assertTrue(tagGraph.isTagged(() -> java.util.Set.of(toolMaterialTag), materialTag), "tool_material should be tagged as material via inheritance.");
		assertTrue(tagGraph.isTagged(() -> java.util.Set.of(pickaxeTag), toolTag), "pickaxe should be tagged as tool via inheritance.");

		// Check non-existent relationships
		assertFalse(tagGraph.getParents(materialTag).contains(pickaxeHeadTypeTag), "material should not have pickaxe_head_type as a direct parent.");
		assertFalse(tagGraph.isTagged(() -> java.util.Set.of(handleTypeTag), pickaxeHeadTypeTag), "handle_type should not be tagged as pickaxe_head_type.");
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
		// ID for generated part is canonical
		OpenIdentifier ironPickaxeHeadId = idFactory.of("forgero:iron-pickaxe_head");

		Optional<Component> ironPickaxeHeadOpt = registry.find(ironPickaxeHeadId);
		assertTrue(ironPickaxeHeadOpt.isPresent(), "Iron Pickaxe Head should be generated and present in registry. Available components: " + registry.all().stream().map(Component::id).map(OpenIdentifier::toString).collect(Collectors.joining(", ")));

		Component ironPickaxeHead = ironPickaxeHeadOpt.get();
		assertEquals(ironPickaxeHeadId, ironPickaxeHead.id());
		// Tags are now canonical
		assertTrue(ironPickaxeHead.getTags().contains(idFactory.of("forgero:pickaxe_head_type")), "Generated part should inherit template tags.");
		// id.name() should return the simple name, which is the path for canonical IDs
		assertEquals("iron-pickaxe_head", ironPickaxeHead.id().name(), "Generated part name should match the combined pattern.");


		// Verify composite attribute resolution
		// The ResolverEngine needs its DataTypeEngines initialized as well.
		Resolver resolver = new ResolverEngine(List.of(new AttributeEngine()));
		AttributeQueryResult attributes = resolver.resolve(ironPickaxeHead, AttributeEngine.KEY).orElseThrow();

		// Calculations based on the dummy JSONs:
		// Iron material: durability = 250, mining_speed composite +5 (using ADDITION_OPERATOR implicitly)
		// Pickaxe Head template: durability composite x1.5, mining_speed composite x1.2
		// Expected durability: (0 + 250) * 1.5 = 375
		// Expected mining_speed: (0 + 5) * 1.2 = 6
		assertEquals(375f, attributes.getValue(DefaultAttributes.DURABILITY), 0.001f, "Durability should be resolved correctly from composite (250 * 1.5).");
		assertEquals(6f, attributes.getValue(DefaultAttributes.MINING_SPEED), 0.001f, "Mining speed should be resolved correctly from composite (5 * 1.2).");

		// Verify structure points to concrete material ID
		assertInstanceOf(StructuredComponent.class, ironPickaxeHead, "Iron Pickaxe Head should be a structured component.");
		StructuredComponent structuredHead = (StructuredComponent) ironPickaxeHead;
		assertEquals(1, structuredHead.structure().slots().size(), "Structured head should have 1 slot (for material).");
		StructureSlot materialSlot = structuredHead.structure().slots().get(0);
		assertEquals(idFactory.of("material_slot"), materialSlot.id(), "Material slot should have the correct ID (canonical).");
		assertTrue(materialSlot.get().isPresent(), "Material slot should be filled.");
		// The component ID in the slot should be canonical
		assertEquals(idFactory.of("forgero:iron"), materialSlot.get().get().id(), "Material slot should contain concrete iron component (canonical ID).");
	}

	@Test
	void testGeneratedToolExistsAndHasCorrectStructure() {
		TaggedRegistry<Component> registry = initializer.getComponentRegistry();
		// The ID of the generated tool combines the tool template name and the parts' names
		OpenIdentifier ironPickaxeOakHandleId = idFactory.of("forgero:pickaxe-iron_pickaxe_head-oak_handle");
		OpenIdentifier ironPickaxeHeadId = idFactory.of("forgero:iron-pickaxe_head");

		Optional<Component> ironPickaxeOpt = registry.find(ironPickaxeOakHandleId);
		assertTrue(ironPickaxeOpt.isPresent(), "Iron Pickaxe with Oak Handle should be generated and present. Available components: " + registry.all().stream().map(Component::id).map(OpenIdentifier::toString).collect(Collectors.joining(", ")));

		Component ironPickaxe = ironPickaxeOpt.get();
		assertInstanceOf(StructuredComponent.class, ironPickaxe, "Generated tool should be a StructuredComponent.");
		StructuredComponent structuredPickaxe = (StructuredComponent) ironPickaxe;

		assertEquals(2, structuredPickaxe.structure().slots().size(), "Pickaxe should have 2 structural slots.");

		// Check head slot content
		Optional<StructureSlot> headSlotOpt = structuredPickaxe.structure().slots().stream()
				.filter(s -> s.id().path().equals("head")) // Slot ID as defined in ToolTemplateData (is canonical now)
				.findFirst();
		assertTrue(headSlotOpt.isPresent(), "Head slot should exist in the pickaxe structure.");
		assertTrue(headSlotOpt.get().get().isPresent(), "Head slot should be filled in the pickaxe structure.");
		assertEquals(ironPickaxeHeadId, headSlotOpt.get().get().get().id(), "Head slot should contain the generated iron pickaxe head (canonical ID).");

		// Check handle slot content
		Optional<StructureSlot> handleSlotOpt = structuredPickaxe.structure().slots().stream()
				.filter(s -> s.id().path().equals("handle")) // Slot ID as defined in ToolTemplateData (is canonical now)
				.findFirst();
		assertTrue(handleSlotOpt.isPresent(), "Handle slot should exist in the pickaxe structure.");
		assertTrue(handleSlotOpt.get().get().isPresent(), "Handle slot should be filled in the pickaxe structure.");
		assertEquals(idFactory.of("forgero:static_oak_handle"), handleSlotOpt.get().get().get().id(), "Handle slot should contain the static oak handle part (canonical ID).");
	}

	@Test
	void testToolGenerationVariants() {
		TaggedRegistry<Component> registry = initializer.getComponentRegistry();

		// Based on the provided test resources:
		// - ToolTemplate: pickaxe_template (combinatorial head, default static_oak_handle)
		// - Available Heads matching "forgero:pickaxe_head_type": only "forgero:iron-pickaxe_head" (generated from iron + pickaxe_head_template)
		// - Available Handles matching "forgero:handle_type": only "forgero:static_oak_handle" (and it's explicitly defaulted)

		// This means only one combination should be generated: iron-pickaxe_head with static_oak_handle.
		// Query for tools with 'pickaxe' tag (now canonical: "forgero:pickaxe")
		List<Component> pickaxes = registry.query(idFactory.of("forgero:pickaxe"));
		assertEquals(1, pickaxes.size(), "Only one pickaxe variant (iron-pickaxe_head-oak_handle) should be generated based on available parts and defaults. Found: " + pickaxes.stream().map(Component::id).map(OpenIdentifier::toString).collect(Collectors.joining(", ")));
		assertEquals(idFactory.of("forgero:pickaxe-iron_pickaxe_head-oak_handle"), pickaxes.get(0).id(), "The generated pickaxe ID should be as expected.");
	}

	@Test
	void testStaticPartDirectlyRegistered() {
		TaggedRegistry<Component> registry = initializer.getComponentRegistry();
		// ID from file: static_oak_handle.json, now canonical: forgero:static_oak_handle
		OpenIdentifier oakHandleId = idFactory.of("forgero:static_oak_handle");

		Optional<Component> oakHandleOpt = registry.find(oakHandleId);
		assertTrue(oakHandleOpt.isPresent(), "Oak Handle static part should be registered. Available components: " + registry.all().stream().map(Component::id).map(OpenIdentifier::toString).collect(Collectors.joining(", ")));
		assertEquals(oakHandleId, oakHandleOpt.get().id());
		// Tag is now canonical
		assertTrue(oakHandleOpt.get().getTags().contains(idFactory.of("forgero:handle_type")), "Oak handle should retain its tags.");
		assertEquals("static_oak_handle", oakHandleOpt.get().id().name(), "Static part name should match the JSON name directly.");
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
}
