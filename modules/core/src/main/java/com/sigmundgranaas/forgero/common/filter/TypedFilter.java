package com.sigmundgranaas.forgero.common.filter;

/**
 * A filter that has a type identifier for codec-based serialization.
 * <p>
 * This interface extends {@link Filter} with a {@link #type()} method that
 * returns a unique identifier used for polymorphic serialization via codecs.
 *
 * <h2>Type Naming Convention</h2>
 * Types should follow the format {@code "namespace:filter_name"}, for example:
 * <ul>
 *   <li>{@code "forgero:and"} - Composite AND filter</li>
 *   <li>{@code "forgero:is_player"} - Player entity filter</li>
 *   <li>{@code "forgero:can_mine"} - Block mining filter</li>
 * </ul>
 *
 * @param <T> The target type being tested
 * @param <C> The context type
 * @see Filter
 */
public interface TypedFilter<T, C> extends Filter<T, C> {

	/**
	 * Returns the unique type identifier for this filter.
	 * <p>
	 * This is used by codecs to determine which filter implementation
	 * to instantiate during deserialization.
	 *
	 * @return The type identifier in {@code "namespace:name"} format
	 */
	String type();
}
