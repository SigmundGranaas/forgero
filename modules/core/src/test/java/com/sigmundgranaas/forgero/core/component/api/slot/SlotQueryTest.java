package com.sigmundgranaas.forgero.core.component.api.slot;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.slot.impl.SlotQueryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for SlotQuery fluent builder API.
 */
class SlotQueryTest {

	// Test slot types
	private static final OpenIdentifier GEM_TYPE = OpenIdentifier.parse("forgero:gem");
	private static final OpenIdentifier BINDING_TYPE = OpenIdentifier.parse("forgero:binding");
	private static final OpenIdentifier HANDLE_TYPE = OpenIdentifier.parse("forgero:handle");
	private static final OpenIdentifier OFFENSIVE_GEM_TYPE = OpenIdentifier.parse("forgero:offensive_gem_slot");

	// Test components
	private Component gemUpgrade;
	private Component bindingUpgrade;

	// Test slots
	private List<ComponentUpgradeSlot> testSlots;

	@BeforeEach
	void setUp() {
		// Create test upgrades
		gemUpgrade = createTestUpgrade("ruby");
		bindingUpgrade = createTestUpgrade("leather_binding");

		// Create test slots with various types and states
		testSlots = List.of(
			// Gem slots
			createEmptySlot("gem_slot_1", GEM_TYPE, "Gem Slot 1"),
			createEmptySlot("gem_slot_2", GEM_TYPE, "Gem Slot 2"),
			createFilledSlot("gem_slot_3", GEM_TYPE, "Gem Slot 3", gemUpgrade),

			// Binding slots
			createEmptySlot("binding_slot_1", BINDING_TYPE, "Binding Slot"),
			createFilledSlot("binding_slot_2", BINDING_TYPE, "Binding Slot 2", bindingUpgrade),

			// Handle slots
			createEmptySlot("handle_slot_1", HANDLE_TYPE, "Handle Slot"),

			// Offensive gem slot (for type filtering tests)
			createEmptySlot("offensive_gem_1", OFFENSIVE_GEM_TYPE, "Offensive Gem Slot")
		);
	}

	// ============================================================
	// Type Filtering Tests
	// ============================================================

	@Nested
	@DisplayName("Type Filtering")
	class TypeFilteringTests {

		@Test
		@DisplayName("ofType() should filter by single type")
		void ofType_FiltersByType() {
			SlotQuery<ComponentUpgradeSlot> query = new SlotQueryImpl<>(testSlots);

			List<ComponentUpgradeSlot> result = query.ofType(GEM_TYPE).execute();

			assertEquals(3, result.size(), "Should find all gem slots");
			assertTrue(result.stream().allMatch(s -> s.slotType().equals(GEM_TYPE)),
				"All results should be gem type");
		}

		@Test
		@DisplayName("ofType() should return empty list for non-existent type")
		void ofType_ReturnsEmptyForNonExistentType() {
			SlotQuery<ComponentUpgradeSlot> query = new SlotQueryImpl<>(testSlots);
			OpenIdentifier nonExistent = OpenIdentifier.parse("forgero:nonexistent");

			List<ComponentUpgradeSlot> result = query.ofType(nonExistent).execute();

			assertTrue(result.isEmpty(), "Should return empty list for non-existent type");
		}

		@Test
		@DisplayName("ofAnyType() should filter by multiple types")
		void ofAnyType_FiltersByMultipleTypes() {
			SlotQuery<ComponentUpgradeSlot> query = new SlotQueryImpl<>(testSlots);

			List<ComponentUpgradeSlot> result = query.ofAnyType(GEM_TYPE, BINDING_TYPE).execute();

			assertEquals(5, result.size(), "Should find all gem and binding slots");
			assertTrue(result.stream().allMatch(s ->
					s.slotType().equals(GEM_TYPE) || s.slotType().equals(BINDING_TYPE)),
				"All results should be gem or binding type");
		}

		@Test
		@DisplayName("ofAnyType() with single type should behave like ofType()")
		void ofAnyType_SingleType_BehavesLikeOfType() {
			SlotQuery<ComponentUpgradeSlot> query = new SlotQueryImpl<>(testSlots);

			List<ComponentUpgradeSlot> result = query.ofAnyType(HANDLE_TYPE).execute();

			assertEquals(1, result.size(), "Should find handle slot");
			assertEquals(HANDLE_TYPE, result.get(0).slotType(), "Should be handle type");
		}
	}

