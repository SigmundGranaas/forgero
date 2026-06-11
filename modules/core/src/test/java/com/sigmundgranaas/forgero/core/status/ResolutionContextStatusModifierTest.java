package com.sigmundgranaas.forgero.core.status;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.property.compilation.ResolutionContext;
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

@DisplayName("ResolutionContext Status Modifier Tests")
class ResolutionContextStatusModifierTest {

	private static OpenIdentifier id(String path) {
		return OpenIdentifier.of(path);
	}

	private static StatusModifier createModifier(String name) {
		return SimpleStatusModifier.builder("forgero:" + name)
				.displayName(name.substring(0, 1).toUpperCase() + name.substring(1))
				.build();
	}

	/**
	 * Simple component that does NOT support status modifiers.
	 */
	record SimpleComponent(OpenIdentifier id) implements Component {
		@Override
		public Set<OpenIdentifier> getTags() {
			return Set.of();
		}

		@Override
		public OpenIdentifier getTypeIdentifier() {
			return OpenIdentifier.of("simple_component");
		}

		@Override
		public Map<String, List<?>> propertiesAsMap() {
			return Collections.emptyMap();
		}

		@Override
		public Component withProperties(Map<String, List<?>> newProperties) {
			return this;
		}
	}

	/**
	 * Component that supports status modifiers.
	 */
	record ModifiableComponent(
			OpenIdentifier id,
			StatusModifierSlotContainer statusModifiers
	) implements StatusModifiableComponent {

		static ModifiableComponent create(String name, int slotCapacity) {
			return new ModifiableComponent(
					OpenIdentifier.of(name),
					StatusModifierSlotContainer.withCapacity(slotCapacity)
			);
		}

		static ModifiableComponent createWithModifiers(String name, StatusModifier... modifiers) {
			StatusModifierSlotContainer container = StatusModifierSlotContainer.withCapacity(modifiers.length);
			for (StatusModifier modifier : modifiers) {
				container = container.tryInstall(modifier).orElseThrow();
			}
			return new ModifiableComponent(OpenIdentifier.of(name), container);
		}

		@Override
		public Set<OpenIdentifier> getTags() {
			return Set.of(OpenIdentifier.of("modifiable"));
		}

		@Override
		public OpenIdentifier getTypeIdentifier() {
			return OpenIdentifier.of("modifiable_component");
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
			return new ModifiableComponent(id, modifiers);
		}
	}

	@Nested
	@DisplayName("Basic Status Modifier Detection")
	class BasicDetection {

		@Test
		@DisplayName("should return empty list for non-modifiable component")
		void shouldReturnEmptyListForNonModifiableComponent() {
			SimpleComponent component = new SimpleComponent(id("simple"));
			ResolutionContext context = new ResolutionContext(component, component);

			List<StatusModifierSlot> slots = context.getStatusModifierSlots();

			assertTrue(slots.isEmpty());
		}

		@Test
		@DisplayName("should return slots for modifiable component")
		void shouldReturnSlotsForModifiableComponent() {
			ModifiableComponent component = ModifiableComponent.create("modifiable", 3);
			ResolutionContext context = new ResolutionContext(component, component);

			List<StatusModifierSlot> slots = context.getStatusModifierSlots();

			assertEquals(3, slots.size());
		}

		@Test
		@DisplayName("should detect status modifier presence")
		void shouldDetectStatusModifierPresence() {
			StatusModifier sharp = createModifier("sharp");
			ModifiableComponent component = ModifiableComponent.createWithModifiers("weapon", sharp);
			ResolutionContext context = new ResolutionContext(component, component);

			assertTrue(context.hasStatusModifier(id("sharp")));
			assertFalse(context.hasStatusModifier(id("durable")));
		}
	}

	@Nested
	@DisplayName("Multiple Modifiers")
	class MultipleModifiers {

		@Test
		@DisplayName("should detect multiple status modifiers")
		void shouldDetectMultipleStatusModifiers() {
			StatusModifier sharp = createModifier("sharp");
			StatusModifier durable = createModifier("durable");
			StatusModifier enchanted = createModifier("enchanted");

			ModifiableComponent component = ModifiableComponent.createWithModifiers(
					"weapon", sharp, durable, enchanted
			);
			ResolutionContext context = new ResolutionContext(component, component);

			assertTrue(context.hasStatusModifier(id("sharp")));
			assertTrue(context.hasStatusModifier(id("durable")));
			assertTrue(context.hasStatusModifier(id("enchanted")));
			assertFalse(context.hasStatusModifier(id("broken")));
		}

