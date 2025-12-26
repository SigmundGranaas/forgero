package com.sigmundgranaas.forgero.common.filter;

import java.util.List;

/**
 * A filter composed of multiple child filters.
 * <p>
 * This interface represents composite filters like AND, OR, and NOT operations.
 * Implementations combine multiple filters using boolean logic.
 *
 * @param <T> The target type being tested
 * @param <C> The context type
 * @see AndFilter
 * @see OrFilter
 * @see NotFilter
 */
public interface CompositeFilter<T, C> extends Filter<T, C> {

	/**
	 * Returns the child filters that make up this composite.
	 *
	 * @return An unmodifiable list of child filters
	 */
	List<Filter<T, C>> filters();
}
