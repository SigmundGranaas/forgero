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
	private static final OpenIdentifier GEM_TYPE = OpenIdentifier.of("forgero:gem");
	private static final OpenIdentifier BINDING_TYPE = OpenIdentifier.of("forgero:binding");
	private static final OpenIdentifier HANDLE_TYPE = OpenIdentifier.of("forgero:handle");
	private static final OpenIdentifier OFFENSIVE_GEM_TYPE = OpenIdentifier.of("forgero:offensive_gem_slot");

	// Test components
	private Component gemUpgrade;
	private Component bindingUpgrade;

	// Test slots
	private List<UpgradeSlot> testSlots;

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
			SlotQuery<UpgradeSlot> query = new SlotQueryImpl<>(testSlots);

			List<UpgradeSlot> result = query.ofType(GEM_TYPE).execute();

			assertEquals(3, result.size(), "Should find all gem slots");
			assertTrue(result.stream().allMatch(s -> s.type().equals(GEM_TYPE)),
				"All results should be gem type");
		}

		@Test
		@DisplayName("ofType() should return empty list for non-existent type")
		void ofType_ReturnsEmptyForNonExistentType() {
			SlotQuery<UpgradeSlot> query = new SlotQueryImpl<>(testSlots);
			OpenIdentifier nonExistent = OpenIdentifier.of("forgero:nonexistent");

			List<UpgradeSlot> result = query.ofType(nonExistent).execute();

			assertTrue(result.isEmpty(), "Should return empty list for non-existent type");
		}

		@Test
		@DisplayName("ofAnyType() should filter by multiple types")
		void ofAnyType_FiltersByMultipleTypes() {
			SlotQuery<UpgradeSlot> query = new SlotQueryImpl<>(testSlots);

			List<UpgradeSlot> result = query.ofAnyType(GEM_TYPE, BINDING_TYPE).execute();

			assertEquals(5, result.size(), "Should find all gem and binding slots");
			assertTrue(result.stream().allMatch(s ->
					s.type().equals(GEM_TYPE) || s.type().equals(BINDING_TYPE)),
				"All results should be gem or binding type");
		}

		@Test
		@DisplayName("ofAnyType() with single type should behave like ofType()")
		void ofAnyType_SingleType_BehavesLikeOfType() {
			SlotQuery<UpgradeSlot> query = new SlotQueryImpl<>(testSlots);

			List<UpgradeSlot> result = query.ofAnyType(HANDLE_TYPE).execute();

			assertEquals(1, result.size(), "Should find handle slot");
			assertEquals(HANDLE_TYPE, result.get(0).type(), "Should be handle type");
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
			SlotQuery<UpgradeSlot> query = new SlotQueryImpl<>(testSlots);

			List<UpgradeSlot> result = query.onlyEmpty().execute();

			assertEquals(5, result.size(), "Should find all empty slots");
			assertTrue(result.stream().allMatch(UpgradeSlot::isEmpty),
				"All results should be empty");
		}

		@Test
		@DisplayName("onlyFilled() should filter filled slots")
		void onlyFilled_FiltersFilledSlots() {
			SlotQuery<UpgradeSlot> query = new SlotQueryImpl<>(testSlots);

			List<UpgradeSlot> result = query.onlyFilled().execute();

			assertEquals(2, result.size(), "Should find all filled slots");
			assertTrue(result.stream().noneMatch(UpgradeSlot::isEmpty),
				"All results should be filled");
		}

		@Test
		@DisplayName("onlyEmpty() combined with type filter")
		void onlyEmpty_CombinedWithTypeFilter() {
			SlotQuery<UpgradeSlot> query = new SlotQueryImpl<>(testSlots);

			List<UpgradeSlot> result = query
				.ofType(GEM_TYPE)
				.onlyEmpty()
				.execute();

			assertEquals(2, result.size(), "Should find empty gem slots only");
			assertTrue(result.stream().allMatch(s ->
					s.type().equals(GEM_TYPE) && s.isEmpty()),
				"All results should be empty gem slots");
		}

		@Test
		@DisplayName("onlyFilled() combined with type filter")
		void onlyFilled_CombinedWithTypeFilter() {
			SlotQuery<UpgradeSlot> query = new SlotQueryImpl<>(testSlots);

			List<UpgradeSlot> result = query
				.ofType(BINDING_TYPE)
				.onlyFilled()
				.execute();

			assertEquals(1, result.size(), "Should find filled binding slot");
			UpgradeSlot slot = result.get(0);
			assertEquals(BINDING_TYPE, slot.type(), "Should be binding type");
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
			SlotQuery<UpgradeSlot> query = new SlotQueryImpl<>(testSlots);
			Predicate<UpgradeSlot> descriptionContainsGem = slot ->
				slot.description().toLowerCase().contains("gem");

			List<UpgradeSlot> result = query.matching(descriptionContainsGem).execute();

			assertEquals(4, result.size(), "Should find all slots with 'gem' in description");
			assertTrue(result.stream().allMatch(s ->
					s.description().toLowerCase().contains("gem")),
				"All results should have 'gem' in description");
		}

		@Test
		@DisplayName("matching() should chain with other filters")
		void matching_ChainsWithOtherFilters() {
			SlotQuery<UpgradeSlot> query = new SlotQueryImpl<>(testSlots);
			Predicate<UpgradeSlot> idEndsWithOne = slot ->
				slot.id().toString().endsWith("1");

			List<UpgradeSlot> result = query
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
			SlotQuery<UpgradeSlot> query = new SlotQueryImpl<>(testSlots);
			Predicate<UpgradeSlot> isGemType = slot -> slot.type().equals(GEM_TYPE);
			Predicate<UpgradeSlot> descriptionHasSlot1 = slot ->
				slot.description().contains("Slot 1");

			List<UpgradeSlot> result = query
				.matching(isGemType)
				.matching(descriptionHasSlot1)
				.execute();

			assertEquals(1, result.size(), "Should find exactly one slot matching both");
			UpgradeSlot slot = result.get(0);
			assertEquals(GEM_TYPE, slot.type(), "Should be gem type");
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
			UpgradeSlot restrictedSlot = createEmptySlotWithValidator(
				"restricted_gem", GEM_TYPE, "Restricted Gem Slot", gemValidator);

			List<UpgradeSlot> slots = List.of(
				restrictedSlot,
				createEmptySlot("open_slot", GEM_TYPE, "Open Slot")
			);

			SlotQuery<UpgradeSlot> query = new SlotQueryImpl<>(slots);

			List<UpgradeSlot> result = query.compatibleWith(gemUpgrade).execute();

			// Both slots should accept the gem (default validator accepts all)
			assertEquals(2, result.size(), "Should find all compatible slots");
		}

		@Test
		@DisplayName("compatibleWith() should exclude incompatible slots")
		void compatibleWith_ExcludesIncompatibleSlots() {
			// Create validator that rejects gemUpgrade
			SlotValidator rejectingValidator = SlotValidator.custom(component -> !component.equals(gemUpgrade));
			UpgradeSlot restrictedSlot = createEmptySlotWithValidator(
				"restricted", GEM_TYPE, "Restricted Slot", rejectingValidator);

			List<UpgradeSlot> slots = List.of(restrictedSlot);

			SlotQuery<UpgradeSlot> query = new SlotQueryImpl<>(slots);

			List<UpgradeSlot> result = query.compatibleWith(gemUpgrade).execute();

			assertTrue(result.isEmpty(), "Should find no compatible slots");
		}

		@Test
		@DisplayName("compatibleWith() combined with type filter")
		void compatibleWith_CombinedWithTypeFilter() {
			SlotValidator acceptsAll = SlotValidator.ACCEPT_ALL;
			List<UpgradeSlot> slots = List.of(
				createEmptySlotWithValidator("gem1", GEM_TYPE, "Gem 1", acceptsAll),
				createEmptySlotWithValidator("binding1", BINDING_TYPE, "Binding", acceptsAll)
			);

			SlotQuery<UpgradeSlot> query = new SlotQueryImpl<>(slots);

			List<UpgradeSlot> result = query
				.ofType(GEM_TYPE)
				.compatibleWith(gemUpgrade)
				.execute();

			assertEquals(1, result.size(), "Should find only gem type compatible slot");
			assertEquals(GEM_TYPE, result.get(0).type(), "Should be gem type");
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
			SlotQuery<UpgradeSlot> query = new SlotQueryImpl<>(testSlots);

			List<UpgradeSlot> result = query.ofType(GEM_TYPE).execute();

			assertEquals(3, result.size(), "Should return all gem slots");
		}

		@Test
		@DisplayName("execute() should return empty list when no matches")
		void execute_ReturnsEmptyWhenNoMatches() {
			SlotQuery<UpgradeSlot> query = new SlotQueryImpl<>(testSlots);
			OpenIdentifier nonExistent = OpenIdentifier.of("forgero:nonexistent");

			List<UpgradeSlot> result = query.ofType(nonExistent).execute();

			assertTrue(result.isEmpty(), "Should return empty list");
		}

		@Test
		@DisplayName("first() should return first match")
		void first_ReturnsFirstMatch() {
			SlotQuery<UpgradeSlot> query = new SlotQueryImpl<>(testSlots);

			Optional<UpgradeSlot> result = query.ofType(GEM_TYPE).first();

			assertTrue(result.isPresent(), "Should find first gem slot");
			assertEquals(GEM_TYPE, result.get().type(), "Should be gem type");
		}

		@Test
		@DisplayName("first() should return empty when no matches")
		void first_ReturnsEmptyWhenNoMatches() {
			SlotQuery<UpgradeSlot> query = new SlotQueryImpl<>(testSlots);
			OpenIdentifier nonExistent = OpenIdentifier.of("forgero:nonexistent");

			Optional<UpgradeSlot> result = query.ofType(nonExistent).first();

			assertTrue(result.isEmpty(), "Should return empty");
		}

		@Test
		@DisplayName("exists() should return true when matches exist")
		void exists_ReturnsTrueWhenMatchesExist() {
			SlotQuery<UpgradeSlot> query = new SlotQueryImpl<>(testSlots);

			boolean result = query.ofType(GEM_TYPE).exists();

			assertTrue(result, "Should return true for existing type");
		}

		@Test
		@DisplayName("exists() should return false when no matches")
		void exists_ReturnsFalseWhenNoMatches() {
			SlotQuery<UpgradeSlot> query = new SlotQueryImpl<>(testSlots);
			OpenIdentifier nonExistent = OpenIdentifier.of("forgero:nonexistent");

			boolean result = query.ofType(nonExistent).exists();

			assertFalse(result, "Should return false for non-existent type");
		}

		@Test
		@DisplayName("count() should return number of matches")
		void count_ReturnsNumberOfMatches() {
			SlotQuery<UpgradeSlot> query = new SlotQueryImpl<>(testSlots);

			long result = query.ofType(GEM_TYPE).count();

			assertEquals(3, result, "Should count all gem slots");
		}

		@Test
		@DisplayName("count() should return 0 when no matches")
		void count_ReturnsZeroWhenNoMatches() {
			SlotQuery<UpgradeSlot> query = new SlotQueryImpl<>(testSlots);
			OpenIdentifier nonExistent = OpenIdentifier.of("forgero:nonexistent");

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
			SlotQuery<UpgradeSlot> query = new SlotQueryImpl<>(testSlots);

			List<UpgradeSlot> result = query
				.ofType(GEM_TYPE)
				.onlyEmpty()
				.matching(slot -> slot.description().contains("Slot 1"))
				.execute();

			assertEquals(1, result.size(), "Should find exactly one slot");
			UpgradeSlot slot = result.get(0);
			assertEquals(GEM_TYPE, slot.type(), "Should be gem type");
			assertTrue(slot.isEmpty(), "Should be empty");
			assertTrue(slot.description().contains("Slot 1"),
				"Should have 'Slot 1' in description");
		}

		@Test
		@DisplayName("should handle complex filter chains")
		void complexFilterChain_WorksCorrectly() {
			SlotQuery<UpgradeSlot> query = new SlotQueryImpl<>(testSlots);

			List<UpgradeSlot> result = query
				.ofAnyType(GEM_TYPE, BINDING_TYPE)
				.onlyEmpty()
				.matching(slot -> !slot.description().contains("Offensive"))
				.execute();

			assertTrue(result.size() >= 2, "Should find multiple slots");
			assertTrue(result.stream().allMatch(s ->
					(s.type().equals(GEM_TYPE) || s.type().equals(BINDING_TYPE)) &&
					s.isEmpty() &&
					!s.description().contains("Offensive")),
				"All results should match all filter criteria");
		}

		@Test
		@DisplayName("order of filters should not matter for final result")
		void filterOrder_DoesNotMatterForResult() {
			SlotQuery<UpgradeSlot> query1 = new SlotQueryImpl<>(testSlots);
			SlotQuery<UpgradeSlot> query2 = new SlotQueryImpl<>(testSlots);

			List<UpgradeSlot> result1 = query1
				.ofType(GEM_TYPE)
				.onlyEmpty()
				.execute();

			List<UpgradeSlot> result2 = query2
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
			SlotQuery<UpgradeSlot> query = new SlotQueryImpl<>(List.of());

			List<UpgradeSlot> result = query.ofType(GEM_TYPE).execute();

			assertTrue(result.isEmpty(), "Should return empty list");
		}

		@Test
		@DisplayName("should handle query with no filters")
		void noFilters_ReturnsAllSlots() {
			SlotQuery<UpgradeSlot> query = new SlotQueryImpl<>(testSlots);

			List<UpgradeSlot> result = query.execute();

			assertEquals(testSlots.size(), result.size(),
				"Should return all slots when no filters applied");
		}

		@Test
		@DisplayName("should handle consecutive execution calls")
		void consecutiveExecutions_ReturnSameResult() {
			SlotQuery<UpgradeSlot> query = new SlotQueryImpl<>(testSlots)
				.ofType(GEM_TYPE)
				.onlyEmpty();

			List<UpgradeSlot> result1 = query.execute();
			List<UpgradeSlot> result2 = query.execute();

			assertEquals(result1, result2, "Consecutive executions should return same result");
		}
	}

	// ============================================================
	// Helper Methods
	// ============================================================

	private Component createTestUpgrade(String id) {
		OpenIdentifier upgradeId = OpenIdentifier.of("forgero:" + id);
		return new TestComponent(upgradeId);
	}

	private UpgradeSlot createEmptySlot(String id, OpenIdentifier type, String description) {
		return UpgradeSlot.emptyWithValidator(
			OpenIdentifier.of(id),
			type,
			description,
			SlotValidator.ACCEPT_ALL
		);
	}

	private UpgradeSlot createFilledSlot(String id, OpenIdentifier type,
	                                     String description, Component content) {
		return UpgradeSlot.filled(
			OpenIdentifier.of(id),
			type,
			description,
			SlotValidator.ACCEPT_ALL,
			content
		);
	}

	private UpgradeSlot createEmptySlotWithValidator(String id, OpenIdentifier type,
	                                                 String description, SlotValidator validator) {
		return UpgradeSlot.emptyWithValidator(
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
			return OpenIdentifier.of("forgero:test_component");
		}

		@Override
		public Component withProperties(Map<String, List<?>> newProperties) {
			return this; // Immutable for testing
		}
	}
}
