package com.sigmundgranaas.forgero.core.status;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.status.api.StatusModifier;
import com.sigmundgranaas.forgero.core.status.api.StatusModifierSlot;
import com.sigmundgranaas.forgero.core.status.api.StatusModifierSlotContainer;
import com.sigmundgranaas.forgero.core.status.impl.SimpleStatusModifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StatusModifierSlotContainer Tests")
class StatusModifierSlotContainerTest {

	private static OpenIdentifier id(String path) {
		return OpenIdentifier.of(path);
	}

	private static StatusModifier createModifier(String name) {
		return createModifier(name, 0);
	}

	private static StatusModifier createModifier(String name, int priority) {
		return SimpleStatusModifier.builder("forgero:" + name)
				.displayName(name.substring(0, 1).toUpperCase() + name.substring(1))
				.priority(priority)
				.build();
	}

	private static StatusModifier createIncompatibleModifier(String name, String... incompatibilities) {
		var builder = SimpleStatusModifier.builder("forgero:" + name)
				.displayName(name.substring(0, 1).toUpperCase() + name.substring(1));
		for (String inc : incompatibilities) {
			builder.incompatibleWith("forgero:" + inc);
		}
		return builder.build();
	}

	@Nested
	@DisplayName("Container Construction")
	class Construction {

		@Test
		@DisplayName("should create empty container")
		void shouldCreateEmptyContainer() {
			StatusModifierSlotContainer container = StatusModifierSlotContainer.empty();

			assertEquals(0, container.size());
			assertTrue(container.isEmpty());
			assertTrue(container.allModifiers().isEmpty());
		}

		@Test
		@DisplayName("should create container with capacity")
		void shouldCreateContainerWithCapacity() {
			StatusModifierSlotContainer container = StatusModifierSlotContainer.withCapacity(3);

			assertEquals(3, container.size());
			assertEquals(0, container.filledCount());
			assertEquals(3, container.emptyCount());
			assertTrue(container.isEmpty());
		}

		@Test
		@DisplayName("should create container from slots")
		void shouldCreateContainerFromSlots() {
			StatusModifierSlot slot1 = StatusModifierSlot.emptyAt(0);
			StatusModifierSlot slot2 = StatusModifierSlot.filled(id("slot_1"), 1, createModifier("sharp"));

			StatusModifierSlotContainer container = StatusModifierSlotContainer.of(slot1, slot2);

			assertEquals(2, container.size());
			assertEquals(1, container.filledCount());
			assertEquals(1, container.emptyCount());
		}

		@Test
		@DisplayName("should create container from collection")
		void shouldCreateContainerFromCollection() {
			List<StatusModifierSlot> slots = List.of(
					StatusModifierSlot.emptyAt(0),
					StatusModifierSlot.emptyAt(1),
					StatusModifierSlot.emptyAt(2)
			);

			StatusModifierSlotContainer container = StatusModifierSlotContainer.of(slots);

			assertEquals(3, container.size());
		}
	}

	@Nested
	@DisplayName("Query Operations")
	class QueryOperations {

		@Test
		@DisplayName("should get slot by id")
		void shouldGetSlotById() {
			StatusModifierSlot slot = StatusModifierSlot.empty(id("target_slot"), 0);
			StatusModifierSlotContainer container = StatusModifierSlotContainer.of(slot);

			Optional<StatusModifierSlot> found = container.get(id("target_slot"));

			assertTrue(found.isPresent());
			assertEquals(slot, found.get());
		}

		@Test
		@DisplayName("should return empty for unknown id")
		void shouldReturnEmptyForUnknownId() {
			StatusModifierSlotContainer container = StatusModifierSlotContainer.withCapacity(1);

			Optional<StatusModifierSlot> found = container.get(id("unknown"));

			assertTrue(found.isEmpty());
		}

