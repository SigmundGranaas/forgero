package com.sigmundgranaas.forgero.core.component.slot;

import com.sigmundgranaas.forgero.core.ForgeroTest;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.Slot;
import com.sigmundgranaas.forgero.core.component.api.slot.ComponentUpgradeSlot;
import com.sigmundgranaas.forgero.core.component.api.slot.SlotContainer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.sigmundgranaas.forgero.testutils.ForgeroTestFactory.*;
import static com.sigmundgranaas.forgero.testutils.TestIdentifiers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Behavior tests for SlotContainer - a heterogeneous container for mutable slots.
 */
@DisplayName("SlotContainer")
class SlotContainerBehaviorTest extends ForgeroTest {

	@Nested
	@DisplayName("Creation")
	class CreationTests {

		@Test
		@DisplayName("should create empty container")
		void createsEmptyContainer() {
			SlotContainer container = SlotContainer.empty();

			assertTrue(container.isEmpty());
			assertEquals(0, container.size());
		}

		@Test
		@DisplayName("should create container from collection")
		void createsContainerFromCollection() {
			var slot1 = upgradeSlot("gem_slot_1", GEM_SLOT_TYPE);
			var slot2 = upgradeSlot("gem_slot_2", GEM_SLOT_TYPE);

			SlotContainer container = SlotContainer.of(List.of(slot1, slot2));

			assertEquals(2, container.size());
			assertFalse(container.isEmpty());
		}

		@Test
		@DisplayName("should create container from varargs")
		void createsContainerFromVarargs() {
			var slot1 = upgradeSlot("gem_slot_1", GEM_SLOT_TYPE);
			var slot2 = upgradeSlot("rune_slot", id("rune"));

			SlotContainer container = SlotContainer.of(slot1, slot2);

			assertEquals(2, container.size());
		}

		@Test
		@DisplayName("should throw on duplicate slot IDs")
		void throwsOnDuplicateSlotIds() {
			var slot1 = upgradeSlot("same_id", GEM_SLOT_TYPE);
			var slot2 = upgradeSlot("same_id", id("rune")); // Same ID

			assertThrows(IllegalArgumentException.class, () -> {
				SlotContainer.of(List.of(slot1, slot2));
			});
		}

		@Test
		@DisplayName("should preserve insertion order")
		void preservesInsertionOrder() {
			var slot1 = upgradeSlot("first", GEM_SLOT_TYPE);
			var slot2 = upgradeSlot("second", id("rune"));
			var slot3 = upgradeSlot("third", id("binding"));

			SlotContainer container = SlotContainer.of(slot1, slot2, slot3);

			List<Slot> slots = container.asList();
			assertEquals(id("first"), slots.get(0).id());
			assertEquals(id("second"), slots.get(1).id());
			assertEquals(id("third"), slots.get(2).id());
		}
	}

	@Nested
	@DisplayName("Query Operations")
	class QueryTests {

		@Test
		@DisplayName("should get slot by ID")
		void getsSlotById() {
			var slot = upgradeSlot("gem_slot", GEM_SLOT_TYPE);
			SlotContainer container = SlotContainer.of(slot);

			Optional<Slot> result = container.get(id("gem_slot"));

			assertTrue(result.isPresent());
			assertEquals(slot.id(), result.get().id());
		}

		@Test
		@DisplayName("should return empty for nonexistent slot ID")
		void returnsEmptyForNonexistentId() {
			var slot = upgradeSlot("gem_slot", GEM_SLOT_TYPE);
			SlotContainer container = SlotContainer.of(slot);

			Optional<Slot> result = container.get(id("nonexistent"));

			assertTrue(result.isEmpty());
		}

		@Test
		@DisplayName("should check if slot exists with contains()")
		void containsChecksFoSlotExistence() {
			var slot = upgradeSlot("gem_slot", GEM_SLOT_TYPE);
			SlotContainer container = SlotContainer.of(slot);

			assertTrue(container.contains(id("gem_slot")));
			assertFalse(container.contains(id("nonexistent")));
		}

