package com.sigmundgranaas.forgero.core.property.api;

import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.ComponentTraversal;
import com.sigmundgranaas.forgero.core.property.context.DynamicContext;

import java.util.List;
import java.util.stream.Stream;

/**
 * A self-contained engine that manages the entire resolution lifecycle for a specific data type.
 * It defines how to bake an intermediate result and how to convert that into a final, queryable object.
 *
 * <p>Each engine is self-contained and can resolve properties directly from a component tree
 * using the {@link #resolve(Component, DynamicContext)} method.
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

	/**
	 * Resolves properties from a component tree with a dynamic context.
	 *
	 * <p>This is the primary entry point for property resolution. It traverses the component tree,
	 * bakes the intermediate result, and applies the dynamic context to produce the final result.
	 *
	 * <p>For terminal components (equipment) that have pre-baked attributes, prefer using
	 * {@link com.sigmundgranaas.forgero.core.component.api.EquipmentComponent#getAttribute} directly.
	 *
	 * @param root    The root component of the tree to resolve.
	 * @param context The dynamic context for runtime condition evaluation.
	 * @return The fully resolved result.
	 */
	default R resolve(Component root, DynamicContext context) {
		List<Component> components = ComponentTraversal.traverse(root);
		B baked = bake(components.stream());
		return apply(baked, context);
	}

	/**
	 * Resolves properties from a component tree with an empty context.
	 *
	 * <p>Convenience overload for cases where no runtime context is needed.
	 *
	 * @param root The root component of the tree to resolve.
	 * @return The fully resolved result.
	 */
	default R resolve(Component root) {
		return resolve(root, DynamicContext.empty());
	}
}