		@Test
		@DisplayName("should get slot by index")
		void shouldGetSlotByIndex() {
			StatusModifierSlotContainer container = StatusModifierSlotContainer.withCapacity(3);

			Optional<StatusModifierSlot> found = container.getByIndex(1);

			assertTrue(found.isPresent());
			assertEquals(1, found.get().index());
		}

		@Test
		@DisplayName("should return all modifiers")
		void shouldReturnAllModifiers() {
			StatusModifier sharp = createModifier("sharp");
			StatusModifier durable = createModifier("durable");

			StatusModifierSlotContainer container = StatusModifierSlotContainer.of(
					StatusModifierSlot.filled(id("slot_0"), 0, sharp),
					StatusModifierSlot.emptyAt(1),
					StatusModifierSlot.filled(id("slot_2"), 2, durable)
			);

			List<StatusModifier> modifiers = container.allModifiers();

			assertEquals(2, modifiers.size());
			assertTrue(modifiers.contains(sharp));
			assertTrue(modifiers.contains(durable));
		}

		@Test
		@DisplayName("should return modifiers by priority")
		void shouldReturnModifiersByPriority() {
			StatusModifier low = createModifier("low", 1);
			StatusModifier high = createModifier("high", 10);
			StatusModifier medium = createModifier("medium", 5);

			StatusModifierSlotContainer container = StatusModifierSlotContainer.of(
					StatusModifierSlot.filled(id("slot_0"), 0, low),
					StatusModifierSlot.filled(id("slot_1"), 1, high),
					StatusModifierSlot.filled(id("slot_2"), 2, medium)
			);

			List<StatusModifier> modifiers = container.modifiersByPriority();

			assertEquals(3, modifiers.size());
			assertEquals(high, modifiers.get(0)); // Highest priority first
			assertEquals(medium, modifiers.get(1));
			assertEquals(low, modifiers.get(2));
		}

		@Test
		@DisplayName("should check if has modifier")
		void shouldCheckIfHasModifier() {
			StatusModifier sharp = createModifier("sharp");
			StatusModifierSlotContainer container = StatusModifierSlotContainer.of(
					StatusModifierSlot.filled(id("slot_0"), 0, sharp)
			);

			assertTrue(container.hasModifier(id("sharp")));
			assertFalse(container.hasModifier(id("durable")));
		}

		@Test
		@DisplayName("should find modifier by id")
		void shouldFindModifierById() {
			StatusModifier sharp = createModifier("sharp");
			StatusModifierSlotContainer container = StatusModifierSlotContainer.of(
					StatusModifierSlot.filled(id("slot_0"), 0, sharp)
			);

			Optional<StatusModifier> found = container.findModifier(id("sharp"));

			assertTrue(found.isPresent());
			assertEquals(sharp, found.get());
		}

		@Test
		@DisplayName("should return empty slots")
		void shouldReturnEmptySlots() {
			StatusModifierSlotContainer container = StatusModifierSlotContainer.of(
					StatusModifierSlot.filled(id("slot_0"), 0, createModifier("sharp")),
					StatusModifierSlot.emptyAt(1),
					StatusModifierSlot.emptyAt(2)
			);

			List<StatusModifierSlot> emptySlots = container.emptySlots();

			assertEquals(2, emptySlots.size());
			assertTrue(emptySlots.stream().allMatch(StatusModifierSlot::isEmpty));
		}

		@Test
		@DisplayName("should return filled slots")
		void shouldReturnFilledSlots() {
			StatusModifierSlotContainer container = StatusModifierSlotContainer.of(
					StatusModifierSlot.filled(id("slot_0"), 0, createModifier("sharp")),
					StatusModifierSlot.emptyAt(1),
					StatusModifierSlot.filled(id("slot_2"), 2, createModifier("durable"))
			);

			List<StatusModifierSlot> filledSlots = container.filledSlots();

			assertEquals(2, filledSlots.size());
			assertTrue(filledSlots.stream().allMatch(StatusModifierSlot::isFilled));
		}

