package com.sigmundgranaas.forgero.core.component.api;

import com.sigmundgranaas.forgero.core.component.api.slot.ComponentUpgradeSlot;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentPart;

import java.util.ArrayList;
import java.util.List;

/**
 * Walks a component tree, invoking visitors at each node.
 *
 * <p>This utility class provides methods for traversing component hierarchies
 * with one or more visitors. It supports both single-visitor and multi-visitor
 * traversals, allowing multiple collection strategies to be applied in a
 * single pass through the tree.</p>
 *
 * <h2>Traversal Order</h2>
 * <p>The tree is traversed depth-first in the following order:</p>
 * <ol>
 *   <li>Visit the current component</li>
 *   <li>If visitor returns true, visit structure parts (ComponentPart)</li>
 *   <li>Then visit upgrade slots (ComponentUpgradeSlot)</li>
 * </ol>
 *
 * <h2>Example: Single Visitor</h2>
 * <pre>{@code
 * // Collect all components in the tree
 * class ComponentCollector implements ComponentTreeVisitor<List<Component>> {
 *     private final List<Component> components = new ArrayList<>();
 *
 *     @Override
 *     public boolean visit(Component component, Component root, int depth) {
 *         components.add(component);
 *         return true;
 *     }
 *
 *     @Override
 *     public List<Component> getResult() {
 *         return components;
 *     }
 * }
 *
 * List<Component> allComponents = ComponentTreeWalker.walk(root, new ComponentCollector());
 * }</pre>
 *
 * <h2>Example: Multiple Visitors (Single Pass)</h2>
 * <pre>{@code
 * PartCompositeCollector partCollector = new PartCompositeCollector();
 * DefaultCollector defaultCollector = new DefaultCollector();
 * UpgradeCollector upgradeCollector = new UpgradeCollector();
 *
 * ComponentTreeWalker.walkMultiple(root, List.of(partCollector, defaultCollector, upgradeCollector));
 *
 * // Each collector now has its results
 * Map<String, List<Attribute>> partComposite = partCollector.getResult();
 * List<Attribute> defaults = defaultCollector.getResult();
 * List<Attribute> upgrades = upgradeCollector.getResult();
 * }</pre>
 *
 * @see ComponentTreeVisitor
 */
public final class ComponentTreeWalker {

	private ComponentTreeWalker() {
		// Utility class
	}

	/**
	 * Walk the component tree with a single visitor.
	 *
	 * @param root The root component to start traversal from
	 * @param visitor The visitor to apply at each node
	 * @param <T> The result type of the visitor
	 * @return The visitor's collected result
	 */
	public static <T> T walk(Component root, ComponentTreeVisitor<T> visitor) {
		walkRecursive(root, root, visitor, 0);
		return visitor.getResult();
	}

	/**
	 * Walk the component tree with multiple visitors in a single pass.
	 *
	 * <p>This is more efficient than calling {@link #walk} multiple times
	 * when you need to collect different types of information from the
	 * same tree.</p>
	 *
	 * @param root The root component to start traversal from
	 * @param visitors The list of visitors to apply at each node
	 */
	public static void walkMultiple(Component root, List<? extends ComponentTreeVisitor<?>> visitors) {
		walkRecursiveMultiple(root, root, new ArrayList<>(visitors), 0);
	}

	/**
	 * Walk the component tree starting from a specific component (not necessarily root).
	 *
	 * <p>This variant allows specifying a different root for context resolution
	 * while starting traversal from a subtree.</p>
	 *
	 * @param start The component to start traversal from
	 * @param root The root component for context resolution
	 * @param visitor The visitor to apply at each node
	 * @param <T> The result type of the visitor
	 * @return The visitor's collected result
	 */
	public static <T> T walkFrom(Component start, Component root, ComponentTreeVisitor<T> visitor) {
		walkRecursive(start, root, visitor, 0);
		return visitor.getResult();
	}

	private static <T> void walkRecursive(
			Component component,
			Component root,
			ComponentTreeVisitor<T> visitor,
			int depth
	) {
		boolean continueToChildren = visitor.visit(component, root, depth);

		if (!continueToChildren) {
			return;
		}

		// Visit structure parts
		if (component instanceof StructuredComponent structured) {
			for (ComponentPart part : structured.structure().allParts()) {
				visitor.enterStructureSlot(part.id(), part.getContent());
				walkRecursive(part.getContent(), root, visitor, depth + 1);
				visitor.exitStructureSlot(part.id());
			}
		}

		// Visit upgrade slots
		if (component instanceof CustomizableComponent customizable) {
			for (ComponentUpgradeSlot slot : customizable.upgrades().allUpgradeSlots()) {
				if (slot.isFilled()) {
					visitor.enterUpgradeSlot(slot);
					slot.content().ifPresent(upgrade ->
							walkRecursive(upgrade, root, visitor, depth + 1));
					visitor.exitUpgradeSlot(slot);
				}
			}
		}
	}

	private static void walkRecursiveMultiple(
			Component component,
			Component root,
			List<ComponentTreeVisitor<?>> visitors,
			int depth
	) {
		if (visitors.isEmpty()) {
			return;
		}

		// Visit with all visitors, track which want to continue
		List<ComponentTreeVisitor<?>> continueVisitors = new ArrayList<>();
		for (ComponentTreeVisitor<?> visitor : visitors) {
			if (visitor.visit(component, root, depth)) {
				continueVisitors.add(visitor);
			}
		}

		if (continueVisitors.isEmpty()) {
			return;
		}

		// Visit structure parts
		if (component instanceof StructuredComponent structured) {
			for (ComponentPart part : structured.structure().allParts()) {
				for (ComponentTreeVisitor<?> v : continueVisitors) {
					v.enterStructureSlot(part.id(), part.getContent());
				}
				walkRecursiveMultiple(part.getContent(), root, continueVisitors, depth + 1);
				for (ComponentTreeVisitor<?> v : continueVisitors) {
					v.exitStructureSlot(part.id());
				}
			}
		}

		// Visit upgrade slots
		if (component instanceof CustomizableComponent customizable) {
			for (ComponentUpgradeSlot slot : customizable.upgrades().allUpgradeSlots()) {
				if (slot.isFilled()) {
					for (ComponentTreeVisitor<?> v : continueVisitors) {
						v.enterUpgradeSlot(slot);
					}
					slot.content().ifPresent(upgrade ->
							walkRecursiveMultiple(upgrade, root, continueVisitors, depth + 1));
					for (ComponentTreeVisitor<?> v : continueVisitors) {
						v.exitUpgradeSlot(slot);
					}
				}
			}
		}
	}
}