	// ============================================================
	// State Filtering Tests
	// ============================================================

	@Nested
	@DisplayName("State Filtering")
	class StateFilteringTests {

		@Test
		@DisplayName("onlyEmpty() should filter empty slots")
		void onlyEmpty_FiltersEmptySlots() {
			SlotQuery<ComponentUpgradeSlot> query = new SlotQueryImpl<>(testSlots);

			List<ComponentUpgradeSlot> result = query.onlyEmpty().execute();

			assertEquals(5, result.size(), "Should find all empty slots");
			assertTrue(result.stream().allMatch(ComponentUpgradeSlot::isEmpty),
				"All results should be empty");
		}

		@Test
		@DisplayName("onlyFilled() should filter filled slots")
		void onlyFilled_FiltersFilledSlots() {
			SlotQuery<ComponentUpgradeSlot> query = new SlotQueryImpl<>(testSlots);

			List<ComponentUpgradeSlot> result = query.onlyFilled().execute();

			assertEquals(2, result.size(), "Should find all filled slots");
			assertTrue(result.stream().noneMatch(ComponentUpgradeSlot::isEmpty),
				"All results should be filled");
		}

		@Test
		@DisplayName("onlyEmpty() combined with type filter")
		void onlyEmpty_CombinedWithTypeFilter() {
			SlotQuery<ComponentUpgradeSlot> query = new SlotQueryImpl<>(testSlots);

			List<ComponentUpgradeSlot> result = query
				.ofType(GEM_TYPE)
				.onlyEmpty()
				.execute();

			assertEquals(2, result.size(), "Should find empty gem slots only");
			assertTrue(result.stream().allMatch(s ->
					s.slotType().equals(GEM_TYPE) && s.isEmpty()),
				"All results should be empty gem slots");
		}

		@Test
		@DisplayName("onlyFilled() combined with type filter")
		void onlyFilled_CombinedWithTypeFilter() {
			SlotQuery<ComponentUpgradeSlot> query = new SlotQueryImpl<>(testSlots);

			List<ComponentUpgradeSlot> result = query
				.ofType(BINDING_TYPE)
				.onlyFilled()
				.execute();

			assertEquals(1, result.size(), "Should find filled binding slot");
			ComponentUpgradeSlot slot = result.get(0);
			assertEquals(BINDING_TYPE, slot.slotType(), "Should be binding type");
			assertFalse(slot.isEmpty(), "Should be filled");
		}
	}

	// ============================================================
	// Custom Predicate Tests
	// ============================================================

	@Nested
	@DisplayName("Custom Predicate Filtering")
	class CustomPredicateTests {

		@Test
		@DisplayName("matching() should apply custom predicate")
		void matching_AppliesCustomPredicate() {
			SlotQuery<ComponentUpgradeSlot> query = new SlotQueryImpl<>(testSlots);
			Predicate<ComponentUpgradeSlot> descriptionContainsGem = slot ->
				slot.description().toLowerCase().contains("gem");

			List<ComponentUpgradeSlot> result = query.matching(descriptionContainsGem).execute();

			assertEquals(4, result.size(), "Should find all slots with 'gem' in description");
			assertTrue(result.stream().allMatch(s ->
					s.description().toLowerCase().contains("gem")),
				"All results should have 'gem' in description");
		}

		@Test
		@DisplayName("matching() should chain with other filters")
		void matching_ChainsWithOtherFilters() {
			SlotQuery<ComponentUpgradeSlot> query = new SlotQueryImpl<>(testSlots);
			Predicate<ComponentUpgradeSlot> idEndsWithOne = slot ->
				slot.id().toString().endsWith("1");

			List<ComponentUpgradeSlot> result = query
				.onlyEmpty()
				.matching(idEndsWithOne)
				.execute();

			assertTrue(result.size() >= 1, "Should find at least one empty slot ending in 1");
			assertTrue(result.stream().allMatch(s ->
					s.isEmpty() && s.id().toString().endsWith("1")),
				"All results should be empty and have ID ending in 1");
		}

