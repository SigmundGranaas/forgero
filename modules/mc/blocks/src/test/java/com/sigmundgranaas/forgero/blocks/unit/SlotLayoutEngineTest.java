package com.sigmundgranaas.forgero.blocks.unit;

import com.sigmundgranaas.forgero.blocks.api.ComponentTreeBuilder.SlotNode;
import com.sigmundgranaas.forgero.blocks.api.ComponentTreeBuilder.SlotNodeType;
import com.sigmundgranaas.forgero.blocks.api.ComponentTreeBuilder.SlotTree;
import com.sigmundgranaas.forgero.blocks.common.layout.SlotLayoutEngine;
import com.sigmundgranaas.forgero.blocks.common.layout.SlotLayoutEngine.SlotPosition;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the SlotLayoutEngine.
 */
class SlotLayoutEngineTest {

	@Test
	void emptyTree_returnsNoPositions() {
		SlotTree tree = SlotTree.empty();
		SlotLayoutEngine engine = new SlotLayoutEngine(tree);

		assertEquals(0, engine.size());
	}

	@Test
	void singleNode_positionsAtRoot() {
		SlotNode root = createNode("root", Collections.emptyList());
		SlotTree tree = new SlotTree(root);

		SlotLayoutEngine engine = new SlotLayoutEngine(tree, 80, 20);

		SlotPosition pos = engine.getPosition(OpenIdentifier.of("root"));
		assertEquals(80, pos.x());
		assertEquals(20, pos.y());
	}

	@Test
	void twoChildren_centeredUnderParent() {
		SlotNode child1 = createNode("child1", Collections.emptyList());
		SlotNode child2 = createNode("child2", Collections.emptyList());
		SlotNode root = createNode("root", List.of(child1, child2));
		SlotTree tree = new SlotTree(root);

		SlotLayoutEngine engine = new SlotLayoutEngine(tree, 80, 20);

		SlotPosition child1Pos = engine.getPosition(OpenIdentifier.of("child1"));
		SlotPosition child2Pos = engine.getPosition(OpenIdentifier.of("child2"));

		// Children should be at the same Y level
		assertEquals(child1Pos.y(), child2Pos.y());

		// Child1 should be to the left of child2
		assertTrue(child1Pos.x() < child2Pos.x());

		// Children should be centered under parent (80)
		int centerX = (child1Pos.x() + child2Pos.x()) / 2;
		assertTrue(Math.abs(centerX - 80) <= SlotLayoutEngine.SLOT_SIZE);
	}

	@Test
	void deepTree_increasesYWithDepth() {
		SlotNode grandchild = createNode("grandchild", Collections.emptyList());
		SlotNode child = createNode("child", List.of(grandchild));
		SlotNode root = createNode("root", List.of(child));
		SlotTree tree = new SlotTree(root);

		SlotLayoutEngine engine = new SlotLayoutEngine(tree, 80, 20);

		SlotPosition rootPos = engine.getPosition(OpenIdentifier.of("root"));
		SlotPosition childPos = engine.getPosition(OpenIdentifier.of("child"));
		SlotPosition grandchildPos = engine.getPosition(OpenIdentifier.of("grandchild"));

		// Each level should be VERTICAL_SPACING below the previous
		assertEquals(rootPos.y() + SlotLayoutEngine.VERTICAL_SPACING, childPos.y());
		assertEquals(childPos.y() + SlotLayoutEngine.VERTICAL_SPACING, grandchildPos.y());
	}

	@Test
	void noOverlappingSlots() {
		// Create a tree with multiple children at each level
		SlotNode child1 = createNode("child1", Collections.emptyList());
		SlotNode child2 = createNode("child2", Collections.emptyList());
		SlotNode child3 = createNode("child3", Collections.emptyList());
		SlotNode root = createNode("root", List.of(child1, child2, child3));
		SlotTree tree = new SlotTree(root);

		SlotLayoutEngine engine = new SlotLayoutEngine(tree, 80, 20);

		SlotPosition pos1 = engine.getPosition(OpenIdentifier.of("child1"));
		SlotPosition pos2 = engine.getPosition(OpenIdentifier.of("child2"));
		SlotPosition pos3 = engine.getPosition(OpenIdentifier.of("child3"));

		// Check that no slots overlap (minimum distance is SLOT_SIZE + SPACING)
		int minDistance = SlotLayoutEngine.SLOT_SIZE + SlotLayoutEngine.HORIZONTAL_SPACING;

		assertTrue(Math.abs(pos1.x() - pos2.x()) >= SlotLayoutEngine.SLOT_SIZE);
		assertTrue(Math.abs(pos2.x() - pos3.x()) >= SlotLayoutEngine.SLOT_SIZE);
	}

	@Test
	void unknownSlotId_returnsZeroPosition() {
		SlotNode root = createNode("root", Collections.emptyList());
		SlotTree tree = new SlotTree(root);

		SlotLayoutEngine engine = new SlotLayoutEngine(tree);

		SlotPosition pos = engine.getPosition(OpenIdentifier.of("unknown"));
		assertEquals(SlotPosition.ZERO, pos);
	}

	@Test
	void getTotalHeight_calculatesCorrectly() {
		SlotNode grandchild = createNode("grandchild", Collections.emptyList());
		SlotNode child = createNode("child", List.of(grandchild));
		SlotNode root = createNode("root", List.of(child));
		SlotTree tree = new SlotTree(root);

		SlotLayoutEngine engine = new SlotLayoutEngine(tree, 80, 20);

		int expectedHeight = 2 * SlotLayoutEngine.VERTICAL_SPACING + SlotLayoutEngine.SLOT_SIZE;
		assertEquals(expectedHeight, engine.getTotalHeight());
	}

	// Helper to create test nodes
	private SlotNode createNode(String name, List<SlotNode> children) {
		return new SlotNode(
				OpenIdentifier.of(name),
				SlotNodeType.UPGRADE_SLOT,
				Optional.empty(),
				null,
				children
		);
	}
}
