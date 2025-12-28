package com.sigmundgranaas.forgero.blocks.common.layout;

import com.sigmundgranaas.forgero.blocks.api.ComponentTreeBuilder.SlotNode;
import com.sigmundgranaas.forgero.blocks.api.ComponentTreeBuilder.SlotTree;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Calculates slot positions for tree-based UI layouts.
 * <p>
 * This engine takes a {@link SlotTree} and computes 2D positions for each node,
 * creating a visually hierarchical layout where children are positioned below
 * their parent.
 * <p>
 * The algorithm:
 * <ol>
 *   <li>Calculate the width of each subtree (based on leaf count)</li>
 *   <li>Position children horizontally centered under their parent</li>
 *   <li>Distribute siblings evenly across the available width</li>
 * </ol>
 * <p>
 * This replaces the inline layout calculation in the legacy UpgradeStationScreenHandler.
 */
public class SlotLayoutEngine {

	/**
	 * Standard slot size in pixels.
	 */
	public static final int SLOT_SIZE = 18;

	/**
	 * Horizontal spacing between siblings.
	 */
	public static final int HORIZONTAL_SPACING = 4;

	/**
	 * Vertical spacing between rows.
	 */
	public static final int VERTICAL_SPACING = 25;

	/**
	 * Default root X position (center of container).
	 */
	public static final int DEFAULT_ROOT_X = 80;

	/**
	 * Default root Y position.
	 */
	public static final int DEFAULT_ROOT_Y = 20;

	private final SlotTree tree;
	private final Map<OpenIdentifier, SlotPosition> positions;
	private final int rootX;
	private final int rootY;

	/**
	 * A calculated position for a slot.
	 *
	 * @param x X coordinate in pixels
	 * @param y Y coordinate in pixels
	 */
	public record SlotPosition(int x, int y) {
		/**
		 * Zero position (disabled/invisible).
		 */
		public static final SlotPosition ZERO = new SlotPosition(0, 0);
	}

	/**
	 * Creates a layout engine with default root position.
	 *
	 * @param tree The slot tree to layout
	 */
	public SlotLayoutEngine(SlotTree tree) {
		this(tree, DEFAULT_ROOT_X, DEFAULT_ROOT_Y);
	}

	/**
	 * Creates a layout engine with custom root position.
	 *
	 * @param tree  The slot tree to layout
	 * @param rootX X position of the root node
	 * @param rootY Y position of the root node
	 */
	public SlotLayoutEngine(SlotTree tree, int rootX, int rootY) {
		this.tree = tree;
		this.rootX = rootX;
		this.rootY = rootY;
		this.positions = new HashMap<>();
		calculatePositions();
	}

	/**
	 * Calculates positions for all nodes in the tree.
	 */
	private void calculatePositions() {
		if (tree.isEmpty() || tree.root() == null) {
			return;
		}

		// Position root
		SlotNode root = tree.root();
		positions.put(root.id(), new SlotPosition(rootX, rootY));

		// Layout children recursively
		layoutChildren(root, rootX, rootY + VERTICAL_SPACING);
	}

	/**
	 * Recursively layouts children of a node.
	 *
	 * @param parent  The parent node
	 * @param centerX Center X position for children
	 * @param y       Y position for this row
	 */
	private void layoutChildren(SlotNode parent, int centerX, int y) {
		List<SlotNode> children = parent.children();
		if (children.isEmpty()) {
			return;
		}

		// Calculate total width needed for all children
		int totalWidth = calculateTotalWidth(children);

		// Start position (left edge)
		int currentX = centerX - totalWidth / 2;

		for (int i = 0; i < children.size(); i++) {
			SlotNode child = children.get(i);
			int childWidth = calculateWidth(child);
			int childCenterX = currentX + childWidth / 2;

			// Position this child
			positions.put(child.id(), new SlotPosition(childCenterX, y));

			// Layout grandchildren
			layoutChildren(child, childCenterX, y + VERTICAL_SPACING);

			// Move to next child position
			currentX += childWidth;
			if (i < children.size() - 1) {
				currentX += HORIZONTAL_SPACING;
			}
		}
	}

	/**
	 * Calculates the width needed for a node and all its descendants.
	 *
	 * @param node The node to measure
	 * @return Width in pixels
	 */
	private int calculateWidth(SlotNode node) {
		if (node.children().isEmpty()) {
			return SLOT_SIZE;
		}
		return calculateTotalWidth(node.children());
	}

	/**
	 * Calculates total width for a list of sibling nodes.
	 *
	 * @param nodes List of sibling nodes
	 * @return Total width including spacing
	 */
	private int calculateTotalWidth(List<SlotNode> nodes) {
		if (nodes.isEmpty()) {
			return 0;
		}

		int totalWidth = 0;
		for (SlotNode node : nodes) {
			totalWidth += calculateWidth(node);
		}
		// Add spacing between siblings
		totalWidth += (nodes.size() - 1) * HORIZONTAL_SPACING;
		return totalWidth;
	}

	/**
	 * Gets the position for a slot by its ID.
	 *
	 * @param slotId The slot identifier
	 * @return The calculated position, or ZERO if not found
	 */
	public SlotPosition getPosition(OpenIdentifier slotId) {
		return positions.getOrDefault(slotId, SlotPosition.ZERO);
	}

	/**
	 * Gets all calculated positions.
	 *
	 * @return Map of slot ID to position
	 */
	public Map<OpenIdentifier, SlotPosition> getAllPositions() {
		return new HashMap<>(positions);
	}

	/**
	 * Gets the number of positioned nodes.
	 *
	 * @return Number of positions calculated
	 */
	public int size() {
		return positions.size();
	}

	/**
	 * Gets the maximum Y coordinate used.
	 *
	 * @return Maximum Y position, or 0 if no positions
	 */
	public int getMaxY() {
		return positions.values().stream()
				.mapToInt(SlotPosition::y)
				.max()
				.orElse(0);
	}

	/**
	 * Gets the total height used by the layout.
	 *
	 * @return Height from root to deepest node plus slot size
	 */
	public int getTotalHeight() {
		return getMaxY() - rootY + SLOT_SIZE;
	}
}