		@Test
		@DisplayName("multiple matching() calls should AND predicates")
		void multipleMatching_ANDsPredicates() {
			SlotQuery<ComponentUpgradeSlot> query = new SlotQueryImpl<>(testSlots);
			Predicate<ComponentUpgradeSlot> isGemType = slot -> slot.slotType().equals(GEM_TYPE);
			Predicate<ComponentUpgradeSlot> descriptionHasSlot1 = slot ->
				slot.description().contains("Slot 1");

			List<ComponentUpgradeSlot> result = query
				.matching(isGemType)
				.matching(descriptionHasSlot1)
				.execute();

			assertEquals(1, result.size(), "Should find exactly one slot matching both");
			ComponentUpgradeSlot slot = result.get(0);
			assertEquals(GEM_TYPE, slot.slotType(), "Should be gem type");
			assertTrue(slot.description().contains("Slot 1"),
				"Should have 'Slot 1' in description");
		}
	}

	// ============================================================
	// Compatibility Filtering Tests
	// ============================================================

	@Nested
	@DisplayName("Compatibility Filtering")
	class CompatibilityFilteringTests {

		@Test
		@DisplayName("compatibleWith() should filter compatible slots")
		void compatibleWith_FiltersCompatibleSlots() {
			// Create validator that only accepts the gemUpgrade
			SlotValidator gemValidator = SlotValidator.custom(component -> component.equals(gemUpgrade));
			ComponentUpgradeSlot restrictedSlot = createEmptySlotWithValidator(
				"restricted_gem", GEM_TYPE, "Restricted Gem Slot", gemValidator);

			List<ComponentUpgradeSlot> slots = List.of(
				restrictedSlot,
				createEmptySlot("open_slot", GEM_TYPE, "Open Slot")
			);

			SlotQuery<ComponentUpgradeSlot> query = new SlotQueryImpl<>(slots);

			List<ComponentUpgradeSlot> result = query.compatibleWith(gemUpgrade).execute();

			// Both slots should accept the gem (default validator accepts all)
			assertEquals(2, result.size(), "Should find all compatible slots");
		}

		@Test
		@DisplayName("compatibleWith() should exclude incompatible slots")
		void compatibleWith_ExcludesIncompatibleSlots() {
			// Create validator that rejects gemUpgrade
			SlotValidator rejectingValidator = SlotValidator.custom(component -> !component.equals(gemUpgrade));
			ComponentUpgradeSlot restrictedSlot = createEmptySlotWithValidator(
				"restricted", GEM_TYPE, "Restricted Slot", rejectingValidator);

			List<ComponentUpgradeSlot> slots = List.of(restrictedSlot);

			SlotQuery<ComponentUpgradeSlot> query = new SlotQueryImpl<>(slots);

			List<ComponentUpgradeSlot> result = query.compatibleWith(gemUpgrade).execute();

			assertTrue(result.isEmpty(), "Should find no compatible slots");
		}

		@Test
		@DisplayName("compatibleWith() combined with type filter")
		void compatibleWith_CombinedWithTypeFilter() {
			SlotValidator acceptsAll = SlotValidator.ACCEPT_ALL;
			List<ComponentUpgradeSlot> slots = List.of(
				createEmptySlotWithValidator("gem1", GEM_TYPE, "Gem 1", acceptsAll),
				createEmptySlotWithValidator("binding1", BINDING_TYPE, "Binding", acceptsAll)
			);

			SlotQuery<ComponentUpgradeSlot> query = new SlotQueryImpl<>(slots);

			List<ComponentUpgradeSlot> result = query
				.ofType(GEM_TYPE)
				.compatibleWith(gemUpgrade)
				.execute();

			assertEquals(1, result.size(), "Should find only gem type compatible slot");
			assertEquals(GEM_TYPE, result.get(0).slotType(), "Should be gem type");
		}
	}

	// ============================================================
	// Execution Method Tests
	// ============================================================

	@Nested
	@DisplayName("Execution Methods")
	class ExecutionMethodTests {

		@Test
		@DisplayName("execute() should return all matching slots")
		void execute_ReturnsAllMatches() {
			SlotQuery<ComponentUpgradeSlot> query = new SlotQueryImpl<>(testSlots);

			List<ComponentUpgradeSlot> result = query.ofType(GEM_TYPE).execute();

			assertEquals(3, result.size(), "Should return all gem slots");
		}

