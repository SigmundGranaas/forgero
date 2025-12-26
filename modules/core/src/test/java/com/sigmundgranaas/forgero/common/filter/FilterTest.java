package com.sigmundgranaas.forgero.common.filter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the unified Filter system.
 */
class FilterTest {

	@Nested
	@DisplayName("Base Filter interface")
	class BaseFilterTests {

		@Test
		@DisplayName("alwaysTrue filter should always return true")
		void alwaysTrueReturnsTrue() {
			Filter<String, Void> filter = Filter.alwaysTrue();

			assertTrue(filter.test(null, "test"));
			assertTrue(filter.test(null, ""));
			assertTrue(filter.test(null, null));
		}

		@Test
		@DisplayName("alwaysFalse filter should always return false")
		void alwaysFalseReturnsFalse() {
			Filter<String, Void> filter = Filter.alwaysFalse();

			assertFalse(filter.test(null, "test"));
			assertFalse(filter.test(null, ""));
			assertFalse(filter.test(null, null));
		}

		@Test
		@DisplayName("negate should invert filter result")
		void negateInvertsResult() {
			Filter<Integer, Void> isPositive = (ctx, n) -> n > 0;
			Filter<Integer, Void> isNotPositive = isPositive.negate();

			assertTrue(isPositive.test(null, 5));
			assertFalse(isNotPositive.test(null, 5));

			assertFalse(isPositive.test(null, -5));
			assertTrue(isNotPositive.test(null, -5));
		}

		@Test
		@DisplayName("and should combine filters with AND logic")
		void andCombinesFilters() {
			Filter<Integer, Void> isPositive = (ctx, n) -> n > 0;
			Filter<Integer, Void> isEven = (ctx, n) -> n % 2 == 0;
			Filter<Integer, Void> isPositiveAndEven = isPositive.and(isEven);

			assertTrue(isPositiveAndEven.test(null, 4));  // positive and even
			assertFalse(isPositiveAndEven.test(null, 3)); // positive but odd
			assertFalse(isPositiveAndEven.test(null, -4)); // even but negative
			assertFalse(isPositiveAndEven.test(null, -3)); // neither
		}

		@Test
		@DisplayName("or should combine filters with OR logic")
		void orCombinesFilters() {
			Filter<Integer, Void> isPositive = (ctx, n) -> n > 0;
			Filter<Integer, Void> isEven = (ctx, n) -> n % 2 == 0;
			Filter<Integer, Void> isPositiveOrEven = isPositive.or(isEven);

			assertTrue(isPositiveOrEven.test(null, 4));  // positive and even
			assertTrue(isPositiveOrEven.test(null, 3));  // positive but odd
			assertTrue(isPositiveOrEven.test(null, -4)); // even but negative
			assertFalse(isPositiveOrEven.test(null, -3)); // neither
		}
	}

	@Nested
	@DisplayName("AndFilter")
	class AndFilterTests {

		@Test
		@DisplayName("empty AndFilter should always pass")
		void emptyAndFilterPasses() {
			AndFilter<String, Void> filter = AndFilter.empty();
			assertTrue(filter.test(null, "anything"));
		}

		@Test
		@DisplayName("AndFilter should require all filters to pass")
		void andRequiresAll() {
			Filter<Integer, Void> isPositive = (ctx, n) -> n > 0;
			Filter<Integer, Void> isLessThan10 = (ctx, n) -> n < 10;
			Filter<Integer, Void> isEven = (ctx, n) -> n % 2 == 0;

			AndFilter<Integer, Void> combined = AndFilter.of(isPositive, isLessThan10, isEven);

			assertTrue(combined.test(null, 4));   // passes all
			assertFalse(combined.test(null, 12)); // fails isLessThan10
			assertFalse(combined.test(null, 3));  // fails isEven
			assertFalse(combined.test(null, -2)); // fails isPositive
		}

		@Test
		@DisplayName("AndFilter should short-circuit on first failure")
		void andShortCircuits() {
			int[] counter = {0};
			Filter<Integer, Void> incrementCounter = (ctx, n) -> {
				counter[0]++;
				return true;
			};
			Filter<Integer, Void> alwaysFail = (ctx, n) -> false;

			AndFilter<Integer, Void> combined = AndFilter.of(alwaysFail, incrementCounter);
			combined.test(null, 1);

			assertEquals(0, counter[0], "Second filter should not be called after first fails");
		}
	}

	@Nested
	@DisplayName("OrFilter")
	class OrFilterTests {

		@Test
		@DisplayName("empty OrFilter should always fail")
		void emptyOrFilterFails() {
			OrFilter<String, Void> filter = OrFilter.empty();
			assertFalse(filter.test(null, "anything"));
		}

		@Test
		@DisplayName("OrFilter should pass if any filter passes")
		void orRequiresAny() {
			Filter<Integer, Void> isNegative = (ctx, n) -> n < 0;
			Filter<Integer, Void> isZero = (ctx, n) -> n == 0;
			Filter<Integer, Void> isGreaterThan100 = (ctx, n) -> n > 100;

			OrFilter<Integer, Void> combined = OrFilter.of(isNegative, isZero, isGreaterThan100);

			assertTrue(combined.test(null, -5));   // passes isNegative
			assertTrue(combined.test(null, 0));    // passes isZero
			assertTrue(combined.test(null, 101));  // passes isGreaterThan100
			assertFalse(combined.test(null, 50));  // fails all
		}

