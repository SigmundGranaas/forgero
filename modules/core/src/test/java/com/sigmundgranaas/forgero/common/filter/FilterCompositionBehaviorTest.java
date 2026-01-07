package com.sigmundgranaas.forgero.common.filter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for filter composition behaviors.
 * <p>
 * Filters are the core mechanism for selecting targets in Forgero's effect system.
 * This tests the composition utilities (AND, OR, NOT) that allow complex targeting logic.
 */
@DisplayName("Filter Composition Behavior")
class FilterCompositionBehaviorTest {

	// Test filters for composition
	private static final Filter<Integer, Void> IS_POSITIVE = (ctx, n) -> n > 0;
	private static final Filter<Integer, Void> IS_EVEN = (ctx, n) -> n % 2 == 0;
	private static final Filter<Integer, Void> IS_LESS_THAN_10 = (ctx, n) -> n < 10;
	private static final Filter<Integer, Void> ALWAYS_TRUE = Filter.alwaysTrue();
	private static final Filter<Integer, Void> ALWAYS_FALSE = Filter.alwaysFalse();

	@Nested
	@DisplayName("AND Filter Composition (Filters.all)")
	class AndFilterComposition {

		@Test
		@DisplayName("passes when all filters pass")
		void passesWhenAllFiltersPass() {
			Filter<Integer, Void> combined = Filters.all(IS_POSITIVE, IS_EVEN, IS_LESS_THAN_10);

			assertTrue(combined.test(null, 4)); // positive, even, < 10
			assertTrue(combined.test(null, 2));
			assertTrue(combined.test(null, 8));
		}

		@Test
		@DisplayName("fails when any filter fails")
		void failsWhenAnyFilterFails() {
			Filter<Integer, Void> combined = Filters.all(IS_POSITIVE, IS_EVEN, IS_LESS_THAN_10);

			assertFalse(combined.test(null, -2)); // negative (fails IS_POSITIVE)
			assertFalse(combined.test(null, 3));  // odd (fails IS_EVEN)
			assertFalse(combined.test(null, 12)); // >= 10 (fails IS_LESS_THAN_10)
		}

		@Test
		@DisplayName("empty collection returns alwaysTrue filter")
		void emptyCollectionReturnsAlwaysTrueFilter() {
			Filter<Integer, Void> combined = Filters.all(List.of());

			assertTrue(combined.test(null, 0));
			assertTrue(combined.test(null, -100));
		}

		@Test
		@DisplayName("single filter returns that filter directly")
		void singleFilterReturnsThatFilterDirectly() {
			Filter<Integer, Void> combined = Filters.all(List.of(IS_POSITIVE));

			assertTrue(combined.test(null, 1));
			assertFalse(combined.test(null, -1));
			// Should be the same filter instance for efficiency
			assertSame(IS_POSITIVE, combined);
		}

		@Test
		@DisplayName("short-circuits on first failure")
		void shortCircuitsOnFirstFailure() {
			int[] callCount = {0};
			Filter<Integer, Void> counting = (ctx, n) -> {
				callCount[0]++;
				return n > 5;
			};

			Filter<Integer, Void> combined = Filters.all(ALWAYS_FALSE, counting);
			combined.test(null, 10);

			assertEquals(0, callCount[0], "Second filter should not be called after first fails");
		}
	}

	@Nested
	@DisplayName("OR Filter Composition (Filters.any)")
	class OrFilterComposition {

		@Test
		@DisplayName("passes when any filter passes")
		void passesWhenAnyFilterPasses() {
			Filter<Integer, Void> combined = Filters.any(IS_POSITIVE, IS_EVEN);

			assertTrue(combined.test(null, 5));  // positive only
			assertTrue(combined.test(null, -2)); // even only
			assertTrue(combined.test(null, 4));  // both
		}

		@Test
		@DisplayName("fails when all filters fail")
		void failsWhenAllFiltersFail() {
			Filter<Integer, Void> combined = Filters.any(IS_POSITIVE, IS_EVEN);

			assertFalse(combined.test(null, -3)); // negative and odd
			assertFalse(combined.test(null, -1));
		}

