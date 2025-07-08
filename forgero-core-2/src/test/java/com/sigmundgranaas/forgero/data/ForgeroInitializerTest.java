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
		initializer = new ForgeroDataInitializer("forgero");
	}

	@Test
	void testTagGraphLoading() {
		TagGraph tagGraph = initializer.getTagGraph();
		assertNotNull(tagGraph);
		OpenIdentifier materialTag = idFactory.of("forgero:materials/material");
		OpenIdentifier metalTag = idFactory.of("forgero:materials/metal");
		assertTrue(tagGraph.getParents(metalTag).contains(materialTag), "metal should have material as a direct parent.");
	}

	@Test
	void testComponentRegistryAvailability() {
		TaggedRegistry<Component> registry = initializer.getComponentRegistry();
		assertNotNull(registry);
		assertFalse(registry.all().isEmpty(), "Component registry should not be empty after initialization.");
	}

	@Test
	void testGeneratedPartExistsAndHasCorrectProperties() {
		TaggedRegistry<Component> registry = initializer.getComponentRegistry();
		OpenIdentifier ironPickaxeHeadId = idFactory.of("forgero:iron-pickaxe_head");

		Optional<Component> ironPickaxeHeadOpt = registry.find(ironPickaxeHeadId);
		assertTrue(ironPickaxeHeadOpt.isPresent(), "Iron Pickaxe Head should be generated and present in registry.");

		Component ironPickaxeHead = ironPickaxeHeadOpt.get();
		assertEquals(ironPickaxeHeadId, ironPickaxeHead.id());
		assertTrue(ironPickaxeHead.getTags().contains(idFactory.of("forgero:parts/pickaxe_head_type")));
		assertTrue(ironPickaxeHead.getTags().contains(idFactory.of("forgero:materials/metal")));
		assertTrue(ironPickaxeHead.getTags().contains(idFactory.of("forgero:pickaxe_head_shape")));

		Resolver resolver = new ResolverEngine(List.of(new AttributeEngine()));
		AttributeQueryResult attributes = resolver.resolve(ironPickaxeHead, AttributeEngine.KEY).orElseThrow();

		assertEquals(375f, attributes.getValue(DefaultAttributes.DURABILITY), 0.001f, "Durability should be resolved correctly from composite (250 * 1.5).");
		assertEquals(6f, attributes.getValue(DefaultAttributes.MINING_SPEED), 0.001f, "Mining speed should be resolved correctly from composite (5 * 1.2).");

		assertInstanceOf(StructuredComponent.class, ironPickaxeHead, "Iron Pickaxe Head should be a structured component.");
		StructuredComponent structuredHead = (StructuredComponent) ironPickaxeHead;
		assertEquals(2, structuredHead.structure().slots().size());
	}

	@Test
	void testGeneratedToolExistsAndHasCorrectStructure() {
		TaggedRegistry<Component> registry = initializer.getComponentRegistry();
		OpenIdentifier ironPickaxeId = idFactory.of("forgero:iron-pickaxe");
		OpenIdentifier ironPickaxeHeadId = idFactory.of("forgero:iron-pickaxe_head");

		Optional<Component> ironPickaxeOpt = registry.find(ironPickaxeId);
		assertTrue(ironPickaxeOpt.isPresent(), "Iron Pickaxe should be generated and present.");

		Component ironPickaxe = ironPickaxeOpt.get();
		assertInstanceOf(StructuredComponent.class, ironPickaxe);
		StructuredComponent structuredPickaxe = (StructuredComponent) ironPickaxe;

		assertEquals(2, structuredPickaxe.structure().slots().size());

		Optional<StructureSlot> headSlotOpt = structuredPickaxe.structure().slots().stream()
				.filter(s -> s.id().path().equals("head"))
				.findFirst();
		assertTrue(headSlotOpt.isPresent());
		assertTrue(headSlotOpt.get().get().isPresent());
		assertEquals(ironPickaxeHeadId, headSlotOpt.get().get().get().id());
	}

	@Test
	void testVariantPartsAreGeneratedButVariantToolsAreNot() {
		TaggedRegistry<Component> registry = initializer.getComponentRegistry();

		// Check that all part variants are generated
		assertTrue(registry.find(idFactory.of("forgero:iron-pickaxe_head")).isPresent(), "Default iron pickaxe head part should be generated.");
		assertTrue(registry.find(idFactory.of("forgero:iron-pickaxe_head_cast")).isPresent(), "Cast iron pickaxe head part should be generated.");
		assertTrue(registry.find(idFactory.of("forgero:iron-mastercrafted_pickaxe_head")).isPresent(), "Mastercrafted iron pickaxe head part should be generated.");

		// Check that only the default tool is generated
		List<Component> pickaxes = registry.query(idFactory.of("forgero:tools/pickaxe"));
		assertEquals(1, pickaxes.size(), "Only one pickaxe (the default iron one) should be generated.");
		assertEquals(idFactory.of("forgero:iron-pickaxe"), pickaxes.get(0).id());

		// Explicitly check that a tool with a non-default part was NOT generated
		OpenIdentifier castPickaxeId = idFactory.of("forgero:iron-cast-pickaxe"); // An ID that might be generated from a non-default part.
		assertFalse(registry.find(castPickaxeId).isPresent(), "A pickaxe with a non-default part should not be generated by default.");
	}

	@Test
	void testStaticPartDirectlyRegistered() {
		TaggedRegistry<Component> registry = initializer.getComponentRegistry();
		OpenIdentifier oakHandleId = idFactory.of("forgero:static_oak_handle");

		Optional<Component> oakHandleOpt = registry.find(oakHandleId);
		assertTrue(oakHandleOpt.isPresent(), "Oak Handle static part should be registered.");
		assertEquals(oakHandleId, oakHandleOpt.get().id());
		assertTrue(oakHandleOpt.get().getTags().contains(idFactory.of("forgero:parts/handle_type")));
	}
}