		@Test
		@DisplayName("should find first empty slot")
		void shouldFindFirstEmptySlot() {
			StatusModifierSlotContainer container = StatusModifierSlotContainer.of(
					StatusModifierSlot.filled(id("slot_0"), 0, createModifier("sharp")),
					StatusModifierSlot.emptyAt(1),
					StatusModifierSlot.emptyAt(2)
			);

			Optional<StatusModifierSlot> firstEmpty = container.firstEmpty();

			assertTrue(firstEmpty.isPresent());
			assertEquals(1, firstEmpty.get().index());
		}

		@Test
		@DisplayName("should check if full")
		void shouldCheckIfFull() {
			StatusModifierSlotContainer full = StatusModifierSlotContainer.of(
					StatusModifierSlot.filled(id("slot_0"), 0, createModifier("sharp")),
					StatusModifierSlot.filled(id("slot_1"), 1, createModifier("durable"))
			);

			StatusModifierSlotContainer notFull = StatusModifierSlotContainer.of(
					StatusModifierSlot.filled(id("slot_0"), 0, createModifier("sharp")),
					StatusModifierSlot.emptyAt(1)
			);

			assertTrue(full.isFull());
			assertFalse(notFull.isFull());
		}
	}

	@Nested
	@DisplayName("Transformation Operations")
	class TransformationOperations {

		@Test
		@DisplayName("should add modifier to first empty slot")
		void shouldAddModifierToFirstEmptySlot() {
			StatusModifierSlotContainer container = StatusModifierSlotContainer.withCapacity(2);
			StatusModifier sharp = createModifier("sharp");

			StatusModifierSlotContainer updated = container.withModifier(sharp);

			// Original unchanged
			assertEquals(0, container.filledCount());

			// New container has modifier
			assertEquals(1, updated.filledCount());
			assertTrue(updated.hasModifier(id("sharp")));
		}

		@Test
		@DisplayName("should return same container when no empty slots")
		void shouldReturnSameContainerWhenNoEmptySlots() {
			StatusModifierSlotContainer container = StatusModifierSlotContainer.of(
					StatusModifierSlot.filled(id("slot_0"), 0, createModifier("sharp"))
			);

			StatusModifierSlotContainer result = container.withModifier(createModifier("durable"));

			assertEquals(container, result);
			assertFalse(result.hasModifier(id("durable")));
		}

		@Test
		@DisplayName("should remove modifier by id")
		void shouldRemoveModifierById() {
			StatusModifierSlotContainer container = StatusModifierSlotContainer.of(
					StatusModifierSlot.filled(id("slot_0"), 0, createModifier("sharp")),
					StatusModifierSlot.filled(id("slot_1"), 1, createModifier("durable"))
			);

			StatusModifierSlotContainer updated = container.removeModifier(id("sharp"));

			assertEquals(1, updated.filledCount());
			assertFalse(updated.hasModifier(id("sharp")));
			assertTrue(updated.hasModifier(id("durable")));
		}

		@Test
		@DisplayName("should preserve slot when removing modifier")
		void shouldPreserveSlotWhenRemovingModifier() {
			StatusModifierSlotContainer container = StatusModifierSlotContainer.of(
					StatusModifierSlot.filled(id("slot_0"), 0, createModifier("sharp"))
			);

			StatusModifierSlotContainer updated = container.removeModifier(id("sharp"));

			assertEquals(1, updated.size());
			assertTrue(updated.get(id("slot_0")).isPresent());
			assertTrue(updated.get(id("slot_0")).get().isEmpty());
		}

