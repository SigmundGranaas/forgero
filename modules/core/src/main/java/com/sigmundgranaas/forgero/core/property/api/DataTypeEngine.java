package com.sigmundgranaas.forgero.core.property.api;

import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.ComponentTraversal;

import java.util.List;
import java.util.stream.Stream;

/**
 * A self-contained engine that manages the entire resolution lifecycle for a specific data type.
 * It defines how to bake an intermediate result and how to convert that into a final, queryable object.
 *
 * <p>Each engine is self-contained and can resolve properties directly from a component tree
 * using the {@link #resolve(Component)} method.
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
	 * The "Finalize" phase. Converts the compiled intermediate result into the final result
	 * type. All static conditions have already been resolved during {@link #bake}; any
	 * dynamic conditions are carried through as data for the game layer to evaluate.
	 *
	 * @param baked The intermediate result from the bake phase.
	 * @return The final, compiled object of type R.
	 */
	R apply(B baked);

	/**
	 * Compiles properties from a component tree.
	 *
	 * <p>This is the primary entry point. It traverses the component tree, bakes the
	 * intermediate result (resolving all static conditions), and finalizes it. Runtime
	 * state never enters this path: dynamic conditions are carried in the result as data
	 * and evaluated by the game layer.
	 *
	 * <p>For terminal components (equipment) that have pre-baked attributes, prefer using
	 * {@link com.sigmundgranaas.forgero.core.component.api.EquipmentComponent#getAttribute} directly.
	 *
	 * @param root The root component of the tree to compile.
	 * @return The fully compiled result.
	 */
	default R resolve(Component root) {
		List<Component> components = ComponentTraversal.traverse(root);
		B baked = bake(components.stream());
		return apply(baked);
	}
}
