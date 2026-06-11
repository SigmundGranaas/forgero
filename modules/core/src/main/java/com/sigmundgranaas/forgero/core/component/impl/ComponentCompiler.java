package com.sigmundgranaas.forgero.core.component.impl;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.attribute.api.BakedAttributes;
import com.sigmundgranaas.forgero.core.attribute.impl.AttributeEngine;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.property.api.DataTypeEngine;
import com.sigmundgranaas.forgero.core.property.compiled.CompiledProperties;
import com.sigmundgranaas.forgero.core.property.compiled.CompilerPasses;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Compiles a terminal component's whole tree into a {@link CompiledProperties} artifact.
 * <p>
 * Runs at construction time (the factory): it traverses the tree once, compiles attributes
 * via the built-in attribute pass, and runs every property pass registered in
 * {@link CompilerPasses}. The result is stored on the terminal component, so the game layer
 * reads compiled data and never re-traverses the tree. See
 * {@code docs/ADR-002-compiler-in-the-factory.md}.
 */
final class ComponentCompiler {
	private static final AttributeEngine ATTRIBUTES = new AttributeEngine();

	private ComponentCompiler() {}

	/**
	 * Compiles the full property artifact for a component tree.
	 */
	static CompiledProperties compile(Component root) {
		List<Component> components = traverse(root);
		BakedAttributes attributes = ATTRIBUTES.bake(components.stream());

		Map<OpenIdentifier, List<?>> properties = new HashMap<>();
		for (var entry : CompilerPasses.registered().entrySet()) {
			List<?> compiled = runPass(entry.getValue().get(), components);
			if (!compiled.isEmpty()) {
				properties.put(entry.getKey(), compiled);
			}
		}

		return new CompiledProperties(attributes, properties);
	}

	private static <B, R extends List<?>> R runPass(DataTypeEngine<B, R> engine, List<Component> components) {
		return engine.apply(engine.bake(components.stream()));
	}

	private static List<Component> traverse(Component component) {
		List<Component> allComponents = new ArrayList<>();
		Deque<Component> stack = new ArrayDeque<>();
		stack.push(component);
		while (!stack.isEmpty()) {
			Component current = stack.pop();
			allComponents.add(current);
			List<Component> children = current.getChildren();
			for (int i = children.size() - 1; i >= 0; i--) {
				stack.push(children.get(i));
			}
		}
		return allComponents;
	}
}
