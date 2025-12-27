package com.sigmundgranaas.forgero.cof;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ComponentTypeRegistry to ensure all type constants are
 * correctly defined and utility methods work as expected.
 */
class ComponentTypeRegistryTest {

	@Test
	void testAllConstantsAreNonNull() {
		assertNotNull(ComponentTypeRegistry.STATIC_COMPONENT, "STATIC_COMPONENT should not be null");
		assertNotNull(ComponentTypeRegistry.STATIC_EQUIPMENT, "STATIC_EQUIPMENT should not be null");
		assertNotNull(ComponentTypeRegistry.EXTENSIBLE_PART, "EXTENSIBLE_PART should not be null");
		assertNotNull(ComponentTypeRegistry.EXTENSIBLE_EQUIPMENT, "EXTENSIBLE_EQUIPMENT should not be null");
		assertNotNull(ComponentTypeRegistry.STRUCTURED_PART, "STRUCTURED_PART should not be null");
		assertNotNull(ComponentTypeRegistry.STRUCTURED_EQUIPMENT, "STRUCTURED_EQUIPMENT should not be null");
		assertNotNull(ComponentTypeRegistry.STRUCTURED_EXTENSIBLE_PART, "STRUCTURED_EXTENSIBLE_PART should not be null");
		assertNotNull(ComponentTypeRegistry.STRUCTURED_EXTENSIBLE_EQUIPMENT, "STRUCTURED_EXTENSIBLE_EQUIPMENT should not be null");
	}

	@Test
	void testAllConstantsAreUnique() {
		Set<OpenIdentifier> types = new HashSet<>();
		types.add(ComponentTypeRegistry.STATIC_COMPONENT);
		types.add(ComponentTypeRegistry.STATIC_EQUIPMENT);
		types.add(ComponentTypeRegistry.EXTENSIBLE_PART);
		types.add(ComponentTypeRegistry.EXTENSIBLE_EQUIPMENT);
		types.add(ComponentTypeRegistry.STRUCTURED_PART);
		types.add(ComponentTypeRegistry.STRUCTURED_EQUIPMENT);
		types.add(ComponentTypeRegistry.STRUCTURED_EXTENSIBLE_PART);
		types.add(ComponentTypeRegistry.STRUCTURED_EXTENSIBLE_EQUIPMENT);

		assertEquals(8, types.size(), "All 8 component type constants should be unique");
	}

	@Test
	void testConstantsHaveCorrectStringValues() {
		assertEquals("forgero:static_component", ComponentTypeRegistry.STATIC_COMPONENT.toString());
		assertEquals("forgero:static_equipment", ComponentTypeRegistry.STATIC_EQUIPMENT.toString());
		assertEquals("forgero:extensible_part", ComponentTypeRegistry.EXTENSIBLE_PART.toString());
		assertEquals("forgero:extensible_equipment", ComponentTypeRegistry.EXTENSIBLE_EQUIPMENT.toString());
		assertEquals("forgero:structured_part", ComponentTypeRegistry.STRUCTURED_PART.toString());
		assertEquals("forgero:structured_equipment", ComponentTypeRegistry.STRUCTURED_EQUIPMENT.toString());
		assertEquals("forgero:structured_extensible_part", ComponentTypeRegistry.STRUCTURED_EXTENSIBLE_PART.toString());
		assertEquals("forgero:structured_extensible_equipment", ComponentTypeRegistry.STRUCTURED_EXTENSIBLE_EQUIPMENT.toString());
	}

	@Test
	void testConstantsHaveForgerNamespace() {
		assertEquals("forgero", ComponentTypeRegistry.STATIC_COMPONENT.namespace());
		assertEquals("forgero", ComponentTypeRegistry.STATIC_EQUIPMENT.namespace());
		assertEquals("forgero", ComponentTypeRegistry.EXTENSIBLE_PART.namespace());
		assertEquals("forgero", ComponentTypeRegistry.EXTENSIBLE_EQUIPMENT.namespace());
		assertEquals("forgero", ComponentTypeRegistry.STRUCTURED_PART.namespace());
		assertEquals("forgero", ComponentTypeRegistry.STRUCTURED_EQUIPMENT.namespace());
		assertEquals("forgero", ComponentTypeRegistry.STRUCTURED_EXTENSIBLE_PART.namespace());
		assertEquals("forgero", ComponentTypeRegistry.STRUCTURED_EXTENSIBLE_EQUIPMENT.namespace());
	}

