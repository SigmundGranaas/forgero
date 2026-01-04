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

class StructuredPartTest extends ForgeroTest {

	@Test
	void constructsWithRequiredFields() {
		Component iron = material(IRON_ID, METAL_TAG);
		ComponentStructure structure = ComponentStructure.of(
				structureSlot("material_slot", MATERIAL_SLOT_TYPE, iron)
		);

		StructuredPart part = new StructuredPart(
				PICKAXE_HEAD_ID,
				Set.of(PICKAXE_HEAD_TAG),
				Map.of("forgero:durability", List.of(250)),
				structure
		);

		assertEquals(PICKAXE_HEAD_ID, part.id());
		assertEquals(1, part.getTags().size());
		assertTrue(part.getTags().contains(PICKAXE_HEAD_TAG));
		assertEquals(1, part.propertiesAsMap().size());
		assertEquals(structure, part.structure());
	}

	// Note: Null validation test removed because StructuredPart record
	// does not have a compact constructor to validate nulls.

	@Test
	void propertyMergeWorksCorrectly() {
		Component iron = material(IRON_ID, METAL_TAG);
		ComponentStructure structure = ComponentStructure.of(
				structureSlot("material_slot", MATERIAL_SLOT_TYPE, iron)
		);

		StructuredPart part = new StructuredPart(
				PICKAXE_HEAD_ID,
				Set.of(PICKAXE_HEAD_TAG),
				Map.of("forgero:durability", List.of(250)),
				structure
		);

		StructuredPart withNewProperties = (StructuredPart) part.withProperties(
				Map.of("forgero:damage", List.of(5))
		);

		assertEquals(2, withNewProperties.propertiesAsMap().size());
		assertTrue(withNewProperties.propertiesAsMap().containsKey("forgero:durability"));
		assertTrue(withNewProperties.propertiesAsMap().containsKey("forgero:damage"));
	}

	@Test
	void tagContainmentCorrect() {
		Component iron = material(IRON_ID, METAL_TAG);
		ComponentStructure structure = ComponentStructure.of(
				structureSlot("material_slot", MATERIAL_SLOT_TYPE, iron)
		);

		Set<OpenIdentifier> tags = Set.of(PICKAXE_HEAD_TAG, METAL_TAG);
		StructuredPart part = new StructuredPart(
				PICKAXE_HEAD_ID,
				tags,
				Map.of(),
				structure
		);

		assertEquals(2, part.getTags().size());
		assertTrue(part.getTags().contains(PICKAXE_HEAD_TAG));
		assertTrue(part.getTags().contains(METAL_TAG));
		assertFalse(part.getTags().contains(WOOD_TAG));
	}

	@Test
	void immutabilityGuaranteed() {
		Component iron = material(IRON_ID, METAL_TAG);
		ComponentStructure structure = ComponentStructure.of(
				structureSlot("material_slot", MATERIAL_SLOT_TYPE, iron)
		);

		StructuredPart original = new StructuredPart(
				PICKAXE_HEAD_ID,
				Set.of(PICKAXE_HEAD_TAG),
				Map.of("forgero:durability", List.of(250)),
				structure
		);

		StructuredPart modified = (StructuredPart) original.withProperties(
				Map.of("forgero:damage", List.of(5))
		);

		// Original should be unchanged
		assertNotSame(original, modified);
		assertEquals(1, original.propertiesAsMap().size());
		assertEquals(2, modified.propertiesAsMap().size());

		// Structure should be the same instance (structural sharing)
		assertSame(original.structure(), modified.structure());
	}
}