		@Test
		@DisplayName("execute() should return empty list when no matches")
		void execute_ReturnsEmptyWhenNoMatches() {
			SlotQuery<ComponentUpgradeSlot> query = new SlotQueryImpl<>(testSlots);
			OpenIdentifier nonExistent = OpenIdentifier.parse("forgero:nonexistent");

			List<ComponentUpgradeSlot> result = query.ofType(nonExistent).execute();

			assertTrue(result.isEmpty(), "Should return empty list");
		}

		@Test
		@DisplayName("first() should return first match")
		void first_ReturnsFirstMatch() {
			SlotQuery<ComponentUpgradeSlot> query = new SlotQueryImpl<>(testSlots);

			Optional<ComponentUpgradeSlot> result = query.ofType(GEM_TYPE).first();

			assertTrue(result.isPresent(), "Should find first gem slot");
			assertEquals(GEM_TYPE, result.get().slotType(), "Should be gem type");
		}

		@Test
		@DisplayName("first() should return empty when no matches")
		void first_ReturnsEmptyWhenNoMatches() {
			SlotQuery<ComponentUpgradeSlot> query = new SlotQueryImpl<>(testSlots);
			OpenIdentifier nonExistent = OpenIdentifier.parse("forgero:nonexistent");

			Optional<ComponentUpgradeSlot> result = query.ofType(nonExistent).first();

			assertTrue(result.isEmpty(), "Should return empty");
		}

		@Test
		@DisplayName("exists() should return true when matches exist")
		void exists_ReturnsTrueWhenMatchesExist() {
			SlotQuery<ComponentUpgradeSlot> query = new SlotQueryImpl<>(testSlots);

			boolean result = query.ofType(GEM_TYPE).exists();

			assertTrue(result, "Should return true for existing type");
		}

		@Test
		@DisplayName("exists() should return false when no matches")
		void exists_ReturnsFalseWhenNoMatches() {
			SlotQuery<ComponentUpgradeSlot> query = new SlotQueryImpl<>(testSlots);
			OpenIdentifier nonExistent = OpenIdentifier.parse("forgero:nonexistent");

			boolean result = query.ofType(nonExistent).exists();

			assertFalse(result, "Should return false for non-existent type");
		}

		@Test
		@DisplayName("count() should return number of matches")
		void count_ReturnsNumberOfMatches() {
			SlotQuery<ComponentUpgradeSlot> query = new SlotQueryImpl<>(testSlots);

			long result = query.ofType(GEM_TYPE).count();

			assertEquals(3, result, "Should count all gem slots");
		}

		@Test
		@DisplayName("count() should return 0 when no matches")
		void count_ReturnsZeroWhenNoMatches() {
			SlotQuery<ComponentUpgradeSlot> query = new SlotQueryImpl<>(testSlots);
			OpenIdentifier nonExistent = OpenIdentifier.parse("forgero:nonexistent");

			long result = query.ofType(nonExistent).count();

			assertEquals(0, result, "Should return 0 for non-existent type");
		}
	}

	// ============================================================
	// Chained Filter Tests
	// ============================================================

	@Nested
	@DisplayName("Chained Filters")
	class ChainedFilterTests {

		@Test
		@DisplayName("should apply multiple filters in sequence")
		void multipleFilters_ApplyInSequence() {
			SlotQuery<ComponentUpgradeSlot> query = new SlotQueryImpl<>(testSlots);

			List<ComponentUpgradeSlot> result = query
				.ofType(GEM_TYPE)
				.onlyEmpty()
				.matching(slot -> slot.description().contains("Slot 1"))
				.execute();

			assertEquals(1, result.size(), "Should find exactly one slot");
			ComponentUpgradeSlot slot = result.get(0);
			assertEquals(GEM_TYPE, slot.slotType(), "Should be gem type");
			assertTrue(slot.isEmpty(), "Should be empty");
			assertTrue(slot.description().contains("Slot 1"),
				"Should have 'Slot 1' in description");
		}

		@Test
		@DisplayName("should handle complex filter chains")
		void complexFilterChain_WorksCorrectly() {
			SlotQuery<ComponentUpgradeSlot> query = new SlotQueryImpl<>(testSlots);

			List<ComponentUpgradeSlot> result = query
				.ofAnyType(GEM_TYPE, BINDING_TYPE)
				.onlyEmpty()
				.matching(slot -> !slot.description().contains("Offensive"))
				.execute();

			assertTrue(result.size() >= 2, "Should find multiple slots");
			assertTrue(result.stream().allMatch(s ->
					(s.slotType().equals(GEM_TYPE) || s.slotType().equals(BINDING_TYPE)) &&
					s.isEmpty() &&
					!s.description().contains("Offensive")),
				"All results should match all filter criteria");
		}

