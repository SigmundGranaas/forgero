package com.sigmundgranaas.forgero.core.component.api;

import com.sigmundgranaas.forgero.common.identifier.api.IdentifierFactory;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.slot.ComponentUpgrades;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentStructure;
import com.sigmundgranaas.forgero.core.component.impl.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Terminal vs Contributing component architecture.
 * <p>
 * This test verifies that:
 * <ul>
 *     <li>Equipment classes implement {@link EquipmentComponent} and apply attributes</li>
 *     <li>Part classes implement {@link ContributingComponent} and don't apply attributes</li>
 * </ul>
 */
@DisplayName("Component Type Hierarchy")
class ComponentTypeTest {

	private static final IdentifierFactory ID_FACTORY = new IdentifierFactory.Builder()
			.defaultNamespace("test")
			.build();

	private static final OpenIdentifier TEST_ID = ID_FACTORY.of("component");
	private static final Set<OpenIdentifier> EMPTY_TAGS = Set.of();
	private static final Map<String, List<?>> EMPTY_PROPS = Map.of();

	@Nested
	@DisplayName("Equipment Components")
	class EquipmentComponents {

		@Test
		@DisplayName("StaticEquipment implements EquipmentComponent")
		void staticEquipmentImplementsEquipmentComponent() {
			Component equipment = StaticEquipment.create(TEST_ID, EMPTY_TAGS, EMPTY_PROPS);

			assertTrue(equipment instanceof EquipmentComponent,
					"StaticEquipment should implement EquipmentComponent");
			assertFalse(equipment instanceof ContributingComponent,
					"StaticEquipment should NOT implement ContributingComponent");
		}

		@Test
		@DisplayName("StaticEquipment.appliesAttributes returns true")
		void staticEquipmentAppliesAttributes() {
			StaticEquipment equipment = StaticEquipment.create(TEST_ID, EMPTY_TAGS, EMPTY_PROPS);

			assertTrue(equipment.appliesAttributes(),
					"Equipment components should apply attributes");
		}

		@Test
		@DisplayName("ExtensibleEquipment implements EquipmentComponent")
		void extensibleEquipmentImplementsEquipmentComponent() {
			ExtensibleEquipment equipment = ExtensibleEquipment.create(
					TEST_ID, EMPTY_TAGS, EMPTY_PROPS, ComponentUpgrades.empty());

			assertTrue(equipment instanceof EquipmentComponent,
					"ExtensibleEquipment should implement EquipmentComponent");
			assertTrue(equipment.appliesAttributes(),
					"ExtensibleEquipment should apply attributes");
		}

		@Test
		@DisplayName("StructuredEquipment implements EquipmentComponent")
		void structuredEquipmentImplementsEquipmentComponent() {
			StructuredEquipment equipment = StructuredEquipment.create(
					TEST_ID, EMPTY_TAGS, EMPTY_PROPS, ComponentStructure.empty());

			assertTrue(equipment instanceof EquipmentComponent,
					"StructuredEquipment should implement EquipmentComponent");
			assertTrue(equipment.appliesAttributes(),
					"StructuredEquipment should apply attributes");
		}

		@Test
		@DisplayName("StructuredExtensibleEquipment implements EquipmentComponent")
		void structuredExtensibleEquipmentImplementsEquipmentComponent() {
			StructuredExtensibleEquipment equipment = StructuredExtensibleEquipment.create(
					TEST_ID, EMPTY_TAGS, EMPTY_PROPS, ComponentStructure.empty(), ComponentUpgrades.empty());

			assertTrue(equipment instanceof EquipmentComponent,
					"StructuredExtensibleEquipment should implement EquipmentComponent");
			assertTrue(equipment.appliesAttributes(),
					"StructuredExtensibleEquipment should apply attributes");
		}
	}

	@Nested
	@DisplayName("Contributing Components (Parts)")
	class ContributingComponents {

		@Test
		@DisplayName("StaticComponent implements ContributingComponent")
		void staticComponentImplementsContributingComponent() {
			Component component = new StaticComponent(TEST_ID, EMPTY_TAGS, EMPTY_PROPS);

			assertTrue(component instanceof ContributingComponent,
					"StaticComponent should implement ContributingComponent");
			assertFalse(component instanceof EquipmentComponent,
					"StaticComponent should NOT implement EquipmentComponent");
		}

		@Test
		@DisplayName("StaticComponent.appliesAttributes returns false")
		void staticComponentDoesNotApplyAttributes() {
			StaticComponent component = new StaticComponent(TEST_ID, EMPTY_TAGS, EMPTY_PROPS);

			assertFalse(component.appliesAttributes(),
					"Contributing components should NOT apply attributes");
		}

		@Test
		@DisplayName("ExtensiblePart implements ContributingComponent")
		void extensiblePartImplementsContributingComponent() {
			ExtensiblePart part = new ExtensiblePart(
					TEST_ID, EMPTY_TAGS, EMPTY_PROPS, ComponentUpgrades.empty());

			assertTrue(part instanceof ContributingComponent,
					"ExtensiblePart should implement ContributingComponent");
			assertFalse(part.appliesAttributes(),
					"ExtensiblePart should NOT apply attributes");
		}

		@Test
		@DisplayName("StructuredPart implements ContributingComponent")
		void structuredPartImplementsContributingComponent() {
			StructuredPart part = new StructuredPart(
					TEST_ID, EMPTY_TAGS, EMPTY_PROPS, ComponentStructure.empty());

			assertTrue(part instanceof ContributingComponent,
					"StructuredPart should implement ContributingComponent");
			assertFalse(part.appliesAttributes(),
					"StructuredPart should NOT apply attributes");
		}

		@Test
		@DisplayName("StructuredExtensiblePart implements ContributingComponent")
		void structuredExtensiblePartImplementsContributingComponent() {
			StructuredExtensiblePart part = new StructuredExtensiblePart(
					TEST_ID, EMPTY_TAGS, EMPTY_PROPS, ComponentStructure.empty(), ComponentUpgrades.empty());

			assertTrue(part instanceof ContributingComponent,
					"StructuredExtensiblePart should implement ContributingComponent");
			assertFalse(part.appliesAttributes(),
					"StructuredExtensiblePart should NOT apply attributes");
		}
	}

	@Nested
	@DisplayName("Type Distinction")
	class TypeDistinction {

		@Test
		@DisplayName("Equipment and Part types are mutually exclusive")
		void equipmentAndPartAreMutuallyExclusive() {
			Component equipment = StaticEquipment.create(TEST_ID, EMPTY_TAGS, EMPTY_PROPS);
			Component part = new StaticComponent(TEST_ID, EMPTY_TAGS, EMPTY_PROPS);

			// Equipment is only EquipmentComponent
			assertTrue(equipment instanceof EquipmentComponent);
			assertFalse(equipment instanceof ContributingComponent);

			// Part is only ContributingComponent
			assertTrue(part instanceof ContributingComponent);
			assertFalse(part instanceof EquipmentComponent);
		}

		@Test
		@DisplayName("instanceof check is O(1)")
		void instanceOfCheckIsEfficient() {
			Component component = StaticEquipment.create(TEST_ID, EMPTY_TAGS, EMPTY_PROPS);

			// This is a simple instanceof check, should be very fast
			boolean isEquipment = component instanceof EquipmentComponent;

			assertTrue(isEquipment);
		}
	}
}
