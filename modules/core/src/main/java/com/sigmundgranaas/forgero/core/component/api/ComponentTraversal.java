package com.sigmundgranaas.forgero.core.component.api;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Utility class for traversing component trees.
 *
 * <p>Provides pre-order traversal of component hierarchies, collecting all components
 * from root to leaves. This is used by {@link com.sigmundgranaas.forgero.core.property.api.CompilerPass}
 * implementations to resolve properties from component trees.
 */
public final class ComponentTraversal {

	private ComponentTraversal() {
		// Utility class
	}

	/**
	 * Performs a pre-order traversal of the component tree.
	 *
	 * <p>The traversal visits the root first, then recursively visits children
	 * in order. This ensures parent components are processed before their children,
	 * which is important for property inheritance and override semantics.
	 *
	 * @param root The root component of the tree to traverse.
	 * @return A list of all components in the tree, in pre-order.
	 */
	public static List<Component> traverse(Component root) {
		List<Component> allComponents = new ArrayList<>();
		Deque<Component> stack = new ArrayDeque<>();
		stack.push(root);

		while (!stack.isEmpty()) {
			Component current = stack.pop();
			allComponents.add(current);
			List<Component> children = current.getChildren();
			// Push in reverse order to maintain left-to-right traversal
			for (int i = children.size() - 1; i >= 0; i--) {
				stack.push(children.get(i));
			}
		}
		return allComponents;
	}
}
