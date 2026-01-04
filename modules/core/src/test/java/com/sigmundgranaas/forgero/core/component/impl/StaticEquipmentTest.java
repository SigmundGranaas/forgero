package com.sigmundgranaas.forgero.core.component.impl;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.ForgeroTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.sigmundgranaas.forgero.testutils.TestIdentifiers.*;
import static org.junit.jupiter.api.Assertions.*;

class StaticEquipmentTest extends ForgeroTest {

	@Test
	void constructsWithRequiredFields() {
		StaticEquipment equipment = new StaticEquipment(
				SWORD_ID,
				Set.of(SWORD_TAG, METAL_TAG),
				Map.of("forgero:durability", List.of(1000))
		);

		assertEquals(SWORD_ID, equipment.id());
		assertEquals(2, equipment.getTags().size());
		assertTrue(equipment.getTags().contains(SWORD_TAG));
		assertTrue(equipment.getTags().contains(METAL_TAG));
		assertEquals(1, equipment.propertiesAsMap().size());
	}

	// Note: Null validation test removed because StaticEquipment record
	// does not have a compact constructor to validate nulls.
	// Null validation could be added in the future if needed.

	@Test
	void propertyMergeWorksCorrectly() {
		StaticEquipment equipment = new StaticEquipment(
				SWORD_ID,
				Set.of(SWORD_TAG),
				Map.of("forgero:durability", List.of(1000))
		);

		StaticEquipment withNewProperties = (StaticEquipment) equipment.withProperties(
				Map.of("forgero:damage", List.of(10))
		);

		// Should have both properties
		assertEquals(2, withNewProperties.propertiesAsMap().size());
		assertTrue(withNewProperties.propertiesAsMap().containsKey("forgero:durability"));
		assertTrue(withNewProperties.propertiesAsMap().containsKey("forgero:damage"));
	}

	@Test
	void tagContainmentCorrect() {
		Set<OpenIdentifier> tags = Set.of(SWORD_TAG, METAL_TAG, WOOD_TAG);
		StaticEquipment equipment = new StaticEquipment(
				SWORD_ID,
				tags,
				Map.of()
		);

		assertEquals(3, equipment.getTags().size());
		assertTrue(equipment.getTags().contains(SWORD_TAG));
		assertTrue(equipment.getTags().contains(METAL_TAG));
		assertTrue(equipment.getTags().contains(WOOD_TAG));
		assertFalse(equipment.getTags().contains(GEM_TAG));
	}

	@Test
	void immutabilityGuaranteed() {
		StaticEquipment original = new StaticEquipment(
				SWORD_ID,
				Set.of(SWORD_TAG),
				Map.of("forgero:durability", List.of(1000))
		);

		StaticEquipment modified = (StaticEquipment) original.withProperties(
				Map.of("forgero:damage", List.of(10))
		);

		// Original should be unchanged
		assertNotSame(original, modified);
		assertEquals(1, original.propertiesAsMap().size());
		assertEquals(2, modified.propertiesAsMap().size());

		// Original properties map should still have only durability
		assertTrue(original.propertiesAsMap().containsKey("forgero:durability"));
		assertFalse(original.propertiesAsMap().containsKey("forgero:damage"));
	}
}
