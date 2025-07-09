package com.sigmundgranaas.forgero.data;

import com.sigmundgranaas.forgero.core.ForgeroTest;
import com.sigmundgranaas.forgero.core.attribute.api.AttributeQueryResult;
import com.sigmundgranaas.forgero.core.attribute.api.DefaultAttributes;
import com.sigmundgranaas.forgero.core.attribute.impl.AttributeEngine;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.CustomizableComponent;
import com.sigmundgranaas.forgero.core.component.api.StructuredComponent;
import com.sigmundgranaas.forgero.core.component.api.slot.UpgradeSlot;
import com.sigmundgranaas.forgero.core.component.api.structure.StructureSlot;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.property.api.Resolver;
import com.sigmundgranaas.forgero.core.property.engine.ResolverEngine;
import com.sigmundgranaas.forgero.common.tags.engine.TagGraph;
import com.sigmundgranaas.forgero.common.tags.engine.TaggedRegistry;
import com.sigmundgranaas.forgero.data.pipeline.api.ForgeroDataInitializer;
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
		resolver = new ResolverEngine();
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
		assertTrue(tagGraph.getParents(id("forgero:materials/armor_material")).contains(id("forgero:materials/material")),
				"The 'armor_material' tag should have 'material' as a parent.");
		assertTrue(tagGraph.getParents(id("forgero:materials/metal")).contains(id("forgero:materials/armor_material")),
				"The 'metal' tag should have 'armor_material' as a parent.");
		assertTrue(tagGraph.getParents(id("forgero:armor/chest_plate")).contains(id("forgero:armor/armor")),
				"The 'chest_plate' tag should have 'armor' as a parent.");
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

	@Test
	@DisplayName("Test Generated Armor Part: Iron Armor Plate")
	void testGeneratedArmorPartExistsAndHasCorrectProperties() {
		OpenIdentifier partId = id("forgero:iron-armor_plate_shape_plate");
		Component part = componentRegistry.find(partId)
				.orElseThrow(() -> new AssertionError("Part not found in registry: " + partId));

		assertEquals(partId, part.id());
		Set<OpenIdentifier> expectedTags = Set.of(
				id("forgero:parts/armor_plate_type"),
				id("forgero:materials/metal"),
				id("forgero:armor_plate_shape")
		);
		assertTrue(part.getTags().containsAll(expectedTags), "Armor part should contain all expected tags.");

		AttributeQueryResult attributes = resolver.resolve(part, AttributeEngine.KEY)
				.orElseThrow(() -> new AssertionError("Could not resolve attributes for " + partId));

		assertEquals(250f, attributes.getValue(DefaultAttributes.DURABILITY), 0.001f, "Durability should be resolved correctly from composite (250 * 1.0).");
		assertEquals(2f, attributes.getValue(DefaultAttributes.ARMOR), 0.001f, "Armor should be resolved correctly from composite (2 * 1.0).");
		assertEquals(0.5f, attributes.getValue(DefaultAttributes.ARMOR_TOUGHNESS), 0.001f, "Armor Toughness should be resolved correctly from composite (0.5 * 1.0).");

		assertInstanceOf(StructuredComponent.class, part, "Armor part should be a structured component.");
		assertEquals(2, ((StructuredComponent) part).structure().slots().size(), "Structured armor part should have 2 slots (material and shape).");
	}

	@Test
	@DisplayName("Test Generated Armor Part: Leather Armor Plate")
	void testGeneratedLeatherArmorPart() {
		OpenIdentifier partId = id("forgero:leather-armor_plate_shape_plate");
		Component part = componentRegistry.find(partId)
				.orElseThrow(() -> new AssertionError("Part not found in registry: " + partId));

		assertEquals(partId, part.id());
		Set<OpenIdentifier> expectedTags = Set.of(
				id("forgero:parts/armor_plate_type"),
				id("forgero:materials/armor_material"),
				id("forgero:armor_plate_shape")
		);
		assertTrue(part.getTags().containsAll(expectedTags), "Leather armor part should contain expected tags.");

		AttributeQueryResult attributes = resolver.resolve(part, AttributeEngine.KEY)
				.orElseThrow(() -> new AssertionError("Could not resolve attributes for " + partId));

		assertEquals(75f, attributes.getValue(DefaultAttributes.DURABILITY), 0.001f, "Leather durability correct.");
		assertEquals(1.0f, attributes.getValue(DefaultAttributes.ARMOR), 0.001f, "Leather armor correct.");
		assertEquals(0.0f, attributes.getValue(DefaultAttributes.ARMOR_TOUGHNESS), 0.001f, "Leather armor toughness correct.");
	}

	@Test
	@DisplayName("Test Generated Armor Piece: Iron Chest Plate has correct structure and upgrade slots")
	void testGeneratedArmorPieceExistsAndHasCorrectStructure() {
		OpenIdentifier armorId = id("forgero:iron-chest_plate");
		Component armor = componentRegistry.find(armorId)
				.orElseThrow(() -> new AssertionError("Armor not found in registry: " + armorId));

		assertInstanceOf(StructuredComponent.class, armor, "Armor must be a structured component.");
		assertInstanceOf(CustomizableComponent.class, armor, "Armor must be a customizable component.");

		StructuredComponent structuredArmor = (StructuredComponent) armor;
		CustomizableComponent customizableArmor = (CustomizableComponent) armor;

		assertEquals(1, structuredArmor.structure().slots().size(), "Armor structure should have 1 slot (body).");

		Component body = structuredArmor.structure().slots().stream().map(StructureSlot::get).flatMap(Optional::stream).filter(s -> s.id().equals(id("forgero:iron-armor_plate_shape_plate"))).findFirst()
				.orElseThrow(() -> new AssertionError("Body slot should be filled with iron-armor_plate_shape_plate."));
		assertEquals(id("forgero:iron-armor_plate_shape_plate"), body.id());

		List<UpgradeSlot> upgradeSlots = customizableArmor.getUpgradeSlots();
		assertEquals(1, upgradeSlots.size(), "Armor should have 1 upgrade slot (trim).");

		UpgradeSlot trimSlot = upgradeSlots.get(0);
		assertEquals(id("forgero:chest_plate-trim_slot"), trimSlot.id(), "Trim slot should have correct ID.");
		assertEquals(id("forgero:upgrade_material"), trimSlot.type(), "Trim slot should have correct type.");
		assertTrue(trimSlot.get().isEmpty(), "Trim slot should be empty by default.");
		assertFalse(trimSlot.isRequired(), "Trim slot should not be required.");
	}

	@Test
	@DisplayName("Test Armor Variants")
	void testVariantArmorPartsAreGeneratedButVariantArmorPiecesAreNot() {
		assertExists(id("forgero:iron-armor_plate_shape_plate"), "Default iron armor plate part");
		assertExists(id("forgero:iron-heavy_armor_plate_shape_plate"), "Heavy iron armor plate part");
		assertExists(id("forgero:iron-light_armor_plate_shape_plate"), "Light iron armor plate part");
		assertExists(id("forgero:leather-armor_plate_shape_plate"), "Default leather armor plate part");
		assertExists(id("forgero:leather-heavy_armor_plate_shape_plate"), "Heavy leather armor plate part");
		assertExists(id("forgero:leather-light_armor_plate_shape_plate"), "Light leather armor plate part");


		List<Component> chestPlates = componentRegistry.query(id("forgero:armor/chest_plate"));
		assertEquals(2, chestPlates.size(), "Two default chest plates (iron, leather) should be generated.");
		assertTrue(chestPlates.stream().anyMatch(c -> c.id().equals(id("forgero:iron-chest_plate"))), "Iron chest plate should be present.");
		assertTrue(chestPlates.stream().anyMatch(c -> c.id().equals(id("forgero:leather-chest_plate"))), "Leather chest plate should be present.");


		assertNotExists(id("forgero:iron-heavy_chest_plate"), "A chest plate with a non-default part (heavy)");
		assertNotExists(id("forgero:iron-light_chest_plate"), "A chest plate with a non-default part (light)");
		assertNotExists(id("forgero:leather-heavy_chest_plate"), "A chest plate with a non-default part (heavy)");
		assertNotExists(id("forgero:leather-light_chest_plate"), "A chest plate with a non-default part (light)");
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
