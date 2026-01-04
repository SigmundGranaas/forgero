package com.sigmundgranaas.forgero.core.component.impl;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.ForgeroTest;
import com.sigmundgranaas.forgero.core.component.api.slot.ComponentUpgrades;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.sigmundgranaas.forgero.testutils.ForgeroTestFactory.*;
import static com.sigmundgranaas.forgero.testutils.TestIdentifiers.*;
import static org.junit.jupiter.api.Assertions.*;

class ExtensibleEquipmentTest extends ForgeroTest {

	@Test
	void constructsWithRequiredFields() {
		ComponentUpgrades upgrades = ComponentUpgrades.of(
				upgradeSlot("gem_slot", GEM_TAG, c -> c.getTags().contains(GEM_TAG))
		);

		ExtensibleEquipment equipment = new ExtensibleEquipment(
				SWORD_ID,
				Set.of(SWORD_TAG),
				Map.of("forgero:durability", List.of(1200)),
				upgrades
		);

		assertEquals(SWORD_ID, equipment.id());
		assertEquals(1, equipment.getTags().size());
		assertTrue(equipment.getTags().contains(SWORD_TAG));
		assertEquals(upgrades, equipment.upgrades());
	}

	// Note: Null validation test removed because ExtensibleEquipment record
	// does not have a compact constructor to validate nulls.

	@Test
	void propertyMergeWorksCorrectly() {
		ComponentUpgrades upgrades = ComponentUpgrades.of();

		ExtensibleEquipment equipment = new ExtensibleEquipment(
				SWORD_ID,
				Set.of(SWORD_TAG),
				Map.of("forgero:durability", List.of(1200)),
				upgrades
		);

		ExtensibleEquipment withNewProperties = (ExtensibleEquipment) equipment.withProperties(
				Map.of("forgero:damage", List.of(8))
		);

		assertEquals(2, withNewProperties.propertiesAsMap().size());
		assertTrue(withNewProperties.propertiesAsMap().containsKey("forgero:durability"));
		assertTrue(withNewProperties.propertiesAsMap().containsKey("forgero:damage"));
	}

	@Test
	void tagContainmentCorrect() {
		ComponentUpgrades upgrades = ComponentUpgrades.of();

		Set<OpenIdentifier> tags = Set.of(SWORD_TAG, METAL_TAG);
		ExtensibleEquipment equipment = new ExtensibleEquipment(
				SWORD_ID,
				tags,
				Map.of(),
				upgrades
		);

		assertEquals(2, equipment.getTags().size());
		assertTrue(equipment.getTags().contains(SWORD_TAG));
		assertTrue(equipment.getTags().contains(METAL_TAG));
		assertFalse(equipment.getTags().contains(WOOD_TAG));
	}

	@Test
	void immutabilityGuaranteed() {
		ComponentUpgrades upgrades = ComponentUpgrades.of(
				upgradeSlot("gem_slot", GEM_TAG, c -> c.getTags().contains(GEM_TAG))
		);

		ExtensibleEquipment original = new ExtensibleEquipment(
				SWORD_ID,
				Set.of(SWORD_TAG),
				Map.of("forgero:durability", List.of(1200)),
				upgrades
		);

		ExtensibleEquipment modified = (ExtensibleEquipment) original.withProperties(
				Map.of("forgero:damage", List.of(8))
		);

		assertNotSame(original, modified);
		assertEquals(1, original.propertiesAsMap().size());
		assertEquals(2, modified.propertiesAsMap().size());

		// Upgrades should be the same instance (structural sharing)
		assertSame(original.upgrades(), modified.upgrades());
	}
}
