package com.sigmundgranaas.forgero.common.filter;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

/**
 * Utility class for creating and combining filters.
 * <p>
 * This class provides factory methods and combinators for working with filters.
 */
public final class Filters {

	private Filters() {
		// Utility class
	}

	/**
	 * Creates an AND filter from a collection of filters.
	 *
	 * @param filters The filters to combine
	 * @param <T>     The target type
	 * @param <C>     The context type
	 * @return A filter that passes only if all filters pass
	 */
	public static <T, C> Filter<T, C> all(Collection<Filter<T, C>> filters) {
		if (filters.isEmpty()) {
			return Filter.alwaysTrue();
		}
		if (filters.size() == 1) {
			return filters.iterator().next();
		}
		return new AndFilter<>(List.copyOf(filters));
	}

	/**
	 * Creates an AND filter from varargs.
	 *
	 * @param filters The filters to combine
	 * @param <T>     The target type
	 * @param <C>     The context type
	 * @return A filter that passes only if all filters pass
	 */
	@SafeVarargs
	public static <T, C> Filter<T, C> all(Filter<T, C>... filters) {
		return all(Arrays.asList(filters));
	}

	/**
	 * Creates an OR filter from a collection of filters.
	 *
	 * @param filters The filters to combine
	 * @param <T>     The target type
	 * @param <C>     The context type
	 * @return A filter that passes if any filter passes
	 */
	public static <T, C> Filter<T, C> any(Collection<Filter<T, C>> filters) {
		if (filters.isEmpty()) {
			return Filter.alwaysFalse();
		}
		if (filters.size() == 1) {
			return filters.iterator().next();
		}
		return new OrFilter<>(List.copyOf(filters));
	}

	/**
	 * Creates an OR filter from varargs.
	 *
	 * @param filters The filters to combine
	 * @param <T>     The target type
	 * @param <C>     The context type
	 * @return A filter that passes if any filter passes
	 */
	@SafeVarargs
	public static <T, C> Filter<T, C> any(Filter<T, C>... filters) {
		return any(Arrays.asList(filters));
	}

	/**
	 * Negates a filter.
	 *
	 * @param filter The filter to negate
	 * @param <T>    The target type
	 * @param <C>    The context type
	 * @return A filter that returns the opposite of the input filter
	 */
	public static <T, C> Filter<T, C> not(Filter<T, C> filter) {
		return new NotFilter<>(filter);
	}

	/**
	 * Creates a filter that ignores context.
	 * <p>
	 * Useful for wrapping simple predicates into the Filter interface.
	 *
	 * @param predicate The predicate to wrap
	 * @param <T>       The target type
	 * @param <C>       The context type
	 * @return A filter that applies the predicate ignoring context
	 */
	public static <T, C> Filter<T, C> fromPredicate(Predicate<T> predicate) {
		return (context, target) -> predicate.test(target);
	}

	/**
	 * Creates a filter that only uses context.
	 * <p>
	 * Useful when the decision depends only on context, not the target.
	 *
	 * @param predicate The predicate to apply to context
	 * @param <T>       The target type
	 * @param <C>       The context type
	 * @return A filter that applies the predicate to context
	 */
	public static <T, C> Filter<T, C> fromContext(Predicate<C> predicate) {
		return (context, target) -> predicate.test(context);
	}

	/**
	 * Applies a list of filters to a context and target, returning true only if all pass.
	 *
	 * @param filters The filters to apply
	 * @param context The context
	 * @param target  The target to test
	 * @param <T>     The target type
	 * @param <C>     The context type
	 * @return {@code true} if all filters pass
	 */
	public static <T, C> boolean testAll(List<Filter<T, C>> filters, C context, T target) {
		for (Filter<T, C> filter : filters) {
			if (!filter.test(context, target)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Applies a list of filters to a context and target, returning true if any pass.
	 *
	 * @param filters The filters to apply
	 * @param context The context
	 * @param target  The target to test
	 * @param <T>     The target type
	 * @param <C>     The context type
	 * @return {@code true} if any filter passes
	 */
	public static <T, C> boolean testAny(List<Filter<T, C>> filters, C context, T target) {
		for (Filter<T, C> filter : filters) {
			if (filter.test(context, target)) {
				return true;
			}
		}
		return false;
	}
}
