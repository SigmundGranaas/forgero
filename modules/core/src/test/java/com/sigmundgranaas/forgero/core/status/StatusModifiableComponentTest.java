package com.sigmundgranaas.forgero.core.status;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.status.api.StatusModifiableComponent;
import com.sigmundgranaas.forgero.core.status.api.StatusModifier;
import com.sigmundgranaas.forgero.core.status.api.StatusModifierSlot;
import com.sigmundgranaas.forgero.core.status.api.StatusModifierSlotContainer;
import com.sigmundgranaas.forgero.core.status.impl.SimpleStatusModifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StatusModifiableComponent Tests")
class StatusModifiableComponentTest {

	private static OpenIdentifier id(String path) {
		return OpenIdentifier.of(path);
	}

	private static StatusModifier createModifier(String name) {
		return SimpleStatusModifier.builder("forgero:" + name)
				.displayName(name.substring(0, 1).toUpperCase() + name.substring(1))
				.build();
	}

	/**
	 * Test implementation of StatusModifiableComponent for testing purposes.
	 */
	record TestModifiableComponent(
			OpenIdentifier id,
			Set<OpenIdentifier> tags,
			StatusModifierSlotContainer statusModifiers
	) implements StatusModifiableComponent {

		static TestModifiableComponent create(String name, int slotCapacity) {
			return new TestModifiableComponent(
					OpenIdentifier.of(name),
					Set.of(OpenIdentifier.of("test")),
					StatusModifierSlotContainer.withCapacity(slotCapacity)
			);
		}

		@Override
		public Set<OpenIdentifier> getTags() {
			return tags;
		}

		@Override
		public OpenIdentifier getTypeIdentifier() {
			return OpenIdentifier.of("test_modifiable_component");
		}

		@Override
		public Map<String, List<?>> propertiesAsMap() {
			return Collections.emptyMap();
		}

		@Override
		public Component withProperties(Map<String, List<?>> newProperties) {
			return this;
		}

		@Override
		public Component withStatusModifiers(StatusModifierSlotContainer modifiers) {
			return new TestModifiableComponent(id, tags, modifiers);
		}
	}

	@Nested
	@DisplayName("Interface Methods")
	class InterfaceMethods {

		@Test
		@DisplayName("should return status modifiers container")
		void shouldReturnStatusModifiersContainer() {
			TestModifiableComponent component = TestModifiableComponent.create("test", 3);

			StatusModifierSlotContainer container = component.statusModifiers();

			assertNotNull(container);
			assertEquals(3, container.size());
		}

		@Test
		@DisplayName("should get status modifier slots")
		void shouldGetStatusModifierSlots() {
			TestModifiableComponent component = TestModifiableComponent.create("test", 2);

			List<StatusModifierSlot> slots = component.getStatusModifierSlots();

			assertEquals(2, slots.size());
		}

		@Test
		@DisplayName("should check hasStatusModifiers correctly")
		void shouldCheckHasStatusModifiersCorrectly() {
			TestModifiableComponent empty = TestModifiableComponent.create("empty", 2);
			assertFalse(empty.hasStatusModifiers());

			// Add a modifier
			StatusModifier sharp = createModifier("sharp");
			StatusModifierSlotContainer withModifier = empty.statusModifiers().tryInstall(sharp).orElseThrow();
			TestModifiableComponent filled = (TestModifiableComponent) empty.withStatusModifiers(withModifier);

			assertTrue(filled.hasStatusModifiers());
		}

		@Test
		@DisplayName("should return installed modifiers")
		void shouldReturnInstalledModifiers() {
			TestModifiableComponent component = TestModifiableComponent.create("test", 3);
			StatusModifier sharp = createModifier("sharp");
			StatusModifier durable = createModifier("durable");

			StatusModifierSlotContainer container = component.statusModifiers()
					.tryInstall(sharp).orElseThrow()
					.tryInstall(durable).orElseThrow();
			TestModifiableComponent updated = (TestModifiableComponent) component.withStatusModifiers(container);

			List<StatusModifier> installed = updated.getInstalledModifiers();

			assertEquals(2, installed.size());
			assertTrue(installed.contains(sharp));
			assertTrue(installed.contains(durable));
		}
	}

	@Nested
	@DisplayName("Component Transformation")
	class ComponentTransformation {