		@Test
		@DisplayName("empty collection returns alwaysFalse filter")
		void emptyCollectionReturnsAlwaysFalseFilter() {
			Filter<Integer, Void> combined = Filters.any(List.of());

			assertFalse(combined.test(null, 0));
			assertFalse(combined.test(null, 100));
		}

		@Test
		@DisplayName("single filter returns that filter directly")
		void singleFilterReturnsThatFilterDirectly() {
			Filter<Integer, Void> combined = Filters.any(List.of(IS_EVEN));

			assertTrue(combined.test(null, 2));
			assertFalse(combined.test(null, 3));
			assertSame(IS_EVEN, combined);
		}

		@Test
		@DisplayName("short-circuits on first success")
		void shortCircuitsOnFirstSuccess() {
			int[] callCount = {0};
			Filter<Integer, Void> counting = (ctx, n) -> {
				callCount[0]++;
				return n > 5;
			};

			Filter<Integer, Void> combined = Filters.any(ALWAYS_TRUE, counting);
			combined.test(null, 10);

			assertEquals(0, callCount[0], "Second filter should not be called after first succeeds");
		}
	}

	@Nested
	@DisplayName("NOT Filter Composition (Filters.not)")
	class NotFilterComposition {

		@Test
		@DisplayName("inverts passing filter to failing")
		void invertsPassingFilterToFailing() {
			Filter<Integer, Void> notPositive = Filters.not(IS_POSITIVE);

			assertFalse(notPositive.test(null, 5));
			assertFalse(notPositive.test(null, 1));
		}

		@Test
		@DisplayName("inverts failing filter to passing")
		void invertsFailingFilterToPassing() {
			Filter<Integer, Void> notPositive = Filters.not(IS_POSITIVE);

			assertTrue(notPositive.test(null, -5));
			assertTrue(notPositive.test(null, 0));
		}

		@Test
		@DisplayName("double negation returns original result")
		void doubleNegationReturnsOriginalResult() {
			Filter<Integer, Void> doubleNot = Filters.not(Filters.not(IS_POSITIVE));

			assertTrue(doubleNot.test(null, 5));
			assertFalse(doubleNot.test(null, -5));
		}
	}

	@Nested
	@DisplayName("Predicate Wrapping (Filters.fromPredicate)")
	class PredicateWrapping {

		@Test
		@DisplayName("wraps predicate ignoring context")
		void wrapsPredicateIgnoringContext() {
			Filter<String, Integer> filter = Filters.fromPredicate(s -> s.startsWith("test"));

			assertTrue(filter.test(999, "test_value")); // context ignored
			assertTrue(filter.test(null, "testing"));
			assertFalse(filter.test(0, "other"));
		}

		@Test
		@DisplayName("works with any target type")
		void worksWithAnyTargetType() {
			Filter<Double, Void> isFinite = Filters.fromPredicate(Double::isFinite);

			assertTrue(isFinite.test(null, 3.14));
			assertFalse(isFinite.test(null, Double.POSITIVE_INFINITY));
			assertFalse(isFinite.test(null, Double.NaN));
		}
	}

	@Nested
	@DisplayName("Context-Based Filtering (Filters.fromContext)")
	class ContextBasedFiltering {

		@Test
		@DisplayName("wraps predicate using only context")
		void wrapsPredicateUsingOnlyContext() {
			Filter<String, Integer> contextFilter = Filters.fromContext(ctx -> ctx > 0);

			assertTrue(contextFilter.test(5, "anything"));  // context > 0
			assertFalse(contextFilter.test(-1, "anything")); // context <= 0
		}

		@Test
		@DisplayName("target is ignored completely")
		void targetIsIgnoredCompletely() {
			Filter<Object, Boolean> isEnabled = Filters.fromContext(ctx -> ctx);

			assertTrue(isEnabled.test(true, null));
			assertTrue(isEnabled.test(true, "ignored"));
			assertFalse(isEnabled.test(false, "also ignored"));
		}
	}

