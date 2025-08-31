package com.sigmundgranaas.forgero.core.property.api;

import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.property.context.DynamicContext;

/**
 * The public-facing service for resolving all types of properties from components.
 * This resolver is a generic engine that orchestrates the two-phase resolution process
 * ("bake" and "apply") for a given {@link DataTypeEngine}. It is completely unaware
 * of specific data types, making the system highly extensible.
 */
public interface Resolver {
	/**
	 * Performs a full resolution for a given component using a specific engine.
	 * The process is designed to be highly efficient, with internal caching of the expensive "bake" phase.
	 *
	 * @param component The root component of the item (e.g., a pickaxe).
	 * @param engine    The {@link DataTypeEngine} that defines the entire resolution logic for a specific data type.
	 * @param context   The dynamic context for the calculation, containing runtime information.
	 * @param <B>       The type of the intermediate baked result, managed by the engine.
	 * @param <R>       The type of the final result, which is inferred from the engine.
	 * @return The fully resolved data, as produced by the engine.
	 */
	<B, R> R resolve(Component component, DataTypeEngine<B, R> engine, DynamicContext context);

	/**
	 * Convenience overload for resolving with an empty dynamic context.
	 * This is useful for properties that do not depend on runtime information.
	 *
	 * @param component The root component of the item.
	 * @param engine    The {@link DataTypeEngine} that defines the resolution logic.
	 * @param <B>       The type of the intermediate baked result.
	 * @param <R>       The type of the final result.
	 * @return The fully resolved data.
	 */
	default <B, R> R resolve(Component component, DataTypeEngine<B, R> engine) {
		return resolve(component, engine, DynamicContext.empty());
	}
}