		@Test
		@DisplayName("should create new component with status modifiers")
		void shouldCreateNewComponentWithStatusModifiers() {
			TestModifiableComponent original = TestModifiableComponent.create("original", 2);
			StatusModifier sharp = createModifier("sharp");

			StatusModifierSlotContainer newContainer = original.statusModifiers()
					.tryInstall(sharp).orElseThrow();
			Component updated = original.withStatusModifiers(newContainer);

			// Original unchanged
			assertTrue(original.statusModifiers().isEmpty());

			// Updated has the modifier
			assertInstanceOf(StatusModifiableComponent.class, updated);
			StatusModifiableComponent modifiable = (StatusModifiableComponent) updated;
			assertTrue(modifiable.statusModifiers().hasModifier(id("sharp")));
		}

		@Test
		@DisplayName("should preserve component identity through modifications")
		void shouldPreserveComponentIdentityThroughModifications() {
			TestModifiableComponent original = TestModifiableComponent.create("iron_sword", 3);
			StatusModifier sharp = createModifier("sharp");

			StatusModifierSlotContainer newContainer = original.statusModifiers()
					.tryInstall(sharp).orElseThrow();
			TestModifiableComponent updated = (TestModifiableComponent) original.withStatusModifiers(newContainer);

			assertEquals(original.id(), updated.id());
			assertEquals(original.tags(), updated.tags());
		}
	}

	@Nested
	@DisplayName("Immutability")
	class Immutability {

		@Test
		@DisplayName("original component should be unchanged after modification")
		void originalComponentShouldBeUnchangedAfterModification() {
			TestModifiableComponent component = TestModifiableComponent.create("test", 2);
			StatusModifier modifier = createModifier("enchanted");

			int originalFilledCount = component.statusModifiers().filledCount();
			StatusModifierSlotContainer newContainer = component.statusModifiers()
					.tryInstall(modifier).orElseThrow();
			component.withStatusModifiers(newContainer);

			// Original should still be unchanged
			assertEquals(originalFilledCount, component.statusModifiers().filledCount());
			assertFalse(component.statusModifiers().hasModifier(id("enchanted")));
		}
	}

	@Nested
	@DisplayName("Integration with StatusModifier Operations")
	class IntegrationOperations {

		@Test
		@DisplayName("should chain multiple modifier installations")
		void shouldChainMultipleModifierInstallations() {
			TestModifiableComponent component = TestModifiableComponent.create("test", 4);
			StatusModifier m1 = createModifier("sharp");
			StatusModifier m2 = createModifier("durable");
			StatusModifier m3 = createModifier("enchanted");

			StatusModifierSlotContainer container = component.statusModifiers();
			container = container.tryInstall(m1).orElseThrow();
			container = container.tryInstall(m2).orElseThrow();
			container = container.tryInstall(m3).orElseThrow();

			TestModifiableComponent result = (TestModifiableComponent) component.withStatusModifiers(container);

			assertEquals(3, result.statusModifiers().filledCount());
			assertEquals(1, result.statusModifiers().emptyCount());
		}

		@Test
		@DisplayName("should fail installation when container is full")
		void shouldFailInstallationWhenContainerIsFull() {
			TestModifiableComponent component = TestModifiableComponent.create("test", 1);
			StatusModifier m1 = createModifier("sharp");
			StatusModifier m2 = createModifier("durable");

			StatusModifierSlotContainer container = component.statusModifiers()
					.tryInstall(m1).orElseThrow();
			TestModifiableComponent filled = (TestModifiableComponent) component.withStatusModifiers(container);

			Optional<StatusModifierSlotContainer> result = filled.statusModifiers().tryInstall(m2);

			assertTrue(result.isEmpty());
		}

		@Test
		@DisplayName("should remove and reinstall modifiers")
		void shouldRemoveAndReinstallModifiers() {
			TestModifiableComponent component = TestModifiableComponent.create("test", 2);
			StatusModifier sharp = createModifier("sharp");
			StatusModifier durable = createModifier("durable");

			// Install sharp
			StatusModifierSlotContainer container = component.statusModifiers()
					.tryInstall(sharp).orElseThrow();
			TestModifiableComponent step1 = (TestModifiableComponent) component.withStatusModifiers(container);
			assertTrue(step1.statusModifiers().hasModifier(id("sharp")));

			// Remove sharp
			container = step1.statusModifiers().removeModifier(id("sharp"));
			TestModifiableComponent step2 = (TestModifiableComponent) step1.withStatusModifiers(container);
			assertFalse(step2.statusModifiers().hasModifier(id("sharp")));

			// Install durable in the now-empty slot
			container = step2.statusModifiers().tryInstall(durable).orElseThrow();
			TestModifiableComponent step3 = (TestModifiableComponent) step2.withStatusModifiers(container);
			assertTrue(step3.statusModifiers().hasModifier(id("durable")));
		}
	}
}
