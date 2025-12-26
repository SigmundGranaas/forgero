package com.sigmundgranaas.forgero.common.filter;

/**
 * Base interface for all filter types in Forgero.
 * <p>
 * Filters are used to test whether a target passes certain criteria, optionally
 * using additional context information.
 *
 * <h2>Type Parameters</h2>
 * <ul>
 *   <li>{@code T} - The type of the target being tested (e.g., Entity, ItemStack, BlockPos)</li>
 *   <li>{@code C} - The type of the context providing additional information for the test.
 *                   Use {@link Void} for filters that don't need context.</li>
 * </ul>
 *
 * <h2>Usage Examples</h2>
 * <pre>{@code
 * // Filter with context (context first, target second)
 * Filter<Entity, Entity> entityFilter = (source, candidate) -> candidate.isAlive();
 *
 * // Filter without context (use Void)
 * Filter<ItemStack, Void> itemFilter = (ctx, stack) -> !stack.isEmpty();
 * }</pre>
 *
 * @param <T> The target type being tested
 * @param <C> The context type (use {@link Void} if no context needed)
 * @see CompositeFilter
 */
@FunctionalInterface
public interface Filter<T, C> {

	/**
	 * Tests whether the target passes this filter.
	 *
	 * @param context Additional context for the test (may be null for Void context)
	 * @param target  The object being tested
	 * @return {@code true} if the target passes the filter, {@code false} otherwise
	 */
	boolean test(C context, T target);

	/**
	 * Creates a filter that always passes.
	 *
	 * @param <T> The target type
	 * @param <C> The context type
	 * @return A filter that always returns {@code true}
	 */
	static <T, C> Filter<T, C> alwaysTrue() {
		return (context, target) -> true;
	}

	/**
	 * Creates a filter that always fails.
	 *
	 * @param <T> The target type
	 * @param <C> The context type
	 * @return A filter that always returns {@code false}
	 */
	static <T, C> Filter<T, C> alwaysFalse() {
		return (context, target) -> false;
	}

	/**
	 * Returns a filter that is the logical negation of this filter.
	 *
	 * @return A filter that returns {@code true} when this filter returns {@code false}
	 */
	default Filter<T, C> negate() {
		return (context, target) -> !this.test(context, target);
	}

	/**
	 * Returns a composed filter that represents a logical AND of this filter
	 * and another filter.
	 *
	 * @param other The other filter to AND with
	 * @return A composed filter
	 */
	default Filter<T, C> and(Filter<T, C> other) {
		return (context, target) -> this.test(context, target) && other.test(context, target);
	}

	/**
	 * Returns a composed filter that represents a logical OR of this filter
	 * and another filter.
	 *
	 * @param other The other filter to OR with
	 * @return A composed filter
	 */
	default Filter<T, C> or(Filter<T, C> other) {
		return (context, target) -> this.test(context, target) || other.test(context, target);
	}
}
