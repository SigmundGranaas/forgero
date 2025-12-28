package com.sigmundgranaas.forgero.blocks.unit;

import com.sigmundgranaas.forgero.blocks.api.ComponentTreeBuilder.SlotNode;
import com.sigmundgranaas.forgero.blocks.api.ComponentTreeBuilder.SlotNodeType;
import com.sigmundgranaas.forgero.blocks.api.ComponentTreeBuilder.SlotTree;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SlotTree and SlotNode.
 */
class SlotTreeTest {

	@Test
	void emptyTree_isEmpty() {
		SlotTree tree = SlotTree.empty();
		assertTrue(tree.isEmpty());
		assertEquals(0, tree.size());
	}

	@Test
	void treeWithRoot_notEmpty() {
		SlotNode root = createNode("root", SlotNodeType.ROOT, Collections.emptyList());
		SlotTree tree = new SlotTree(root);

		assertFalse(tree.isEmpty());
		assertEquals(1, tree.size());
	}

	@Test
	void traverse_visitsAllNodes() {
		SlotNode child1 = createNode("child1", SlotNodeType.UPGRADE_SLOT, Collections.emptyList());
		SlotNode child2 = createNode("child2", SlotNodeType.UPGRADE_SLOT, Collections.emptyList());
		SlotNode root = createNode("root", SlotNodeType.ROOT, List.of(child1, child2));
		SlotTree tree = new SlotTree(root);

		List<String> visited = new ArrayList<>();
		tree.traverse(node -> visited.add(node.id().name()));

		assertEquals(3, visited.size());
		assertTrue(visited.contains("root"));
		assertTrue(visited.contains("child1"));
		assertTrue(visited.contains("child2"));
	}

	@Test
	void traverse_inPreorder() {
		SlotNode grandchild = createNode("grandchild", SlotNodeType.UPGRADE_SLOT, Collections.emptyList());
		SlotNode child = createNode("child", SlotNodeType.UPGRADE_SLOT, List.of(grandchild));
		SlotNode root = createNode("root", SlotNodeType.ROOT, List.of(child));
		SlotTree tree = new SlotTree(root);

		List<String> visited = new ArrayList<>();
		tree.traverse(node -> visited.add(node.id().name()));

		// Pre-order: root, child, grandchild
		assertEquals("root", visited.get(0));
		assertEquals("child", visited.get(1));
		assertEquals("grandchild", visited.get(2));
	}

	@Test
	void upgradeSlotCount_countsOnlyUpgradeSlots() {
		SlotNode upgrade1 = createNode("upgrade1", SlotNodeType.UPGRADE_SLOT, Collections.emptyList());
		SlotNode part = createNode("part", SlotNodeType.STRUCTURE_PART, Collections.emptyList());
		SlotNode upgrade2 = createNode("upgrade2", SlotNodeType.UPGRADE_SLOT, Collections.emptyList());
		SlotNode root = createNode("root", SlotNodeType.ROOT, List.of(upgrade1, part, upgrade2));
		SlotTree tree = new SlotTree(root);

		assertEquals(2, tree.upgradeSlotCount());
	}

	@Test
	void slotNode_isUpgradeSlot() {
		SlotNode upgrade = createNode("upgrade", SlotNodeType.UPGRADE_SLOT, Collections.emptyList());
		SlotNode part = createNode("part", SlotNodeType.STRUCTURE_PART, Collections.emptyList());

		assertTrue(upgrade.isUpgradeSlot());
		assertFalse(part.isUpgradeSlot());
	}

	@Test
	void slotNode_isFilled() {
		SlotNode empty = new SlotNode(
				OpenIdentifier.of("empty"),
				SlotNodeType.UPGRADE_SLOT,
				Optional.empty(),
				null,
				Collections.emptyList()
		);

		// Create a minimal mock component for testing
		Component mockComponent = new MockComponent();
		SlotNode filled = new SlotNode(
				OpenIdentifier.of("filled"),
				SlotNodeType.UPGRADE_SLOT,
				Optional.of(mockComponent),
				null,
				Collections.emptyList()
		);

		assertFalse(empty.isFilled());
		assertTrue(empty.isEmpty());

		assertTrue(filled.isFilled());
		assertFalse(filled.isEmpty());
	}

	/**
	 * Minimal mock implementation of Component for testing.
	 */
	private static class MockComponent implements Component {
		@Override
		public OpenIdentifier getTypeIdentifier() {
			return OpenIdentifier.of("mock");
		}

		@Override
		public OpenIdentifier id() {
			return OpenIdentifier.of("mock_component");
		}

		@Override
		public java.util.Set<OpenIdentifier> getTags() {
			return java.util.Collections.emptySet();
		}

		@Override
		public java.util.Map<String, java.util.List<?>> propertiesAsMap() {
			return java.util.Collections.emptyMap();
		}

		@Override
		public Component withProperties(java.util.Map<String, java.util.List<?>> newProperties) {
			return this;
		}
	}

	@Test
	void slotNode_depth() {
		SlotNode grandchild = createNode("gc", SlotNodeType.UPGRADE_SLOT, Collections.emptyList());
		SlotNode child = createNode("c", SlotNodeType.UPGRADE_SLOT, List.of(grandchild));
		SlotNode root = createNode("r", SlotNodeType.ROOT, List.of(child));

		assertEquals(3, root.depth());
		assertEquals(2, child.depth());
		assertEquals(1, grandchild.depth());
	}

	@Test
	void slotNode_width() {
		SlotNode c1 = createNode("c1", SlotNodeType.UPGRADE_SLOT, Collections.emptyList());
		SlotNode c2 = createNode("c2", SlotNodeType.UPGRADE_SLOT, Collections.emptyList());
		SlotNode c3 = createNode("c3", SlotNodeType.UPGRADE_SLOT, Collections.emptyList());
		SlotNode root = createNode("r", SlotNodeType.ROOT, List.of(c1, c2, c3));

		assertEquals(3, root.width());
		assertEquals(1, c1.width());
	}

	@Test
	void slotNode_leaf() {
		SlotNode leaf = SlotNode.leaf(
				OpenIdentifier.of("leaf"),
				SlotNodeType.UPGRADE_SLOT,
				Optional.empty(),
				null
		);

		assertTrue(leaf.children().isEmpty());
	}

	// Helper to create test nodes
	private SlotNode createNode(String name, SlotNodeType type, List<SlotNode> children) {
		return new SlotNode(
				OpenIdentifier.of(name),
				type,
				Optional.empty(),
				null,
				children
		);
	}
}
