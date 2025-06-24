package com.sigmundgranaas.forgero.core.property.api;

import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.property.context.DynamicContext;

import java.util.Optional;

/**
 * The public-facing service for resolving all types of properties from components.
 * This resolver is a generic engine that delegates all logic to registered {@link DataTypeEngine}s.
 * It is completely unaware of specific data types like "attributes" or "features", making the system
 * highly extensible.
 */
public interface Resolver {
	/**
	 * Performs a full resolution for a given component and a type-safe key.
	 * The process is designed to be highly efficient, with internal caching of the expensive "bake" phase.
	 *
	 * @param component The root component of the item (e.g., a pickaxe).
	 * @param key       The type-safe key identifying the data to resolve (e.g., {@code AttributeEngine.KEY}).
	 * @param context   The dynamic context for the calculation, containing runtime information.
	 * @param <R>       The type of the result, which is inferred from the key, ensuring type safety.
	 * @return An Optional containing the fully resolved data, or empty if no engine is registered for the key.
	 */
	<R> Optional<R> resolve(Component component, ResolutionKey<R> key, DynamicContext context);

	/**
	 * Convenience overload for resolving with an empty dynamic context.
	 * This is useful for properties that do not depend on runtime information.
	 *
	 * @param component The root component of the item.
	 * @param key       The type-safe key identifying the data to resolve.
	 * @param <R>       The type of the result, inferred from the key.
	 * @return An Optional containing the fully resolved data, or empty if no engine is registered for the key.
	 */
	default <R> Optional<R> resolve(Component component, ResolutionKey<R> key) {
		return resolve(component, key, DynamicContext.empty());
	}
}
