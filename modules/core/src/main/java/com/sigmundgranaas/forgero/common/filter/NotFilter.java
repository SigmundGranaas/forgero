package com.sigmundgranaas.forgero.common.filter;

import java.util.List;

/**
 * A filter that negates the result of its child filter.
 * <p>
 * Implements logical NOT operation.
 *
 * @param <T> The target type being tested
 * @param <C> The context type
 */
public record NotFilter<T, C>(Filter<T, C> filter) implements CompositeFilter<T, C> {

	/**
	 * Creates a NotFilter wrapping the given filter.
	 *
	 * @param filter The filter to negate
	 * @param <T>    The target type
	 * @param <C>    The context type
	 * @return A new NotFilter
	 */
	public static <T, C> NotFilter<T, C> of(Filter<T, C> filter) {
		return new NotFilter<>(filter);
	}

	@Override
	public boolean test(C context, T target) {
		return !filter.test(context, target);
	}

	@Override
	public List<Filter<T, C>> filters() {
		return List.of(filter);
	}
}
