package com.sigmundgranaas.forgero.core.component.slot;

import com.sigmundgranaas.forgero.core.ForgeroTest;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.slot.ComponentUpgrades;
import com.sigmundgranaas.forgero.core.component.api.slot.ComponentUpgradeSlot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static com.sigmundgranaas.forgero.testutils.ForgeroTestFactory.*;
import static com.sigmundgranaas.forgero.testutils.TestIdentifiers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Behavior tests for ComponentUpgrades - wrapper for upgrade slots.
 */
@DisplayName("ComponentUpgrades")
class ComponentUpgradesBehaviorTest extends ForgeroTest {

	@Nested
	@DisplayName("Creation")
	class CreationTests {

		@Test
		@DisplayName("should create empty upgrades")
		void createsEmptyUpgrades() {
			ComponentUpgrades upgrades = ComponentUpgrades.empty();

			assertTrue(upgrades.isEmpty());
			assertEquals(0, upgrades.size());
		}

		@Test
		@DisplayName("should create upgrades from varargs")
		void createsFromVarargs() {
			var slot1 = upgradeSlot("gem_slot_1", GEM_SLOT_TYPE);
			var slot2 = upgradeSlot("gem_slot_2", GEM_SLOT_TYPE);

			ComponentUpgrades upgrades = ComponentUpgrades.of(slot1, slot2);

			assertEquals(2, upgrades.size());
			assertFalse(upgrades.isEmpty());
		}

		@Test
		@DisplayName("should create upgrades from collection")
		void createsFromCollection() {
			var slot1 = upgradeSlot("gem_slot_1", GEM_SLOT_TYPE);
			var slot2 = upgradeSlot("rune_slot", id("rune"));

			ComponentUpgrades upgrades = ComponentUpgrades.of(List.of(slot1, slot2));

			assertEquals(2, upgrades.size());
		}
	}

	@Nested
	@DisplayName("Query Operations")
	class QueryTests {

		@Test
		@DisplayName("should get slot by ID")
		void getsSlotById() {
			var slot = upgradeSlot("gem_slot", GEM_SLOT_TYPE);
			ComponentUpgrades upgrades = ComponentUpgrades.of(slot);

			Optional<ComponentUpgradeSlot> result = upgrades.getUpgradeSlot(id("gem_slot"));

			assertTrue(result.isPresent());
			assertEquals(id("gem_slot"), result.get().id());
		}

		@Test
		@DisplayName("should return empty for nonexistent slot ID")
		void returnsEmptyForNonexistentId() {
			var slot = upgradeSlot("gem_slot", GEM_SLOT_TYPE);
			ComponentUpgrades upgrades = ComponentUpgrades.of(slot);

			Optional<ComponentUpgradeSlot> result = upgrades.getUpgradeSlot(id("nonexistent"));

			assertTrue(result.isEmpty());
		}

		@Test
		@DisplayName("should check if slot exists with contains()")
		void containsChecksFoSlotExistence() {
			var slot = upgradeSlot("gem_slot", GEM_SLOT_TYPE);
			ComponentUpgrades upgrades = ComponentUpgrades.of(slot);

			assertTrue(upgrades.contains(id("gem_slot")));
			assertFalse(upgrades.contains(id("nonexistent")));
		}

		@Test
		@DisplayName("should return all upgrade slots")
		void returnsAllUpgradeSlots() {
			var slot1 = upgradeSlot("slot1", GEM_SLOT_TYPE);
			var slot2 = upgradeSlot("slot2", id("rune"));

			ComponentUpgrades upgrades = ComponentUpgrades.of(slot1, slot2);

			List<ComponentUpgradeSlot> all = upgrades.allUpgradeSlots();
			assertEquals(2, all.size());
		}
	}

	@Nested
	@DisplayName("Filled Contents")
	class FilledContentsTests {

		@Test
		@DisplayName("should return empty list when no slots have content")
		void returnsEmptyWhenNoContent() {
			var slot1 = upgradeSlot("slot1", GEM_SLOT_TYPE);
			var slot2 = upgradeSlot("slot2", id("rune"));

			ComponentUpgrades upgrades = ComponentUpgrades.of(slot1, slot2);

			List<Component> contents = upgrades.filledContents();
			assertTrue(contents.isEmpty());
		}

		@Test
		@DisplayName("should return filled slot contents")
		void returnsFilledContents() {
			Component gem1 = part(GEM_ID).withTag(GEM_TAG).build();
			Component gem2 = part(id("diamond_gem")).withTag(GEM_TAG).build();

			var slot1 = upgradeSlot("slot1", GEM_SLOT_TYPE, c -> true, gem1);
			var slot2 = upgradeSlot("slot2", GEM_SLOT_TYPE, c -> true, gem2);
			var emptySlot = upgradeSlot("slot3", id("rune"));

			ComponentUpgrades upgrades = ComponentUpgrades.of(slot1, slot2, emptySlot);

			List<Component> contents = upgrades.filledContents();
			assertEquals(2, contents.size());
			assertTrue(contents.contains(gem1));
			assertTrue(contents.contains(gem2));
		}

