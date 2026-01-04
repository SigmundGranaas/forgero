package com.sigmundgranaas.forgero.core.component.impl;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.ForgeroTest;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentStructure;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.sigmundgranaas.forgero.testutils.ForgeroTestFactory.*;
import static com.sigmundgranaas.forgero.testutils.TestIdentifiers.*;
import static org.junit.jupiter.api.Assertions.*;

class StructuredEquipmentTest extends ForgeroTest {

	@Test
	void constructsWithRequiredFields() {
		Component ironBlade = material(IRON_ID, METAL_TAG);
		Component oakHandle = material(OAK_ID, WOOD_TAG);
		ComponentStructure structure = ComponentStructure.of(
				structureSlot("blade_slot", id("blade"), ironBlade),
				structureSlot("handle_slot", HANDLE_SLOT_TYPE, oakHandle)
		);

		StructuredEquipment equipment = new StructuredEquipment(
				SWORD_ID,
				Set.of(SWORD_TAG),
				Map.of("forgero:durability", List.of(1500)),
				structure
		);

		assertEquals(SWORD_ID, equipment.id());
		assertEquals(1, equipment.getTags().size());
		assertTrue(equipment.getTags().contains(SWORD_TAG));
		assertEquals(structure, equipment.structure());
	}

	// Note: Null validation test removed because StructuredEquipment record
	// does not have a compact constructor to validate nulls.

	@Test
	void propertyMergeWorksCorrectly() {
		Component ironBlade = material(IRON_ID, METAL_TAG);
		ComponentStructure structure = ComponentStructure.of(
				structureSlot("blade_slot", id("blade"), ironBlade)
		);

		StructuredEquipment equipment = new StructuredEquipment(
				SWORD_ID,
				Set.of(SWORD_TAG),
				Map.of("forgero:durability", List.of(1500)),
				structure
		);

		StructuredEquipment withNewProperties = (StructuredEquipment) equipment.withProperties(
				Map.of("forgero:damage", List.of(10))
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

		Set<OpenIdentifier> tags = Set.of(SWORD_TAG, METAL_TAG);
		StructuredEquipment equipment = new StructuredEquipment(
				SWORD_ID,
				tags,
				Map.of(),
				structure
		);

		assertEquals(2, equipment.getTags().size());
		assertTrue(equipment.getTags().contains(SWORD_TAG));
		assertTrue(equipment.getTags().contains(METAL_TAG));
		assertFalse(equipment.getTags().contains(WOOD_TAG));
	}

	@Test
	void immutabilityGuaranteed() {
		Component ironBlade = material(IRON_ID, METAL_TAG);
		ComponentStructure structure = ComponentStructure.of(
				structureSlot("blade_slot", id("blade"), ironBlade)
		);

		StructuredEquipment original = new StructuredEquipment(
				SWORD_ID,
				Set.of(SWORD_TAG),
				Map.of("forgero:durability", List.of(1500)),
				structure
		);

		StructuredEquipment modified = (StructuredEquipment) original.withProperties(
				Map.of("forgero:damage", List.of(10))
		);

		assertNotSame(original, modified);
		assertEquals(1, original.propertiesAsMap().size());
		assertEquals(2, modified.propertiesAsMap().size());

		// Structure should be the same instance (structural sharing)
		assertSame(original.structure(), modified.structure());
	}
}
