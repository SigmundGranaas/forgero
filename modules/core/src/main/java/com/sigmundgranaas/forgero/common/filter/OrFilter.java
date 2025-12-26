package com.sigmundgranaas.forgero.common.filter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A composite filter that requires ANY child filter to pass.
 * <p>
 * Implements logical OR operation with short-circuit evaluation.
 * If any filter passes, the remaining filters are not evaluated.
 *
 * @param <T> The target type being tested
 * @param <C> The context type
 */
public record OrFilter<T, C>(List<Filter<T, C>> filters) implements CompositeFilter<T, C> {

	/**
	 * Creates an OrFilter with the given filters.
	 *
	 * @param filters The filters to combine with OR logic
	 */
	public OrFilter {
		filters = List.copyOf(filters);
	}

	/**
	 * Creates an OrFilter from varargs.
	 *
	 * @param filters The filters to combine
	 * @param <T>     The target type
	 * @param <C>     The context type
	 * @return A new OrFilter
	 */
	@SafeVarargs
	public static <T, C> OrFilter<T, C> of(Filter<T, C>... filters) {
		return new OrFilter<>(Arrays.asList(filters));
	}

	/**
	 * Creates an empty OrFilter that always fails.
	 *
	 * @param <T> The target type
	 * @param <C> The context type
	 * @return An empty OrFilter
	 */
	public static <T, C> OrFilter<T, C> empty() {
		return new OrFilter<>(Collections.emptyList());
	}

	@Override
	public boolean test(C context, T target) {
		for (Filter<T, C> filter : filters) {
			if (filter.test(context, target)) {
				return true;
			}
		}
		return false;
	}
}