		@Test
		@DisplayName("OrFilter should short-circuit on first success")
		void orShortCircuits() {
			int[] counter = {0};
			Filter<Integer, Void> incrementCounter = (ctx, n) -> {
				counter[0]++;
				return true;
			};
			Filter<Integer, Void> alwaysPass = (ctx, n) -> true;

			OrFilter<Integer, Void> combined = OrFilter.of(alwaysPass, incrementCounter);
			combined.test(null, 1);

			assertEquals(0, counter[0], "Second filter should not be called after first passes");
		}
	}

	@Nested
	@DisplayName("NotFilter")
	class NotFilterTests {

		@Test
		@DisplayName("NotFilter should negate the result")
		void notNegatesResult() {
			Filter<String, Void> isEmpty = (ctx, s) -> s.isEmpty();
			NotFilter<String, Void> isNotEmpty = NotFilter.of(isEmpty);

			assertTrue(isEmpty.test(null, ""));
			assertFalse(isNotEmpty.test(null, ""));

			assertFalse(isEmpty.test(null, "hello"));
			assertTrue(isNotEmpty.test(null, "hello"));
		}

		@Test
		@DisplayName("NotFilter.filters() should return the wrapped filter")
		void filtersReturnsWrappedFilter() {
			Filter<String, Void> original = (ctx, s) -> s.isEmpty();
			NotFilter<String, Void> notFilter = NotFilter.of(original);

			assertEquals(1, notFilter.filters().size());
			assertSame(original, notFilter.filters().get(0));
		}
	}

	@Nested
	@DisplayName("Filters utility class")
	class FiltersUtilityTests {

		@Test
		@DisplayName("Filters.all with empty list should pass")
		void allWithEmptyListPasses() {
			Filter<String, Void> filter = Filters.all(List.of());
			assertTrue(filter.test(null, "anything"));
		}

		@Test
		@DisplayName("Filters.all with single filter should return that filter")
		void allWithSingleFilter() {
			Filter<Integer, Void> original = (ctx, n) -> n > 0;
			Filter<Integer, Void> result = Filters.all(List.of(original));

			assertSame(original, result);
		}

		@Test
		@DisplayName("Filters.any with empty list should fail")
		void anyWithEmptyListFails() {
			Filter<String, Void> filter = Filters.any(List.of());
			assertFalse(filter.test(null, "anything"));
		}

		@Test
		@DisplayName("Filters.fromPredicate should wrap predicate")
		void fromPredicateWrapsPredicate() {
			Filter<String, Integer> filter = Filters.fromPredicate(s -> s.length() > 3);

			assertTrue(filter.test(999, "hello")); // context ignored
			assertFalse(filter.test(999, "hi"));   // context ignored
		}

		@Test
		@DisplayName("Filters.fromContext should use context only")
		void fromContextUsesContext() {
			Filter<String, Integer> filter = Filters.fromContext(ctx -> ctx > 0);

			assertTrue(filter.test(5, "ignored"));  // target ignored
			assertFalse(filter.test(-5, "ignored")); // target ignored
		}

		@Test
		@DisplayName("Filters.testAll should test all filters")
		void testAllTestsAllFilters() {
			List<Filter<Integer, Void>> filters = List.of(
					(ctx, n) -> n > 0,
					(ctx, n) -> n < 100,
					(ctx, n) -> n % 2 == 0
			);

			assertTrue(Filters.testAll(filters, null, 50));
			assertFalse(Filters.testAll(filters, null, 51)); // fails even check
		}

		@Test
		@DisplayName("Filters.testAny should test any filter")
		void testAnyTestsAnyFilter() {
			List<Filter<Integer, Void>> filters = List.of(
					(ctx, n) -> n < 0,
					(ctx, n) -> n > 100
			);

			assertTrue(Filters.testAny(filters, null, -5));
			assertTrue(Filters.testAny(filters, null, 150));
			assertFalse(Filters.testAny(filters, null, 50));
		}
	}

	@Nested
	@DisplayName("Filter with context")
	class FilterWithContextTests {

		@Test
		@DisplayName("Filter should receive and use context")
		void filterUsesContext() {
			// Filter that checks if target is greater than context
			Filter<Integer, Integer> isGreaterThanContext = (ctx, target) -> target > ctx;

			assertTrue(isGreaterThanContext.test(5, 10));  // 10 > 5
			assertFalse(isGreaterThanContext.test(10, 5)); // 5 > 10
			assertFalse(isGreaterThanContext.test(5, 5));  // 5 > 5
		}

		@Test
		@DisplayName("Composite filters should pass context to all children")
		void compositeFiltersPassContext() {
			Filter<Integer, Integer> targetGtContext = (ctx, target) -> target > ctx;
			Filter<Integer, Integer> targetLt100 = (ctx, target) -> target < 100;

			AndFilter<Integer, Integer> combined = AndFilter.of(targetGtContext, targetLt100);

			assertTrue(combined.test(5, 50));   // 50 > 5 && 50 < 100
			assertFalse(combined.test(60, 50)); // 50 > 60 is false
			assertFalse(combined.test(5, 150)); // 150 < 100 is false
		}
	}
}
