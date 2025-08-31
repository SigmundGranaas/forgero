package com.sigmundgranaas.forgero.core.property.api;

import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.property.context.DynamicContext;

import java.util.stream.Stream;

/**
 * A self-contained engine that manages the entire resolution lifecycle for a specific data type.
 * It defines how to bake an intermediate result and how to convert that into a final, queryable object.
 * This approach ensures that the central {@link Resolver} remains completely generic and unaware of
 * specific data types like Attributes or Features.
 *
 * @param <B> The type of the intermediate, cachable "baked" object. This object should be lightweight and
 *            contain all statically-resolved data needed for the final computation.
 * @param <R> The final, resolved result type that the client will receive (e.g., an AttributeQueryResult or a List of Features).
 */
public interface DataTypeEngine<B, R> {
	/**
	 * @return The type-safe key this engine is responsible for.
	 */
	ResolutionKey<R> key();

	/**
	 * The "Bake" phase. This method processes all components in a structure to create an intermediate,
	 * cachable result. This is the ideal place to evaluate all static conditions, as it is only run
	 * once per component state and the result is cached.
	 *
	 * @param components A stream of all components in the tree, starting with the root.
	 * @return The intermediate baked object of type B.
	 */
	B bake(Stream<Component> components);

	/**
	 * The "Apply" phase. This method takes the pre-computed baked result and a dynamic context
	 * (containing runtime information like the target entity) to produce the final result.
	 * This operation should be very fast, as it only needs to evaluate dynamic conditions.
	 *
	 * @param baked   The intermediate result from the bake phase.
	 * @param context The dynamic context for the calculation.
	 * @return The final, resolved object of type R.
	 */
	R apply(B baked, DynamicContext context);
}