		@Test
		@DisplayName("should clear all modifiers")
		void shouldClearAllModifiers() {
			StatusModifierSlotContainer container = StatusModifierSlotContainer.of(
					StatusModifierSlot.filled(id("slot_0"), 0, createModifier("sharp")),
					StatusModifierSlot.filled(id("slot_1"), 1, createModifier("durable"))
			);

			StatusModifierSlotContainer cleared = container.clearAll();

			assertEquals(2, cleared.size()); // Slots preserved
			assertEquals(0, cleared.filledCount()); // But empty
			assertTrue(cleared.isEmpty());
		}

		@Test
		@DisplayName("should replace slot")
		void shouldReplaceSlot() {
			StatusModifierSlotContainer container = StatusModifierSlotContainer.withCapacity(2);
			StatusModifierSlot filledSlot = StatusModifierSlot.filled(id("status_slot_0"), 0, createModifier("sharp"));

			StatusModifierSlotContainer updated = container.withSlot(filledSlot);

			assertEquals(1, updated.filledCount());
			assertTrue(updated.hasModifier(id("sharp")));
		}
	}

	@Nested
	@DisplayName("Compatibility Checking")
	class CompatibilityChecking {

		@Test
		@DisplayName("should allow compatible modifier installation")
		void shouldAllowCompatibleModifierInstallation() {
			StatusModifier sharp = createModifier("sharp");
			StatusModifier durable = createModifier("durable");

			StatusModifierSlotContainer container = StatusModifierSlotContainer.of(
					StatusModifierSlot.filled(id("slot_0"), 0, sharp),
					StatusModifierSlot.emptyAt(1)
			);

			assertTrue(container.canInstall(durable));
		}

		@Test
		@DisplayName("should reject incompatible modifier")
		void shouldRejectIncompatibleModifier() {
			StatusModifier sharp = createIncompatibleModifier("sharp", "blunt");
			StatusModifier blunt = createModifier("blunt");

			StatusModifierSlotContainer container = StatusModifierSlotContainer.of(
					StatusModifierSlot.filled(id("slot_0"), 0, sharp),
					StatusModifierSlot.emptyAt(1)
			);

			assertFalse(container.canInstall(blunt));
		}

		@Test
		@DisplayName("should reject duplicate modifier")
		void shouldRejectDuplicateModifier() {
			StatusModifier sharp = createModifier("sharp");

			StatusModifierSlotContainer container = StatusModifierSlotContainer.of(
					StatusModifierSlot.filled(id("slot_0"), 0, sharp),
					StatusModifierSlot.emptyAt(1)
			);

			assertFalse(container.canInstall(sharp));
		}

		@Test
		@DisplayName("should try install compatible modifier")
		void shouldTryInstallCompatibleModifier() {
			StatusModifierSlotContainer container = StatusModifierSlotContainer.withCapacity(2);
			StatusModifier sharp = createModifier("sharp");

			Optional<StatusModifierSlotContainer> result = container.tryInstall(sharp);

			assertTrue(result.isPresent());
			assertTrue(result.get().hasModifier(id("sharp")));
		}

		@Test
		@DisplayName("should return empty for incompatible installation")
		void shouldReturnEmptyForIncompatibleInstallation() {
			StatusModifier sharp = createIncompatibleModifier("sharp", "blunt");
			StatusModifier blunt = createModifier("blunt");

			StatusModifierSlotContainer container = StatusModifierSlotContainer.of(
					StatusModifierSlot.filled(id("slot_0"), 0, sharp),
					StatusModifierSlot.emptyAt(1)
			);

			Optional<StatusModifierSlotContainer> result = container.tryInstall(blunt);

			assertTrue(result.isEmpty());
		}

		@Test
		@DisplayName("should return empty when no slots available")
		void shouldReturnEmptyWhenNoSlotsAvailable() {
			StatusModifierSlotContainer container = StatusModifierSlotContainer.of(
					StatusModifierSlot.filled(id("slot_0"), 0, createModifier("sharp"))
			);

			Optional<StatusModifierSlotContainer> result = container.tryInstall(createModifier("durable"));

			assertTrue(result.isEmpty());
		}
	}
}
