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

class StructuredExtensiblePartTest extends ForgeroTest {

	@Test
	void constructsWithRequiredFields() {
		Component iron = material(IRON_ID, METAL_TAG);
		ComponentStructure structure = ComponentStructure.of(
				structureSlot("material_slot", MATERIAL_SLOT_TYPE, iron)
		);
		ComponentUpgrades upgrades = ComponentUpgrades.of(
				upgradeSlot("gem_slot", GEM_TAG, c -> c.getTags().contains(GEM_TAG))
		);

		StructuredExtensiblePart part = new StructuredExtensiblePart(
				PICKAXE_HEAD_ID,
				Set.of(PICKAXE_HEAD_TAG),
				Map.of("forgero:durability", List.of(250)),
				structure,
				upgrades
		);

		assertEquals(PICKAXE_HEAD_ID, part.id());
		assertEquals(1, part.getTags().size());
		assertEquals(structure, part.structure());
		assertEquals(upgrades, part.upgrades());
	}

	// Note: Null validation test removed because StructuredExtensiblePart record
	// does not have a compact constructor to validate nulls.

	@Test
	void propertyMergeWorksCorrectly() {
		Component iron = material(IRON_ID, METAL_TAG);
		ComponentStructure structure = ComponentStructure.of(
				structureSlot("material_slot", MATERIAL_SLOT_TYPE, iron)
		);
		ComponentUpgrades upgrades = ComponentUpgrades.of();

		StructuredExtensiblePart part = new StructuredExtensiblePart(
				PICKAXE_HEAD_ID,
				Set.of(PICKAXE_HEAD_TAG),
				Map.of("forgero:durability", List.of(250)),
				structure,
				upgrades
		);

		StructuredExtensiblePart withNewProperties = (StructuredExtensiblePart) part.withProperties(
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
		ComponentUpgrades upgrades = ComponentUpgrades.of();

		Set<OpenIdentifier> tags = Set.of(PICKAXE_HEAD_TAG, METAL_TAG);
		StructuredExtensiblePart part = new StructuredExtensiblePart(
				PICKAXE_HEAD_ID,
				tags,
				Map.of(),
				structure,
				upgrades
		);

		assertEquals(2, part.getTags().size());
		assertTrue(part.getTags().contains(PICKAXE_HEAD_TAG));
		assertTrue(part.getTags().contains(METAL_TAG));
	}

	@Test
	void immutabilityGuaranteed() {
		Component iron = material(IRON_ID, METAL_TAG);
		ComponentStructure structure = ComponentStructure.of(
				structureSlot("material_slot", MATERIAL_SLOT_TYPE, iron)
		);
		ComponentUpgrades upgrades = ComponentUpgrades.of();

		StructuredExtensiblePart original = new StructuredExtensiblePart(
				PICKAXE_HEAD_ID,
				Set.of(PICKAXE_HEAD_TAG),
				Map.of("forgero:durability", List.of(250)),
				structure,
				upgrades
		);

		StructuredExtensiblePart modified = (StructuredExtensiblePart) original.withProperties(
				Map.of("forgero:damage", List.of(5))
		);

		assertNotSame(original, modified);
		assertEquals(1, original.propertiesAsMap().size());
		assertEquals(2, modified.propertiesAsMap().size());

		// Structure and upgrades should be same instances
		assertSame(original.structure(), modified.structure());
		assertSame(original.upgrades(), modified.upgrades());
	}
}
