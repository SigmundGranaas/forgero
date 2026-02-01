package com.sigmundgranaas.forgero.core.component.api;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.slot.ComponentUpgradeSlot;

/**
 * Visitor interface for traversing component trees.
 *
 * <p>Implementations collect specific information during traversal. The visitor
 * pattern allows multiple different collection strategies to be applied in a
 * single pass through the component tree.</p>
 *
 * <h2>Traversal Order</h2>
 * <p>The tree is traversed depth-first:</p>
 * <ol>
 *   <li>{@link #visit} is called for each component</li>
 *   <li>If visit returns true, children are traversed</li>
 *   <li>For structure parts: {@link #enterStructureSlot} → visit children → {@link #exitStructureSlot}</li>
 *   <li>For upgrade slots: {@link #enterUpgradeSlot} → visit children → {@link #exitUpgradeSlot}</li>
 * </ol>
 *
 * <h2>Example Usage</h2>
 * <pre>{@code
 * // Collect all attribute types in the tree
 * class AttributeTypeCollector implements ComponentTreeVisitor<Set<OpenIdentifier>> {
 *     private final Set<OpenIdentifier> types = new HashSet<>();
 *
 *     @Override
 *     public boolean visit(Component component, Component root, int depth) {
 *         component.properties(Attribute.KEY).forEach(attr -> types.add(attr.type()));
 *         return true; // Continue to children
 *     }
 *
 *     @Override
 *     public Set<OpenIdentifier> getResult() {
 *         return types;
 *     }
 * }
 *
 * Set<OpenIdentifier> types = ComponentTreeWalker.walk(root, new AttributeTypeCollector());
 * }</pre>
 *
 * @param <T> The type of result collected during traversal
 * @see ComponentTreeWalker
 */
public interface ComponentTreeVisitor<T> {

	/**
	 * Visit a component during traversal.
	 *
	 * @param component The current component being visited
	 * @param root The root component of the tree (for context resolution)
	 * @param depth The current depth in the tree (0 = root)
	 * @return true to continue visiting children of this component, false to skip children
	 */
	boolean visit(Component component, Component root, int depth);

	/**
	 * Called when entering a structure slot (ComponentPart).
	 *
	 * <p>Structure slots contain required parts of a component's composition
	 * (e.g., blade, handle, limb). This method is called before visiting
	 * the component in the slot.</p>
	 *
	 * @param slotId The identifier of the slot being entered
	 * @param component The component in the slot
	 */
	default void enterStructureSlot(OpenIdentifier slotId, Component component) {
		// Default: no-op
	}

	/**
	 * Called when exiting a structure slot.
	 *
	 * <p>This method is called after all descendants of the slot's component
	 * have been visited.</p>
	 *
	 * @param slotId The identifier of the slot being exited
	 */
	default void exitStructureSlot(OpenIdentifier slotId) {
		// Default: no-op
	}

	/**
	 * Called when entering an upgrade slot (ComponentUpgradeSlot).
	 *
	 * <p>Upgrade slots contain optional modifications to a component
	 * (e.g., gems, reinforcements). This method is called before visiting
	 * the upgrade component in the slot.</p>
	 *
	 * @param slot The upgrade slot being entered
	 */
	default void enterUpgradeSlot(ComponentUpgradeSlot slot) {
		// Default: no-op
	}

	/**
	 * Called when exiting an upgrade slot.
	 *
	 * <p>This method is called after all descendants of the upgrade
	 * have been visited.</p>
	 *
	 * @param slot The upgrade slot being exited
	 */
	default void exitUpgradeSlot(ComponentUpgradeSlot slot) {
		// Default: no-op
	}

	/**
	 * Get the collected result after traversal completes.
	 *
	 * @return The result of the traversal
	 */
	T getResult();
}
