package com.sigmundgranaas.forgero.core.property.api;

import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.ComponentTraversal;

import java.util.stream.Stream;

/**
 * A single-phase compiler for one kind of property data.
 *
 * <p>A pass transforms a component tree into a compiled result of type {@code R}, evaluating
 * all static (structural) conditions and carrying any dynamic conditions through as data. It
 * runs once, at component construction (see {@code ComponentCompiler}); the result is stored
 * on the terminal and read at runtime without re-traversal. There is no runtime phase and no
 * runtime context — dynamic evaluation belongs to the game layer (see
 * {@code docs/ADR-002-compiler-in-the-factory.md}).
 *
 * @param <R> The compiled result type (e.g. {@code BakedAttributes} or {@code List<OnHitProperty>}).
 */
public interface CompilerPass<R> {
	/**
	 * @return The type-safe key identifying this pass's compiled output.
	 */
	ResolutionKey<R> key();

	/**
	 * Compiles the result from a stream of all components in a tree (root first).
	 */
	R compile(Stream<Component> components);

	/**
	 * Convenience: compile directly from a root component. Used for the rare non-terminal
	 * read; terminals are compiled once at construction and read their stored result.
	 */
	default R resolve(Component root) {
		return compile(ComponentTraversal.traverse(root).stream());
	}
}