	@Nested
	@DisplayName("Batch Testing (testAll/testAny)")
	class BatchTesting {

		@Test
		@DisplayName("testAll returns true when all filters pass")
		void testAllReturnsTrueWhenAllFiltersPass() {
			List<Filter<Integer, Void>> filters = List.of(IS_POSITIVE, IS_EVEN, IS_LESS_THAN_10);

			assertTrue(Filters.testAll(filters, null, 4));
		}

		@Test
		@DisplayName("testAll returns false when any filter fails")
		void testAllReturnsFalseWhenAnyFilterFails() {
			List<Filter<Integer, Void>> filters = List.of(IS_POSITIVE, IS_EVEN);

			assertFalse(Filters.testAll(filters, null, 3)); // odd fails
		}

		@Test
		@DisplayName("testAll returns true for empty list")
		void testAllReturnsTrueForEmptyList() {
			assertTrue(Filters.testAll(List.of(), null, 999));
		}

		@Test
		@DisplayName("testAny returns true when any filter passes")
		void testAnyReturnsTrueWhenAnyFilterPasses() {
			List<Filter<Integer, Void>> filters = List.of(IS_POSITIVE, IS_EVEN);

			assertTrue(Filters.testAny(filters, null, -2)); // even passes
		}

		@Test
		@DisplayName("testAny returns false when all filters fail")
		void testAnyReturnsFalseWhenAllFiltersFail() {
			List<Filter<Integer, Void>> filters = List.of(IS_POSITIVE, IS_EVEN);

			assertFalse(Filters.testAny(filters, null, -3)); // negative and odd
		}

		@Test
		@DisplayName("testAny returns false for empty list")
		void testAnyReturnsFalseForEmptyList() {
			assertFalse(Filters.testAny(List.of(), null, 999));
		}
	}

	@Nested
	@DisplayName("Filter Interface Default Methods")
	class FilterInterfaceDefaultMethods {

		@Test
		@DisplayName("negate inverts result")
		void negateInvertsResult() {
			Filter<Integer, Void> notPositive = IS_POSITIVE.negate();

			assertTrue(notPositive.test(null, -5));
			assertFalse(notPositive.test(null, 5));
		}

		@Test
		@DisplayName("and combines two filters")
		void andCombinesTwoFilters() {
			Filter<Integer, Void> positiveAndEven = IS_POSITIVE.and(IS_EVEN);

			assertTrue(positiveAndEven.test(null, 4));
			assertFalse(positiveAndEven.test(null, 3));  // not even
			assertFalse(positiveAndEven.test(null, -2)); // not positive
		}

		@Test
		@DisplayName("or combines two filters")
		void orCombinesTwoFilters() {
			Filter<Integer, Void> positiveOrEven = IS_POSITIVE.or(IS_EVEN);

			assertTrue(positiveOrEven.test(null, 5));  // positive only
			assertTrue(positiveOrEven.test(null, -2)); // even only
			assertFalse(positiveOrEven.test(null, -3)); // neither
		}

		@Test
		@DisplayName("alwaysTrue always returns true")
		void alwaysTrueAlwaysReturnsTrue() {
			Filter<Object, Object> f = Filter.alwaysTrue();

			assertTrue(f.test(null, null));
			assertTrue(f.test("context", "target"));
			assertTrue(f.test(1, 2));
		}

		@Test
		@DisplayName("alwaysFalse always returns false")
		void alwaysFalseAlwaysReturnsFalse() {
			Filter<Object, Object> f = Filter.alwaysFalse();

			assertFalse(f.test(null, null));
			assertFalse(f.test("context", "target"));
			assertFalse(f.test(1, 2));
		}
	}

	@Nested
	@DisplayName("AndFilter Record")
	class AndFilterRecord {

