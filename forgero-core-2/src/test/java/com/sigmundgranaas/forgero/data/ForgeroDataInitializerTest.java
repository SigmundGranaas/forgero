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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ForgeroDataInitializerTest extends ForgeroTest {

	private static ForgeroDataInitializer initializer;
	private static TaggedRegistry<Component> componentRegistry;
	private static Resolver resolver;

	@BeforeAll
	static void setUpAll() {
		initializer = new ForgeroDataInitializer("forgero");
		componentRegistry = initializer.getComponentRegistry();
		resolver = new ResolverEngine(List.of(new AttributeEngine()));
	}

	private OpenIdentifier id(String id) {
		return idFactory.of(id);
	}

	@Test
	@DisplayName("Test Tag Graph Loading")
	void testTagGraphLoading() {
		TagGraph tagGraph = initializer.getTagGraph();
		assertNotNull(tagGraph, "TagGraph should not be null after initialization.");
		assertTrue(tagGraph.getParents(id("forgero:materials/metal")).contains(id("forgero:materials/material")),
				"The 'metal' tag should have 'material' as a parent.");
	}

	@Test
	@DisplayName("Test Component Registry Availability")
	void testComponentRegistryAvailability() {
		assertNotNull(componentRegistry, "Component registry should be initialized.");
		assertFalse(componentRegistry.all().isEmpty(), "Component registry should be populated with data.");
	}

	@Test
	@DisplayName("Test Generated Part: Iron Pickaxe Head")
	void testGeneratedPartExistsAndHasCorrectProperties() {
		OpenIdentifier partId = id("forgero:iron-pickaxe_head");
		Component part = componentRegistry.find(partId)
				.orElseThrow(() -> new AssertionError("Part not found in registry: " + partId));

		assertEquals(partId, part.id());
		Set<OpenIdentifier> expectedTags = Set.of(
				id("forgero:parts/pickaxe_head_type"),
				id("forgero:materials/metal"),
				id("forgero:pickaxe_head_shape")
		);
		assertTrue(part.getTags().containsAll(expectedTags), "Part should contain all expected tags.");

		AttributeQueryResult attributes = resolver.resolve(part, AttributeEngine.KEY)
				.orElseThrow(() -> new AssertionError("Could not resolve attributes for " + partId));

		assertEquals(375f, attributes.getValue(DefaultAttributes.DURABILITY), 0.001f, "Durability should be resolved correctly from composite (250 * 1.5).");
		assertEquals(6f, attributes.getValue(DefaultAttributes.MINING_SPEED), 0.001f, "Mining speed should be resolved correctly from composite (5 * 1.2).");

		assertInstanceOf(StructuredComponent.class, part, "Part should be a structured component.");
		assertEquals(2, ((StructuredComponent) part).structure().slots().size(), "Structured part should have 2 slots (material and shape).");
	}

	@Test
	@DisplayName("Test Generated Tool: Iron Pickaxe")
	void testGeneratedToolExistsAndHasCorrectStructure() {
		OpenIdentifier toolId = id("forgero:iron-pickaxe");
		Component tool = componentRegistry.find(toolId)
				.orElseThrow(() -> new AssertionError("Tool not found in registry: " + toolId));

		assertInstanceOf(StructuredComponent.class, tool, "Tool must be a structured component.");
		StructuredComponent structuredTool = (StructuredComponent) tool;

		assertEquals(2, structuredTool.structure().slots().size(), "Tool structure should have 2 slots (head and handle).");

		Component head = structuredTool.structure().slots().stream().map(StructureSlot::get).flatMap(Optional::stream).filter(s -> s.id().equals(id("forgero:iron-pickaxe_head"))).findFirst()
				.orElseThrow(() -> new AssertionError("Head slot should be filled."));
		assertEquals(id("forgero:iron-pickaxe_head"), head.id());

		Component handle = structuredTool.structure().slots().stream().map(StructureSlot::get).flatMap(Optional::stream).filter(s -> s.id().equals(id("forgero:static_oak_handle"))).findFirst()
				.orElseThrow(() -> new AssertionError("Handle slot should be filled."));
		assertEquals(id("forgero:static_oak_handle"), handle.id());
	}

	@Test
	@DisplayName("Test Part and Tool Variants")
	void testVariantPartsAreGeneratedButVariantToolsAreNot() {
		// Check that all part variants are generated
		assertExists(id("forgero:iron-pickaxe_head"), "Default iron pickaxe head part");
		assertExists(id("forgero:iron-pickaxe_head_cast"), "Cast iron pickaxe head part");
		assertExists(id("forgero:iron-mastercrafted_pickaxe_head"), "Mastercrafted iron pickaxe head part");

		// Check that only the default tool is generated
		List<Component> pickaxes = componentRegistry.query(id("forgero:tools/pickaxe"));
		assertEquals(1, pickaxes.size(), "Only one default pickaxe should be generated.");
		assertEquals(id("forgero:iron-pickaxe"), pickaxes.get(0).id(), "The generated pickaxe should be the default iron one.");

		// Explicitly check that a tool with a non-default part was NOT generated
		assertNotExists(id("forgero:iron-cast-pickaxe"), "A pickaxe with a non-default part (cast)");
		assertNotExists(id("forgero:iron-mastercrafted-pickaxe"), "A pickaxe with a non-default part (mastercrafted)");
	}

	@Test
	@DisplayName("Test Static Part Registration")
	void testStaticPartDirectlyRegistered() {
		OpenIdentifier staticPartId = id("forgero:static_oak_handle");
		Component staticPart = assertExists(staticPartId, "Oak Handle static part");
		assertTrue(staticPart.getTags().contains(id("forgero:parts/handle_type")), "Static part should have the correct type tag.");
	}

	private Component assertExists(OpenIdentifier id, String name) {
		Optional<Component> componentOpt = componentRegistry.find(id);
		assertTrue(componentOpt.isPresent(), name + " should be present in the registry with id: " + id);
		return componentOpt.get();
	}

	private void assertNotExists(OpenIdentifier id, String name) {
		assertFalse(componentRegistry.find(id).isPresent(), name + " should NOT be present in the registry with id: " + id);
	}
}
