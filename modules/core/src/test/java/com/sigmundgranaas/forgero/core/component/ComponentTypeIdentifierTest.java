package com.sigmundgranaas.forgero.core.component;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.ComponentFactory;
import com.sigmundgranaas.forgero.core.component.api.slot.ComponentUpgrades;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentStructure;
import com.sigmundgranaas.forgero.core.component.impl.*;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Component.getTypeIdentifier() polymorphic method.
 * Verifies that all component implementations correctly return their type identifiers.
 */
class ComponentTypeIdentifierTest {

	private static final OpenIdentifier TEST_ID = OpenIdentifier.of("test_component");
	private static final Set<OpenIdentifier> TEST_TAGS = Set.of(OpenIdentifier.of("test_tag"));
	private static final Map<String, List<?>> TEST_PROPERTIES = Map.of("test", List.of("value"));

	// ===== Type Identifier Tests for Each Component Type =====

	@Test
	void testStaticComponentTypeIdentifier() {
		Component component = createStaticComponent();
		assertTypeIdentifier(component, "static_component");
	}

	@Test
	void testStaticEquipmentTypeIdentifier() {
		Component component = createStaticEquipment();
		assertTypeIdentifier(component, "static_equipment");
	}

	@Test
	void testExtensiblePartTypeIdentifier() {
		Component component = createExtensiblePart();
		assertTypeIdentifier(component, "extensible_part");
	}

	@Test
	void testExtensibleEquipmentTypeIdentifier() {
		Component component = createExtensibleEquipment();
		assertTypeIdentifier(component, "extensible_equipment");
	}

	@Test
	void testStructuredPartTypeIdentifier() {
		Component component = createStructuredPart();
		assertTypeIdentifier(component, "structured_part");
	}

	@Test
	void testStructuredEquipmentTypeIdentifier() {
		Component component = createStructuredEquipment();
		assertTypeIdentifier(component, "structured_equipment");
	}

	@Test
	void testStructuredExtensiblePartTypeIdentifier() {
		Component component = createStructuredExtensiblePart();
		assertTypeIdentifier(component, "structured_extensible_part");
	}

	@Test
	void testStructuredExtensibleEquipmentTypeIdentifier() {
		Component component = createStructuredExtensibleEquipment();
		assertTypeIdentifier(component, "structured_extensible_equipment");
	}

	// ===== Type Identifier Constant Tests =====

	@Test
	void testStaticComponentTypeIsConstant() {
		assertTypeIsConstant(createStaticComponent());
	}

	@Test
	void testStaticEquipmentTypeIsConstant() {
		assertTypeIsConstant(createStaticEquipment());
	}

	@Test
	void testExtensiblePartTypeIsConstant() {
		assertTypeIsConstant(createExtensiblePart());
	}

	@Test
	void testExtensibleEquipmentTypeIsConstant() {
		assertTypeIsConstant(createExtensibleEquipment());
	}

	@Test
	void testStructuredPartTypeIsConstant() {
		assertTypeIsConstant(createStructuredPart());
	}

	@Test
	void testStructuredEquipmentTypeIsConstant() {
		assertTypeIsConstant(createStructuredEquipment());
	}

	@Test
	void testStructuredExtensiblePartTypeIsConstant() {
		assertTypeIsConstant(createStructuredExtensiblePart());
	}

	@Test
	void testStructuredExtensibleEquipmentTypeIsConstant() {
		assertTypeIsConstant(createStructuredExtensibleEquipment());
	}

	// ===== Backward Compatibility Tests =====

	@Test
	@SuppressWarnings("deprecation")
	void testStaticComponentConsistencyWithFactory() {
		assertConsistencyWithFactory(createStaticComponent());
	}

	@Test
	@SuppressWarnings("deprecation")
	void testStaticEquipmentConsistencyWithFactory() {
		assertConsistencyWithFactory(createStaticEquipment());
	}

	@Test
	@SuppressWarnings("deprecation")
	void testExtensiblePartConsistencyWithFactory() {
		assertConsistencyWithFactory(createExtensiblePart());
	}

	@Test
	@SuppressWarnings("deprecation")
	void testExtensibleEquipmentConsistencyWithFactory() {
		assertConsistencyWithFactory(createExtensibleEquipment());
	}

	@Test
	@SuppressWarnings("deprecation")
	void testStructuredPartConsistencyWithFactory() {
		assertConsistencyWithFactory(createStructuredPart());
	}

	@Test
	@SuppressWarnings("deprecation")
	void testStructuredEquipmentConsistencyWithFactory() {
		assertConsistencyWithFactory(createStructuredEquipment());
	}

	@Test
	@SuppressWarnings("deprecation")
	void testStructuredExtensiblePartConsistencyWithFactory() {
		assertConsistencyWithFactory(createStructuredExtensiblePart());
	}

	@Test
	@SuppressWarnings("deprecation")
	void testStructuredExtensibleEquipmentConsistencyWithFactory() {
		assertConsistencyWithFactory(createStructuredExtensibleEquipment());
	}

	// ===== General Tests =====

