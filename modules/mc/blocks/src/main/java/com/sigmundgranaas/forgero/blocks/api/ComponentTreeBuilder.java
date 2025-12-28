package com.sigmundgranaas.forgero.blocks.api;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.slot.ComponentUpgradeSlot;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Builds a hierarchical tree representation of a component's structure for UI display.
 * <p>
 * This interface abstracts the traversal of component hierarchies (parts and upgrade slots)
 * into a uniform tree structure that can be used by the slot layout engine.
 * <p>
 * Different implementations can provide different views:
 * <ul>
 *   <li>UpgradeTreeBuilder - Shows only editable upgrade slots</li>
 *   <li>FullTreeBuilder - Shows structure parts + upgrade slots</li>
 * </ul>
 */
public interface ComponentTreeBuilder {

	/**
	 * Builds a slot tree from the given component.
	 *
	 * @param component The component to build a tree from
	 * @return A SlotTree representing the component's slot hierarchy
	 */
	SlotTree buildTree(Component component);

	/**
	 * Represents a tree of slot nodes for UI layout.
	 */
	record SlotTree(SlotNode root) {

		/**
		 * Creates an empty tree with no nodes.
		 */
		public static SlotTree empty() {
			return new SlotTree(null);
		}

		/**
		 * Checks if the tree is empty.
		 */
		public boolean isEmpty() {
			return root == null;
		}

		/**
		 * Traverses all nodes in the tree in pre-order.
		 *
		 * @param visitor Consumer called for each node
		 */
		public void traverse(Consumer<SlotNode> visitor) {
			if (root != null) {
				traverseNode(root, visitor);
			}
		}

		private void traverseNode(SlotNode node, Consumer<SlotNode> visitor) {
			visitor.accept(node);
			for (SlotNode child : node.children()) {
				traverseNode(child, visitor);
			}
		}

		/**
		 * Counts the total number of nodes in the tree.
		 */
		public int size() {
			if (root == null) return 0;
			return countNodes(root);
		}

		private int countNodes(SlotNode node) {
			int count = 1;
			for (SlotNode child : node.children()) {
				count += countNodes(child);
			}
			return count;
		}

		/**
		 * Counts only the upgrade slot nodes (editable slots).
		 */
		public int upgradeSlotCount() {
			if (root == null) return 0;
			int[] count = {0};
			traverse(node -> {
				if (node.nodeType() == SlotNodeType.UPGRADE_SLOT) {
					count[0]++;
				}
			});
			return count[0];
		}
	}

	/**
	 * A node in the slot tree representing a component part or upgrade slot.
	 *
	 * @param id          Unique identifier for this node
	 * @param nodeType    The type of node (ROOT, STRUCTURE_PART, UPGRADE_SLOT)
	 * @param content     The component in this slot (if any)
	 * @param upgradeSlot The upgrade slot data (only for UPGRADE_SLOT type)
	 * @param children    Child nodes in the hierarchy
	 */
	record SlotNode(
			OpenIdentifier id,
			SlotNodeType nodeType,
			Optional<Component> content,
			ComponentUpgradeSlot upgradeSlot,
			List<SlotNode> children
	) {
		/**
		 * Creates a node without children.
		 */
		public static SlotNode leaf(
				OpenIdentifier id,
				SlotNodeType type,
				Optional<Component> content,
				ComponentUpgradeSlot slot
		) {
			return new SlotNode(id, type, content, slot, Collections.emptyList());
		}

		/**
		 * Checks if this node represents an editable upgrade slot.
		 */
		public boolean isUpgradeSlot() {
			return nodeType == SlotNodeType.UPGRADE_SLOT;
		}

		/**
		 * Checks if this node has content (is filled).
		 */
		public boolean isFilled() {
			return content.isPresent();
		}

		/**
		 * Checks if this node is empty (no content).
		 */
		public boolean isEmpty() {
			return content.isEmpty();
		}

		/**
		 * Returns the depth of the subtree rooted at this node.
		 */
		public int depth() {
			if (children.isEmpty()) return 1;
			return 1 + children.stream()
					.mapToInt(SlotNode::depth)
					.max()
					.orElse(0);
		}

		/**
		 * Returns the width (number of leaves) of the subtree rooted at this node.
		 */
		public int width() {
			if (children.isEmpty()) return 1;
			return children.stream()
					.mapToInt(SlotNode::width)
					.sum();
		}
	}

	/**
	 * Enumeration of node types in the slot tree.
	 */
	enum SlotNodeType {
		/**
		 * The root node representing the main component.
		 */
		ROOT,

		/**
		 * A structural part of the component (e.g., blade, handle).
		 * These are display-only and not directly editable.
		 */
		STRUCTURE_PART,

		/**
		 * An upgrade slot that can be filled or emptied.
		 * These are the editable slots in the upgrade station.
		 */
		UPGRADE_SLOT
	}
}
