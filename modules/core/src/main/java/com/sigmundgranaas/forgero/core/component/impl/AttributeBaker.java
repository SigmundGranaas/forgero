package com.sigmundgranaas.forgero.core.component.impl;

import com.sigmundgranaas.forgero.core.attribute.api.BakedAttributes;
import com.sigmundgranaas.forgero.core.attribute.impl.AttributeEngine;
import com.sigmundgranaas.forgero.core.component.api.Component;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.stream.Stream;

/**
 * Internal utility for baking attributes from a component tree.
 * <p>
 * This is an implementation detail used by equipment components to
 * pre-compute their attributes at creation time.
 */
final class AttributeBaker {
	private static final AttributeEngine ENGINE = new AttributeEngine();

	private AttributeBaker() {}

	/**
	 * Bakes attributes from a component tree.
	 *
	 * @param root The root component
	 * @return Baked attributes with O(1) type lookup
	 */
	static BakedAttributes bake(Component root) {
		List<Component> components = traverse(root);
		return ENGINE.bake(components.stream());
	}

	/**
	 * Bakes attributes from a root component with additional children.
	 * Used when the component structure isn't yet finalized (during construction).
	 */
	static BakedAttributes bake(Component root, List<Component> additionalChildren) {
		List<Component> components = new ArrayList<>();
		components.add(root);

		// Traverse root's existing children
		for (Component child : root.getChildren()) {
			components.addAll(traverse(child));
		}

		// Traverse additional children
		for (Component child : additionalChildren) {
			components.addAll(traverse(child));
		}

		return ENGINE.bake(components.stream());
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