		@Test
		@DisplayName("order of filters should not matter for final result")
		void filterOrder_DoesNotMatterForResult() {
			SlotQuery<ComponentUpgradeSlot> query1 = new SlotQueryImpl<>(testSlots);
			SlotQuery<ComponentUpgradeSlot> query2 = new SlotQueryImpl<>(testSlots);

			List<ComponentUpgradeSlot> result1 = query1
				.ofType(GEM_TYPE)
				.onlyEmpty()
				.execute();

			List<ComponentUpgradeSlot> result2 = query2
				.onlyEmpty()
				.ofType(GEM_TYPE)
				.execute();

			assertEquals(result1.size(), result2.size(),
				"Both filter orders should return same count");
			// Note: Order of results may differ, but set should be equal
			assertTrue(result1.containsAll(result2) && result2.containsAll(result1),
				"Both filter orders should return same slots");
		}
	}

	// ============================================================
	// Edge Case Tests
	// ============================================================

	@Nested
	@DisplayName("Edge Cases")
	class EdgeCaseTests {

		@Test
		@DisplayName("should handle empty slot list")
		void emptySlotList_HandlesGracefully() {
			SlotQuery<ComponentUpgradeSlot> query = new SlotQueryImpl<>(List.of());

			List<ComponentUpgradeSlot> result = query.ofType(GEM_TYPE).execute();

			assertTrue(result.isEmpty(), "Should return empty list");
		}

		@Test
		@DisplayName("should handle query with no filters")
		void noFilters_ReturnsAllSlots() {
			SlotQuery<ComponentUpgradeSlot> query = new SlotQueryImpl<>(testSlots);

			List<ComponentUpgradeSlot> result = query.execute();

			assertEquals(testSlots.size(), result.size(),
				"Should return all slots when no filters applied");
		}

		@Test
		@DisplayName("should handle consecutive execution calls")
		void consecutiveExecutions_ReturnSameResult() {
			SlotQuery<ComponentUpgradeSlot> query = new SlotQueryImpl<>(testSlots)
				.ofType(GEM_TYPE)
				.onlyEmpty();

			List<ComponentUpgradeSlot> result1 = query.execute();
			List<ComponentUpgradeSlot> result2 = query.execute();

			assertEquals(result1, result2, "Consecutive executions should return same result");
		}
	}

	// ============================================================
	// Helper Methods
	// ============================================================

	private Component createTestUpgrade(String id) {
		OpenIdentifier upgradeId = OpenIdentifier.parse("forgero:" + id);
		return new TestComponent(upgradeId);
	}

	private ComponentUpgradeSlot createEmptySlot(String id, OpenIdentifier type, String description) {
		return ComponentUpgradeSlot.emptyWithValidator(
			OpenIdentifier.of(id),
			type,
			description,
			SlotValidator.ACCEPT_ALL
		);
	}

	private ComponentUpgradeSlot createFilledSlot(String id, OpenIdentifier type,
	                                     String description, Component content) {
		return ComponentUpgradeSlot.filled(
			OpenIdentifier.of(id),
			type,
			description,
			SlotValidator.ACCEPT_ALL,
			content
		);
	}

	private ComponentUpgradeSlot createEmptySlotWithValidator(String id, OpenIdentifier type,
	                                                 String description, SlotValidator validator) {
		return ComponentUpgradeSlot.emptyWithValidator(
			OpenIdentifier.of(id),
			type,
			description,
			validator
		);
	}

	// Simple test component implementation
	private record TestComponent(OpenIdentifier id) implements Component {
		@Override
		public Set<OpenIdentifier> getTags() {
			return Set.of();
		}

		@Override
		public Map<String, List<?>> propertiesAsMap() {
			return Map.of();
		}

		@Override
		public OpenIdentifier getTypeIdentifier() {
			return OpenIdentifier.parse("forgero:test_component");
		}

		@Override
		public Component withProperties(Map<String, List<?>> newProperties) {
			return this; // Immutable for testing
		}
	}
}
