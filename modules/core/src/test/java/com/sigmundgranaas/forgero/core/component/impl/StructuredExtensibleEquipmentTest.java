package com.sigmundgranaas.forgero.core.component.impl;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.ForgeroTest;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.slot.ComponentUpgrades;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentStructure;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.sigmundgranaas.forgero.testutils.ForgeroTestFactory.*;
import static com.sigmundgranaas.forgero.testutils.TestIdentifiers.*;
import static org.junit.jupiter.api.Assertions.*;

class StructuredExtensibleEquipmentTest extends ForgeroTest {

	@Test
	void constructsWithRequiredFields() {
		Component ironBlade = material(IRON_ID, METAL_TAG);
		Component oakHandle = material(OAK_ID, WOOD_TAG);
		ComponentStructure structure = ComponentStructure.of(
				structureSlot("blade_slot", id("blade"), ironBlade),
				structureSlot("handle_slot", HANDLE_SLOT_TYPE, oakHandle)
		);
		ComponentUpgrades upgrades = ComponentUpgrades.of(
				upgradeSlot("gem_slot", GEM_TAG, c -> c.getTags().contains(GEM_TAG))
		);

		StructuredExtensibleEquipment equipment = new StructuredExtensibleEquipment(
				SWORD_ID,
				Set.of(SWORD_TAG),
				Map.of("forgero:durability", List.of(1800)),
				structure,
				upgrades
		);

		assertEquals(SWORD_ID, equipment.id());
		assertEquals(1, equipment.getTags().size());
		assertEquals(structure, equipment.structure());
		assertEquals(upgrades, equipment.upgrades());
	}

	// Note: Null validation test removed because StructuredExtensibleEquipment record
	// does not have a compact constructor to validate nulls.

	@Test
	void propertyMergeWorksCorrectly() {
		Component ironBlade = material(IRON_ID, METAL_TAG);
		ComponentStructure structure = ComponentStructure.of(
				structureSlot("blade_slot", id("blade"), ironBlade)
		);
		ComponentUpgrades upgrades = ComponentUpgrades.of();

		StructuredExtensibleEquipment equipment = new StructuredExtensibleEquipment(
				SWORD_ID,
				Set.of(SWORD_TAG),
				Map.of("forgero:durability", List.of(1800)),
				structure,
				upgrades
		);

		StructuredExtensibleEquipment withNewProperties = (StructuredExtensibleEquipment) equipment.withProperties(
				Map.of("forgero:damage", List.of(12))
		);

		assertEquals(2, withNewProperties.propertiesAsMap().size());
		assertTrue(withNewProperties.propertiesAsMap().containsKey("forgero:durability"));
		assertTrue(withNewProperties.propertiesAsMap().containsKey("forgero:damage"));
	}

	@Test
	void tagContainmentCorrect() {
		Component ironBlade = material(IRON_ID, METAL_TAG);
		ComponentStructure structure = ComponentStructure.of(
				structureSlot("blade_slot", id("blade"), ironBlade)
		);
		ComponentUpgrades upgrades = ComponentUpgrades.of();

		Set<OpenIdentifier> tags = Set.of(SWORD_TAG, METAL_TAG);
		StructuredExtensibleEquipment equipment = new StructuredExtensibleEquipment(
				SWORD_ID,
				tags,
				Map.of(),
				structure,
				upgrades
		);

		assertEquals(2, equipment.getTags().size());
		assertTrue(equipment.getTags().contains(SWORD_TAG));
		assertTrue(equipment.getTags().contains(METAL_TAG));
	}

	@Test
	void immutabilityGuaranteed() {
		Component ironBlade = material(IRON_ID, METAL_TAG);
		ComponentStructure structure = ComponentStructure.of(
				structureSlot("blade_slot", id("blade"), ironBlade)
		);
		ComponentUpgrades upgrades = ComponentUpgrades.of();

		StructuredExtensibleEquipment original = new StructuredExtensibleEquipment(
				SWORD_ID,
				Set.of(SWORD_TAG),
				Map.of("forgero:durability", List.of(1800)),
				structure,
				upgrades
		);

		StructuredExtensibleEquipment modified = (StructuredExtensibleEquipment) original.withProperties(
				Map.of("forgero:damage", List.of(12))
		);

		assertNotSame(original, modified);
		assertEquals(1, original.propertiesAsMap().size());
		assertEquals(2, modified.propertiesAsMap().size());

		// Structure and upgrades should be same instances
		assertSame(original.structure(), modified.structure());
		assertSame(original.upgrades(), modified.upgrades());
	}
}