	/**
	 * Verifies that different component instances of the same type return equal identifiers.
	 */
	@Test
	void testSameTypeReturnsSameIdentifier() {
		var comp1 = new StaticComponent(OpenIdentifier.of("test1"), TEST_TAGS, TEST_PROPERTIES);
		var comp2 = new StaticComponent(OpenIdentifier.of("test2"), TEST_TAGS, TEST_PROPERTIES);

		assertEquals(comp1.getTypeIdentifier(), comp2.getTypeIdentifier(),
				"Different instances of same type should have equal type identifiers");
	}

	/**
	 * Verifies that different component types return different identifiers.
	 */
	@Test
	void testDifferentTypesReturnDifferentIdentifiers() {
		var staticComp = new StaticComponent(TEST_ID, TEST_TAGS, TEST_PROPERTIES);
		var staticEquip = new StaticEquipment(TEST_ID, TEST_TAGS, TEST_PROPERTIES);

		assertNotEquals(staticComp.getTypeIdentifier(), staticEquip.getTypeIdentifier(),
				"Different component types should have different type identifiers");
	}

	/**
	 * Verifies that all 8 component types have unique identifiers.
	 */
	@Test
	void testAllTypeIdentifiersAreUnique() {
		Set<OpenIdentifier> typeIds = new HashSet<>();
		typeIds.add(createStaticComponent().getTypeIdentifier());
		typeIds.add(createStaticEquipment().getTypeIdentifier());
		typeIds.add(createExtensiblePart().getTypeIdentifier());
		typeIds.add(createExtensibleEquipment().getTypeIdentifier());
		typeIds.add(createStructuredPart().getTypeIdentifier());
		typeIds.add(createStructuredEquipment().getTypeIdentifier());
		typeIds.add(createStructuredExtensiblePart().getTypeIdentifier());
		typeIds.add(createStructuredExtensibleEquipment().getTypeIdentifier());

		assertEquals(8, typeIds.size(), "All 8 component types should have unique type identifiers");
	}

	// ===== Helper Assertion Methods =====

	private void assertTypeIdentifier(Component component, String expectedType) {
		OpenIdentifier typeId = component.getTypeIdentifier();
		assertNotNull(typeId, "Type identifier should not be null");
		assertEquals("forgero", typeId.namespace(), "Type identifier should use forgero namespace");
		assertEquals(expectedType, typeId.path(), "Type identifier path should match expected");
		assertEquals(OpenIdentifier.of(expectedType), typeId, "Type identifier should equal OpenIdentifier.of(type)");
	}

	private void assertTypeIsConstant(Component component) {
		OpenIdentifier first = component.getTypeIdentifier();
		OpenIdentifier second = component.getTypeIdentifier();
		assertSame(first, second, "Type identifier should return the same instance (constant)");
	}

	@SuppressWarnings("deprecation")
	private void assertConsistencyWithFactory(Component component) {
		OpenIdentifier fromInstance = component.getTypeIdentifier();
		OpenIdentifier fromFactory = ComponentFactory.getTypeIdentifier(component);

		assertEquals(fromFactory, fromInstance,
				"Polymorphic method should return same identifier as deprecated factory method");
	}

	// ===== Helper Factory Methods =====

	private static Component createStaticComponent() {
		return new StaticComponent(TEST_ID, TEST_TAGS, TEST_PROPERTIES);
	}

	private static Component createStaticEquipment() {
		return new StaticEquipment(TEST_ID, TEST_TAGS, TEST_PROPERTIES);
	}

	private static Component createExtensiblePart() {
		ComponentUpgrades upgrades = ComponentUpgrades.empty();
		return new ExtensiblePart(TEST_ID, TEST_TAGS, TEST_PROPERTIES, upgrades);
	}

	private static Component createExtensibleEquipment() {
		ComponentUpgrades upgrades = ComponentUpgrades.empty();
		return new ExtensibleEquipment(TEST_ID, TEST_TAGS, TEST_PROPERTIES, upgrades);
	}

	private static Component createStructuredPart() {
		ComponentStructure structure = ComponentStructure.empty();
		return new StructuredPart(TEST_ID, TEST_TAGS, TEST_PROPERTIES, structure);
	}

	private static Component createStructuredEquipment() {
		ComponentStructure structure = ComponentStructure.empty();
		return new StructuredEquipment(TEST_ID, TEST_TAGS, TEST_PROPERTIES, structure);
	}

	private static Component createStructuredExtensiblePart() {
		ComponentStructure structure = ComponentStructure.empty();
		ComponentUpgrades upgrades = ComponentUpgrades.empty();
		return new StructuredExtensiblePart(TEST_ID, TEST_TAGS, TEST_PROPERTIES, structure, upgrades);
	}

	private static Component createStructuredExtensibleEquipment() {
		ComponentStructure structure = ComponentStructure.empty();
		ComponentUpgrades upgrades = ComponentUpgrades.empty();
		return new StructuredExtensibleEquipment(TEST_ID, TEST_TAGS, TEST_PROPERTIES, structure, upgrades);
	}
}