	@Test
	void testRequiresUpgrades() {
		// Types that require upgrades
		assertTrue(ComponentTypeRegistry.requiresUpgrades(ComponentTypeRegistry.EXTENSIBLE_PART),
				"EXTENSIBLE_PART should require upgrades");
		assertTrue(ComponentTypeRegistry.requiresUpgrades(ComponentTypeRegistry.EXTENSIBLE_EQUIPMENT),
				"EXTENSIBLE_EQUIPMENT should require upgrades");
		assertTrue(ComponentTypeRegistry.requiresUpgrades(ComponentTypeRegistry.STRUCTURED_EXTENSIBLE_PART),
				"STRUCTURED_EXTENSIBLE_PART should require upgrades");
		assertTrue(ComponentTypeRegistry.requiresUpgrades(ComponentTypeRegistry.STRUCTURED_EXTENSIBLE_EQUIPMENT),
				"STRUCTURED_EXTENSIBLE_EQUIPMENT should require upgrades");

		// Types that do not require upgrades
		assertFalse(ComponentTypeRegistry.requiresUpgrades(ComponentTypeRegistry.STATIC_COMPONENT),
				"STATIC_COMPONENT should not require upgrades");
		assertFalse(ComponentTypeRegistry.requiresUpgrades(ComponentTypeRegistry.STATIC_EQUIPMENT),
				"STATIC_EQUIPMENT should not require upgrades");
		assertFalse(ComponentTypeRegistry.requiresUpgrades(ComponentTypeRegistry.STRUCTURED_PART),
				"STRUCTURED_PART should not require upgrades");
		assertFalse(ComponentTypeRegistry.requiresUpgrades(ComponentTypeRegistry.STRUCTURED_EQUIPMENT),
				"STRUCTURED_EQUIPMENT should not require upgrades");
	}

	@Test
	void testRequiresStructure() {
		// Types that require structure
		assertTrue(ComponentTypeRegistry.requiresStructure(ComponentTypeRegistry.STRUCTURED_PART),
				"STRUCTURED_PART should require structure");
		assertTrue(ComponentTypeRegistry.requiresStructure(ComponentTypeRegistry.STRUCTURED_EQUIPMENT),
				"STRUCTURED_EQUIPMENT should require structure");
		assertTrue(ComponentTypeRegistry.requiresStructure(ComponentTypeRegistry.STRUCTURED_EXTENSIBLE_PART),
				"STRUCTURED_EXTENSIBLE_PART should require structure");
		assertTrue(ComponentTypeRegistry.requiresStructure(ComponentTypeRegistry.STRUCTURED_EXTENSIBLE_EQUIPMENT),
				"STRUCTURED_EXTENSIBLE_EQUIPMENT should require structure");

		// Types that do not require structure
		assertFalse(ComponentTypeRegistry.requiresStructure(ComponentTypeRegistry.STATIC_COMPONENT),
				"STATIC_COMPONENT should not require structure");
		assertFalse(ComponentTypeRegistry.requiresStructure(ComponentTypeRegistry.STATIC_EQUIPMENT),
				"STATIC_EQUIPMENT should not require structure");
		assertFalse(ComponentTypeRegistry.requiresStructure(ComponentTypeRegistry.EXTENSIBLE_PART),
				"EXTENSIBLE_PART should not require structure");
		assertFalse(ComponentTypeRegistry.requiresStructure(ComponentTypeRegistry.EXTENSIBLE_EQUIPMENT),
				"EXTENSIBLE_EQUIPMENT should not require structure");
	}

	@Test
	void testExtensibleTypesLogic() {
		// All extensible types require upgrades
		assertTrue(ComponentTypeRegistry.requiresUpgrades(ComponentTypeRegistry.EXTENSIBLE_PART));
		assertTrue(ComponentTypeRegistry.requiresUpgrades(ComponentTypeRegistry.EXTENSIBLE_EQUIPMENT));

		// Non-structured extensible types don't require structure
		assertFalse(ComponentTypeRegistry.requiresStructure(ComponentTypeRegistry.EXTENSIBLE_PART));
		assertFalse(ComponentTypeRegistry.requiresStructure(ComponentTypeRegistry.EXTENSIBLE_EQUIPMENT));
	}

	@Test
	void testStructuredExtensibleTypesRequireBoth() {
		// Structured extensible types require both
		assertTrue(ComponentTypeRegistry.requiresUpgrades(ComponentTypeRegistry.STRUCTURED_EXTENSIBLE_PART),
				"STRUCTURED_EXTENSIBLE_PART should require upgrades");
		assertTrue(ComponentTypeRegistry.requiresStructure(ComponentTypeRegistry.STRUCTURED_EXTENSIBLE_PART),
				"STRUCTURED_EXTENSIBLE_PART should require structure");

		assertTrue(ComponentTypeRegistry.requiresUpgrades(ComponentTypeRegistry.STRUCTURED_EXTENSIBLE_EQUIPMENT),
				"STRUCTURED_EXTENSIBLE_EQUIPMENT should require upgrades");
		assertTrue(ComponentTypeRegistry.requiresStructure(ComponentTypeRegistry.STRUCTURED_EXTENSIBLE_EQUIPMENT),
				"STRUCTURED_EXTENSIBLE_EQUIPMENT should require structure");
	}
}
