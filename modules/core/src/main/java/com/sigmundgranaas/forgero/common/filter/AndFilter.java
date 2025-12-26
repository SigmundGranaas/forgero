package com.sigmundgranaas.forgero.common.filter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A composite filter that requires ALL child filters to pass.
 * <p>
 * Implements logical AND operation with short-circuit evaluation.
 * If any filter fails, the remaining filters are not evaluated.
 *
 * @param <T> The target type being tested
 * @param <C> The context type
 */
public record AndFilter<T, C>(List<Filter<T, C>> filters) implements CompositeFilter<T, C> {

	/**
	 * Creates an AndFilter with the given filters.
	 *
	 * @param filters The filters to combine with AND logic
	 */
	public AndFilter {
		filters = List.copyOf(filters);
	}

	/**
	 * Creates an AndFilter from varargs.
	 *
	 * @param filters The filters to combine
	 * @param <T>     The target type
	 * @param <C>     The context type
	 * @return A new AndFilter
	 */
	@SafeVarargs
	public static <T, C> AndFilter<T, C> of(Filter<T, C>... filters) {
		return new AndFilter<>(Arrays.asList(filters));
	}

	/**
	 * Creates an empty AndFilter that always passes.
	 *
	 * @param <T> The target type
	 * @param <C> The context type
	 * @return An empty AndFilter
	 */
	public static <T, C> AndFilter<T, C> empty() {
		return new AndFilter<>(Collections.emptyList());
	}

	@Override
	public boolean test(C context, T target) {
		for (Filter<T, C> filter : filters) {
			if (!filter.test(context, target)) {
				return false;
			}
		}
		return true;
	}
}