		@Test
		@DisplayName("should count filled slots correctly")
		void countsFilledSlotsCorrectly() {
			Component gem = part(GEM_ID).withTag(GEM_TAG).build();

			var filledSlot = upgradeSlot("slot1", GEM_SLOT_TYPE, c -> true, gem);
			var emptySlot1 = upgradeSlot("slot2", GEM_SLOT_TYPE);
			var emptySlot2 = upgradeSlot("slot3", id("rune"));

			ComponentUpgrades upgrades = ComponentUpgrades.of(filledSlot, emptySlot1, emptySlot2);

			assertEquals(1, upgrades.filledCount());
			assertEquals(3, upgrades.size());
		}

		@Test
		@DisplayName("should check if all slots are filled")
		void checksIfAllFilled() {
			Component gem1 = part(GEM_ID).withTag(GEM_TAG).build();
			Component gem2 = part(id("diamond_gem")).withTag(GEM_TAG).build();

			var slot1 = upgradeSlot("slot1", GEM_SLOT_TYPE, c -> true, gem1);
			var slot2 = upgradeSlot("slot2", GEM_SLOT_TYPE, c -> true, gem2);
			var emptySlot = upgradeSlot("slot3", id("rune"));

			ComponentUpgrades allFilled = ComponentUpgrades.of(slot1, slot2);
			ComponentUpgrades notAllFilled = ComponentUpgrades.of(slot1, emptySlot);

			assertTrue(allFilled.allFilled());
			assertFalse(notAllFilled.allFilled());
		}
	}

	@Nested
	@DisplayName("Immutable Operations")
	class ImmutableOperationsTests {

		@Test
		@DisplayName("withSlot() should update slot without modifying original")
		void withSlotUpdatesImmutably() {
			var originalSlot = upgradeSlot("gem_slot", GEM_SLOT_TYPE);
			ComponentUpgrades original = ComponentUpgrades.of(originalSlot);

			Component gem = part(GEM_ID).withTag(GEM_TAG).build();
			var filledSlot = upgradeSlot("gem_slot", GEM_SLOT_TYPE, c -> true, gem);

			ComponentUpgrades modified = original.withSlot(filledSlot);

			// Original should still be empty
			assertTrue(original.getUpgradeSlot(id("gem_slot")).get().getContent().isEmpty());

			// Modified should have content
			assertTrue(modified.getUpgradeSlot(id("gem_slot")).get().getContent().isPresent());
		}

		@Test
		@DisplayName("withSlot() should add new slot if ID doesn't exist")
		void withSlotAddsNewSlot() {
			var slot1 = upgradeSlot("slot1", GEM_SLOT_TYPE);
			ComponentUpgrades original = ComponentUpgrades.of(slot1);

			var newSlot = upgradeSlot("slot2", id("rune"));
			ComponentUpgrades modified = original.withSlot(newSlot);

			assertEquals(1, original.size());
			assertEquals(2, modified.size());
		}
	}

	@Nested
	@DisplayName("Empty Upgrades")
	class EmptyUpgradesTests {

		@Test
		@DisplayName("empty upgrades should handle all operations gracefully")
		void emptyHandlesAllOperations() {
			ComponentUpgrades empty = ComponentUpgrades.empty();

			assertTrue(empty.isEmpty());
			assertEquals(0, empty.size());
			assertEquals(0, empty.filledCount());
			assertTrue(empty.getUpgradeSlot(id("any")).isEmpty());
			assertFalse(empty.contains(id("any")));
			assertTrue(empty.allUpgradeSlots().isEmpty());
			assertTrue(empty.filledContents().isEmpty());
			assertTrue(empty.allFilled()); // Vacuously true - all 0 slots are filled
		}

		@Test
		@DisplayName("withSlot() should work on empty upgrades")
		void withSlotWorksOnEmpty() {
			ComponentUpgrades empty = ComponentUpgrades.empty();
			var slot = upgradeSlot("gem_slot", GEM_SLOT_TYPE);

			ComponentUpgrades modified = empty.withSlot(slot);

			assertEquals(0, empty.size());
			assertEquals(1, modified.size());
		}
	}

	@Nested
	@DisplayName("Slot Access")
	class SlotAccessTests {

		@Test
		@DisplayName("should access underlying SlotContainer")
		void accessesUnderlyingContainer() {
			var slot1 = upgradeSlot("slot1", GEM_SLOT_TYPE);
			var slot2 = upgradeSlot("slot2", id("rune"));

			ComponentUpgrades upgrades = ComponentUpgrades.of(slot1, slot2);

			// slots() returns the underlying SlotContainer
			assertEquals(2, upgrades.slots().size());
			assertTrue(upgrades.slots().contains(id("slot1")));
		}

		@Test
		@DisplayName("should filter to only ComponentUpgradeSlot instances")
		void filtersToUpgradeSlots() {
			// Create slots using the factory methods
			var slot1 = upgradeSlot("gem_slot", GEM_SLOT_TYPE);
			var slot2 = upgradeSlot("rune_slot", id("rune"));

			ComponentUpgrades upgrades = ComponentUpgrades.of(slot1, slot2);

			// All should be ComponentUpgradeSlot
			List<ComponentUpgradeSlot> all = upgrades.allUpgradeSlots();
			for (ComponentUpgradeSlot slot : all) {
				assertInstanceOf(ComponentUpgradeSlot.class, slot);
			}
		}
	}
}