		@Test
		@DisplayName("of varargs factory creates AndFilter")
		void ofVarargsFactoryCreatesAndFilter() {
			AndFilter<Integer, Void> filter = AndFilter.of(IS_POSITIVE, IS_EVEN);

			assertTrue(filter.test(null, 4));
			assertFalse(filter.test(null, 3));
		}

		@Test
		@DisplayName("empty factory creates always-true filter")
		void emptyFactoryCreatesAlwaysTrueFilter() {
			AndFilter<Integer, Void> filter = AndFilter.empty();

			assertTrue(filter.test(null, 0));
			assertTrue(filter.test(null, -100));
		}

		@Test
		@DisplayName("filters list is immutable")
		void filtersListIsImmutable() {
			AndFilter<Integer, Void> filter = AndFilter.of(IS_POSITIVE);

			assertThrows(UnsupportedOperationException.class, () -> {
				filter.filters().add(IS_EVEN);
			});
		}
	}

	@Nested
	@DisplayName("OrFilter Record")
	class OrFilterRecord {

		@Test
		@DisplayName("of varargs factory creates OrFilter")
		void ofVarargsFactoryCreatesOrFilter() {
			OrFilter<Integer, Void> filter = OrFilter.of(IS_POSITIVE, IS_EVEN);

			assertTrue(filter.test(null, 5));  // positive only
			assertTrue(filter.test(null, -2)); // even only
			assertFalse(filter.test(null, -3)); // neither
		}

		@Test
		@DisplayName("empty factory creates always-false filter")
		void emptyFactoryCreatesAlwaysFalseFilter() {
			OrFilter<Integer, Void> filter = OrFilter.empty();

			assertFalse(filter.test(null, 0));
			assertFalse(filter.test(null, 100));
		}

		@Test
		@DisplayName("filters list is immutable")
		void filtersListIsImmutable() {
			OrFilter<Integer, Void> filter = OrFilter.of(IS_POSITIVE);

			assertThrows(UnsupportedOperationException.class, () -> {
				filter.filters().add(IS_EVEN);
			});
		}
	}

	@Nested
	@DisplayName("NotFilter Record")
	class NotFilterRecord {

		@Test
		@DisplayName("of factory creates NotFilter")
		void ofFactoryCreatesNotFilter() {
			NotFilter<Integer, Void> filter = NotFilter.of(IS_POSITIVE);

			assertTrue(filter.test(null, -5));
			assertFalse(filter.test(null, 5));
		}

		@Test
		@DisplayName("filters returns single-element list")
		void filtersReturnsSingleElementList() {
			NotFilter<Integer, Void> filter = NotFilter.of(IS_POSITIVE);

			assertEquals(1, filter.filters().size());
			assertSame(IS_POSITIVE, filter.filters().get(0));
		}
	}

	@Nested
	@DisplayName("Complex Composition")
	class ComplexComposition {

		@Test
		@DisplayName("nested AND and OR work correctly")
		void nestedAndAndOrWorkCorrectly() {
			// (positive AND even) OR lessThan10
			Filter<Integer, Void> complex = Filters.any(
					Filters.all(IS_POSITIVE, IS_EVEN),
					IS_LESS_THAN_10
			);

			assertTrue(complex.test(null, 4));   // positive AND even
			assertTrue(complex.test(null, 12));  // positive AND even, but not < 10
			assertTrue(complex.test(null, -5));  // < 10 but not positive
			assertFalse(complex.test(null, 15)); // positive but odd, >= 10
		}

		@Test
		@DisplayName("NOT combined with AND")
		void notCombinedWithAnd() {
			// positive AND NOT even (positive odd numbers)
			Filter<Integer, Void> positiveOdd = Filters.all(IS_POSITIVE, Filters.not(IS_EVEN));

			assertTrue(positiveOdd.test(null, 3));
			assertTrue(positiveOdd.test(null, 7));
			assertFalse(positiveOdd.test(null, 4)); // even
			assertFalse(positiveOdd.test(null, -3)); // negative
		}
	}
}
