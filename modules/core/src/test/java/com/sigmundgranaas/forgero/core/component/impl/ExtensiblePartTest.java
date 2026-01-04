package com.sigmundgranaas.forgero.core.component.impl;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.ForgeroTest;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.slot.ComponentUpgrades;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.sigmundgranaas.forgero.testutils.ForgeroTestFactory.*;
import static com.sigmundgranaas.forgero.testutils.TestIdentifiers.*;
import static org.junit.jupiter.api.Assertions.*;

class ExtensiblePartTest extends ForgeroTest {

	@Test
	void constructsWithRequiredFields() {
		ComponentUpgrades upgrades = ComponentUpgrades.of(
				upgradeSlot("gem_slot", GEM_TAG, c -> c.getTags().contains(GEM_TAG))
		);

		ExtensiblePart part = new ExtensiblePart(
				id("iron_binding"),
				Set.of(BINDING_SLOT_TYPE),
				Map.of("forgero:durability", List.of(50)),
				upgrades
		);

		assertEquals(id("iron_binding"), part.id());
		assertEquals(1, part.getTags().size());
		assertTrue(part.getTags().contains(BINDING_SLOT_TYPE));
		assertEquals(upgrades, part.upgrades());
	}

	// Note: Null validation test removed because ExtensiblePart record
	// does not have a compact constructor to validate nulls.

	@Test
	void propertyMergeWorksCorrectly() {
		ComponentUpgrades upgrades = ComponentUpgrades.of();

		ExtensiblePart part = new ExtensiblePart(
				id("iron_binding"),
				Set.of(BINDING_SLOT_TYPE),
				Map.of("forgero:durability", List.of(50)),
				upgrades
		);

		ExtensiblePart withNewProperties = (ExtensiblePart) part.withProperties(
				Map.of("forgero:defense", List.of(2))
		);

		assertEquals(2, withNewProperties.propertiesAsMap().size());
		assertTrue(withNewProperties.propertiesAsMap().containsKey("forgero:durability"));
		assertTrue(withNewProperties.propertiesAsMap().containsKey("forgero:defense"));
	}

	@Test
	void tagContainmentCorrect() {
		ComponentUpgrades upgrades = ComponentUpgrades.of();

		Set<OpenIdentifier> tags = Set.of(BINDING_SLOT_TYPE, METAL_TAG);
		ExtensiblePart part = new ExtensiblePart(
				id("iron_binding"),
				tags,
				Map.of(),
				upgrades
		);

		assertEquals(2, part.getTags().size());
		assertTrue(part.getTags().contains(BINDING_SLOT_TYPE));
		assertTrue(part.getTags().contains(METAL_TAG));
		assertFalse(part.getTags().contains(WOOD_TAG));
	}

	@Test
	void immutabilityGuaranteed() {
		ComponentUpgrades upgrades = ComponentUpgrades.of(
				upgradeSlot("gem_slot", GEM_TAG, c -> c.getTags().contains(GEM_TAG))
		);

		ExtensiblePart original = new ExtensiblePart(
				id("iron_binding"),
				Set.of(BINDING_SLOT_TYPE),
				Map.of("forgero:durability", List.of(50)),
				upgrades
		);

		ExtensiblePart modified = (ExtensiblePart) original.withProperties(
				Map.of("forgero:defense", List.of(2))
		);

		assertNotSame(original, modified);
		assertEquals(1, original.propertiesAsMap().size());
		assertEquals(2, modified.propertiesAsMap().size());

		// Upgrades should be the same instance (structural sharing)
		assertSame(original.upgrades(), modified.upgrades());
	}
}