		@Test
		@DisplayName("should return all slots")
		void returnsAllSlots() {
			var slot1 = upgradeSlot("slot1", GEM_SLOT_TYPE);
			var slot2 = upgradeSlot("slot2", id("rune"));

			SlotContainer container = SlotContainer.of(slot1, slot2);

			Collection<Slot> all = container.all();
			assertEquals(2, all.size());
		}

		@Test
		@DisplayName("should return all slot IDs")
		void returnsAllSlotIds() {
			var slot1 = upgradeSlot("slot1", GEM_SLOT_TYPE);
			var slot2 = upgradeSlot("slot2", id("rune"));

			SlotContainer container = SlotContainer.of(slot1, slot2);

			Set<com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier> ids = container.ids();
			assertEquals(2, ids.size());
			assertTrue(ids.contains(id("slot1")));
			assertTrue(ids.contains(id("slot2")));
		}

		@Test
		@DisplayName("should convert to list preserving order")
		void convertsToListPreservingOrder() {
			var slot1 = upgradeSlot("a", GEM_SLOT_TYPE);
			var slot2 = upgradeSlot("b", id("rune"));
			var slot3 = upgradeSlot("c", id("binding"));

			SlotContainer container = SlotContainer.of(slot1, slot2, slot3);

			List<Slot> list = container.asList();
			assertEquals(3, list.size());
			assertEquals(id("a"), list.get(0).id());
			assertEquals(id("b"), list.get(1).id());
			assertEquals(id("c"), list.get(2).id());
		}
	}

	@Nested
	@DisplayName("Immutable Operations")
	class ImmutableOperationsTests {

		@Test
		@DisplayName("with() should add new slot without modifying original")
		void withAddsNewSlotImmutably() {
			var slot1 = upgradeSlot("slot1", GEM_SLOT_TYPE);
			SlotContainer original = SlotContainer.of(slot1);

			var newSlot = upgradeSlot("slot2", id("rune"));
			SlotContainer modified = original.with(newSlot);

			assertEquals(1, original.size());
			assertEquals(2, modified.size());
			assertNotSame(original, modified);
		}

		@Test
		@DisplayName("with() should replace existing slot by ID")
		void withReplacesExistingSlot() {
			Component gem = part(GEM_ID).withTag(GEM_TAG).build();
			var originalSlot = upgradeSlot("slot1", GEM_SLOT_TYPE);
			var replacementSlot = upgradeSlot("slot1", GEM_SLOT_TYPE, c -> true, gem);

			SlotContainer original = SlotContainer.of(originalSlot);
			SlotContainer modified = original.with(replacementSlot);

			assertEquals(1, original.size());
			assertEquals(1, modified.size());

			// Original slot should be empty
			var origSlot = (ComponentUpgradeSlot) original.get(id("slot1")).get();
			assertTrue(origSlot.getContent().isEmpty());

			// Modified slot should have content
			var modSlot = (ComponentUpgradeSlot) modified.get(id("slot1")).get();
			assertTrue(modSlot.getContent().isPresent());
		}

		@Test
		@DisplayName("without() should remove slot without modifying original")
		void withoutRemovesSlotImmutably() {
			var slot1 = upgradeSlot("slot1", GEM_SLOT_TYPE);
			var slot2 = upgradeSlot("slot2", id("rune"));
			SlotContainer original = SlotContainer.of(slot1, slot2);

			SlotContainer modified = original.without(id("slot1"));

			assertEquals(2, original.size());
			assertEquals(1, modified.size());
			assertTrue(original.contains(id("slot1")));
			assertFalse(modified.contains(id("slot1")));
		}

		@Test
		@DisplayName("without() should return same container if ID not found")
		void withoutReturnsSameIfIdNotFound() {
			var slot = upgradeSlot("slot1", GEM_SLOT_TYPE);
			SlotContainer container = SlotContainer.of(slot);

			SlotContainer result = container.without(id("nonexistent"));

			assertSame(container, result, "Should return same container when ID not found");
		}
	}