		@Test
		@DisplayName("should return all status modifier slots")
		void shouldReturnAllStatusModifierSlots() {
			StatusModifier sharp = createModifier("sharp");
			StatusModifier durable = createModifier("durable");

			ModifiableComponent component = ModifiableComponent.createWithModifiers(
					"weapon", sharp, durable
			);
			ResolutionContext context = new ResolutionContext(component, component);

			List<StatusModifierSlot> allSlots = context.getAllStatusModifierSlots();

			assertEquals(2, allSlots.size());
			assertTrue(allSlots.stream().allMatch(StatusModifierSlot::isFilled));
		}
	}

	@Nested
	@DisplayName("Empty vs Filled Slots")
	class EmptyVsFilled {

		@Test
		@DisplayName("should include empty slots in slot list")
		void shouldIncludeEmptySlotsInSlotList() {
			ModifiableComponent component = ModifiableComponent.create("weapon", 3);
			// Install only one modifier, leaving 2 slots empty
			StatusModifier sharp = createModifier("sharp");
			StatusModifierSlotContainer container = component.statusModifiers()
					.tryInstall(sharp).orElseThrow();
			ModifiableComponent updated = new ModifiableComponent(component.id(), container);

			ResolutionContext context = new ResolutionContext(updated, updated);

			List<StatusModifierSlot> slots = context.getStatusModifierSlots();
			assertEquals(3, slots.size());

			long filledCount = slots.stream().filter(StatusModifierSlot::isFilled).count();
			long emptyCount = slots.stream().filter(StatusModifierSlot::isEmpty).count();

			assertEquals(1, filledCount);
			assertEquals(2, emptyCount);
		}

		@Test
		@DisplayName("should not detect modifier in empty slots")
		void shouldNotDetectModifierInEmptySlots() {
			ModifiableComponent component = ModifiableComponent.create("weapon", 3);
			ResolutionContext context = new ResolutionContext(component, component);

			assertFalse(context.hasStatusModifier(id("any_modifier")));
		}
	}

	@Nested
	@DisplayName("Context with Self as Root")
	class SelfAsRoot {

		@Test
		@DisplayName("should work when self is the root")
		void shouldWorkWhenSelfIsRoot() {
			StatusModifier broken = createModifier("broken");
			ModifiableComponent root = ModifiableComponent.createWithModifiers("sword", broken);

			ResolutionContext context = new ResolutionContext(root, root);

			assertTrue(context.isRoot());
			assertTrue(context.hasStatusModifier(id("broken")));
		}
	}

	@Nested
	@DisplayName("Context Immutability")
	class ContextImmutability {

		@Test
		@DisplayName("context should not change when component is modified")
		void contextShouldNotChangeWhenComponentIsModified() {
			StatusModifier sharp = createModifier("sharp");
			// Create with capacity for 2 modifiers but only install 1
			StatusModifierSlotContainer container = StatusModifierSlotContainer.withCapacity(2)
					.tryInstall(sharp).orElseThrow();
			ModifiableComponent original = new ModifiableComponent(OpenIdentifier.of("weapon"), container);

			// Create context with original state
			ResolutionContext context = new ResolutionContext(original, original);
			assertTrue(context.hasStatusModifier(id("sharp")));

			// The component is immutable, so creating a new one doesn't affect context
			StatusModifier durable = createModifier("durable");
			StatusModifierSlotContainer newContainer = original.statusModifiers()
					.tryInstall(durable).orElseThrow();
			ModifiableComponent modified = new ModifiableComponent(original.id(), newContainer);

			// Original context should still only know about the original state
			assertTrue(context.hasStatusModifier(id("sharp")));
			// It doesn't have durable because context was created before the change
			assertFalse(context.hasStatusModifier(id("durable")));

			// New context for modified component should see durable
			ResolutionContext newContext = new ResolutionContext(modified, modified);
			assertTrue(newContext.hasStatusModifier(id("durable")));
		}
	}

	@Nested
	@DisplayName("Special Modifier IDs")
	class SpecialModifierIds {

		@Test
		@DisplayName("should detect broken status")
		void shouldDetectBrokenStatus() {
			StatusModifier broken = SimpleStatusModifier.builder("forgero:broken")
					.displayName("Broken")
					.build();
			ModifiableComponent component = ModifiableComponent.createWithModifiers("weapon", broken);
			ResolutionContext context = new ResolutionContext(component, component);

			assertTrue(context.hasStatusModifier(id("broken")));
		}

		@Test
		@DisplayName("should detect unbreakable status")
		void shouldDetectUnbreakableStatus() {
			StatusModifier unbreakable = SimpleStatusModifier.builder("forgero:unbreakable")
					.displayName("Unbreakable")
					.build();
			ModifiableComponent component = ModifiableComponent.createWithModifiers("weapon", unbreakable);
			ResolutionContext context = new ResolutionContext(component, component);

			assertTrue(context.hasStatusModifier(id("unbreakable")));
		}
	}
}