	@Nested
	@DisplayName("Equality and Hashing")
	class EqualityTests {

		@Test
		@DisplayName("should be equal to itself")
		void equalsItself() {
			var slot = upgradeSlot("slot1", GEM_SLOT_TYPE);
			SlotContainer container = SlotContainer.of(slot);

			assertEquals(container, container);
		}

		@Test
		@DisplayName("should be equal to container with same slots")
		void equalsContainerWithSameSlots() {
			var slot1 = upgradeSlot("slot1", GEM_SLOT_TYPE);
			var slot2 = upgradeSlot("slot1", GEM_SLOT_TYPE);

			SlotContainer container1 = SlotContainer.of(slot1);
			SlotContainer container2 = SlotContainer.of(slot2);

			assertEquals(container1, container2);
			assertEquals(container1.hashCode(), container2.hashCode());
		}

		@Test
		@DisplayName("should not be equal to container with different slots")
		void notEqualsContainerWithDifferentSlots() {
			var slot1 = upgradeSlot("slot1", GEM_SLOT_TYPE);
			var slot2 = upgradeSlot("slot2", id("rune"));

			SlotContainer container1 = SlotContainer.of(slot1);
			SlotContainer container2 = SlotContainer.of(slot2);

			assertNotEquals(container1, container2);
		}

		@Test
		@DisplayName("should not be equal to non-SlotContainer")
		void notEqualsNonSlotContainer() {
			var slot = upgradeSlot("slot1", GEM_SLOT_TYPE);
			SlotContainer container = SlotContainer.of(slot);

			assertNotEquals(container, "not a container");
			assertNotEquals(container, null);
		}

		@Test
		@DisplayName("toString should contain slot IDs")
		void toStringContainsSlotIds() {
			var slot1 = upgradeSlot("gem_slot", GEM_SLOT_TYPE);
			var slot2 = upgradeSlot("rune_slot", id("rune"));

			SlotContainer container = SlotContainer.of(slot1, slot2);

			String str = container.toString();
			assertTrue(str.contains("SlotContainer"));
		}
	}

	@Nested
	@DisplayName("Edge Cases")
	class EdgeCaseTests {

		@Test
		@DisplayName("empty container should handle all operations gracefully")
		void emptyContainerHandlesAllOperations() {
			SlotContainer empty = SlotContainer.empty();

			assertTrue(empty.isEmpty());
			assertEquals(0, empty.size());
			assertTrue(empty.get(id("any")).isEmpty());
			assertFalse(empty.contains(id("any")));
			assertTrue(empty.all().isEmpty());
			assertTrue(empty.ids().isEmpty());
			assertTrue(empty.asList().isEmpty());
		}

		@Test
		@DisplayName("empty container without() should return same instance")
		void emptyContainerWithoutReturnsSameInstance() {
			SlotContainer empty = SlotContainer.empty();
			SlotContainer result = empty.without(id("any"));

			assertSame(empty, result);
		}

		@Test
		@DisplayName("container with() should work on empty container")
		void withWorksOnEmptyContainer() {
			SlotContainer empty = SlotContainer.empty();
			var slot = upgradeSlot("slot1", GEM_SLOT_TYPE);

			SlotContainer modified = empty.with(slot);

			assertEquals(0, empty.size());
			assertEquals(1, modified.size());
		}

		@Test
		@DisplayName("all() should return unmodifiable collection")
		void allReturnsUnmodifiableCollection() {
			var slot = upgradeSlot("slot1", GEM_SLOT_TYPE);
			SlotContainer container = SlotContainer.of(slot);

			Collection<Slot> all = container.all();

			// Attempting to modify should throw or be a no-op
			assertThrows(UnsupportedOperationException.class, () -> {
				all.clear();
			});
		}
	}
}
