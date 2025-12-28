package com.sigmundgranaas.forgero.blocks.integration;

import com.sigmundgranaas.forgero.blocks.api.ComponentTreeBuilder;
import com.sigmundgranaas.forgero.blocks.api.ComponentTreeBuilder.SlotNode;
import com.sigmundgranaas.forgero.blocks.api.ComponentTreeBuilder.SlotNodeType;
import com.sigmundgranaas.forgero.blocks.api.ComponentTreeBuilder.SlotTree;
import com.sigmundgranaas.forgero.blocks.common.layout.SlotLayoutEngine;
import com.sigmundgranaas.forgero.blocks.common.layout.SlotLayoutEngine.SlotPosition;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for deep nesting scenarios in the upgrade station.
 * <p>
 * These tests verify that the tree building and layout engine correctly handle
 * hierarchical component structures with multiple levels of nesting.
 * <p>
 * Test structure hierarchy tested:
 * <pre>
 * Tool (ROOT)
 * ├── Head (STRUCTURE_PART)
 * │   ├── gem_slot_1 (UPGRADE_SLOT) → filled with gem → has nested binding_slot
 * │   └── gem_slot_2 (UPGRADE_SLOT) → empty
 * ├── Handle (STRUCTURE_PART)
 * │   └── binding_slot (UPGRADE_SLOT) → filled with binding
 * └── upgrade_slot_1 (UPGRADE_SLOT)
 *     └── nested_gem_slot (UPGRADE_SLOT) → 3rd level nesting
 * </pre>
 */
class DeepNestingTest {

	@Nested
	@DisplayName("Tree Depth Calculations")
	class TreeDepthTests {

		@Test
		@DisplayName("Single level tree has depth 1")
		void singleLevel_depthOne() {
			SlotNode root = createLeafNode("root", SlotNodeType.ROOT);
			SlotTree tree = new SlotTree(root);

			assertEquals(1, tree.root().depth());
		}

		@Test
		@DisplayName("Two level tree has depth 2")
		void twoLevels_depthTwo() {
			SlotNode child = createLeafNode("child", SlotNodeType.UPGRADE_SLOT);
			SlotNode root = createNodeWithChildren("root", SlotNodeType.ROOT, List.of(child));
			SlotTree tree = new SlotTree(root);

			assertEquals(2, tree.root().depth());
			assertEquals(1, child.depth());
		}

		@Test
		@DisplayName("Three level nested tree has depth 3")
		void threeLevels_depthThree() {
			// Level 3: grandchild
			SlotNode grandchild = createLeafNode("grandchild", SlotNodeType.UPGRADE_SLOT);

			// Level 2: child with grandchild
			SlotNode child = createNodeWithChildren("child", SlotNodeType.STRUCTURE_PART, List.of(grandchild));

			// Level 1: root
			SlotNode root = createNodeWithChildren("root", SlotNodeType.ROOT, List.of(child));

			SlotTree tree = new SlotTree(root);

			assertEquals(3, tree.root().depth());
			assertEquals(2, child.depth());
			assertEquals(1, grandchild.depth());
		}

		@Test
		@DisplayName("Complex tree with uneven branches calculates max depth")
		void complexTree_maxDepth() {
			// Branch 1: depth 4 (root → a → b → c)
			SlotNode c = createLeafNode("c", SlotNodeType.UPGRADE_SLOT);
			SlotNode b = createNodeWithChildren("b", SlotNodeType.UPGRADE_SLOT, List.of(c));
			SlotNode a = createNodeWithChildren("a", SlotNodeType.STRUCTURE_PART, List.of(b));

			// Branch 2: depth 2 (root → x)
			SlotNode x = createLeafNode("x", SlotNodeType.UPGRADE_SLOT);

			// Root with both branches
			SlotNode root = createNodeWithChildren("root", SlotNodeType.ROOT, List.of(a, x));

			assertEquals(4, root.depth());
		}
	}

	@Nested
	@DisplayName("Tree Traversal")
	class TreeTraversalTests {

		@Test
		@DisplayName("Traverse visits all nodes in preorder")
		void traverse_preorderAllNodes() {
			SlotNode gc1 = createLeafNode("gc1", SlotNodeType.UPGRADE_SLOT);
			SlotNode gc2 = createLeafNode("gc2", SlotNodeType.UPGRADE_SLOT);
			SlotNode child1 = createNodeWithChildren("child1", SlotNodeType.STRUCTURE_PART, List.of(gc1, gc2));
			SlotNode child2 = createLeafNode("child2", SlotNodeType.UPGRADE_SLOT);
			SlotNode root = createNodeWithChildren("root", SlotNodeType.ROOT, List.of(child1, child2));

			SlotTree tree = new SlotTree(root);
			List<String> visited = new ArrayList<>();
			tree.traverse(node -> visited.add(node.id().name()));

			// Preorder: root, child1, gc1, gc2, child2
			assertEquals(List.of("root", "child1", "gc1", "gc2", "child2"), visited);
		}

		@Test
		@DisplayName("Traverse counts upgrade slots correctly at all levels")
		void traverse_countsUpgradeSlots() {
			// Structure:
			// root (ROOT)
			// ├── part1 (STRUCTURE_PART) - not counted
			// │   ├── upgrade1 (UPGRADE_SLOT) - counted
			// │   └── upgrade2 (UPGRADE_SLOT) - counted
			// └── upgrade3 (UPGRADE_SLOT) - counted
			//     └── nested_upgrade (UPGRADE_SLOT) - counted (nested in upgrade!)

			SlotNode nestedUpgrade = createLeafNode("nested_upgrade", SlotNodeType.UPGRADE_SLOT);
			SlotNode upgrade3 = createNodeWithChildren("upgrade3", SlotNodeType.UPGRADE_SLOT, List.of(nestedUpgrade));
			SlotNode upgrade1 = createLeafNode("upgrade1", SlotNodeType.UPGRADE_SLOT);
			SlotNode upgrade2 = createLeafNode("upgrade2", SlotNodeType.UPGRADE_SLOT);
			SlotNode part1 = createNodeWithChildren("part1", SlotNodeType.STRUCTURE_PART, List.of(upgrade1, upgrade2));
			SlotNode root = createNodeWithChildren("root", SlotNodeType.ROOT, List.of(part1, upgrade3));

			SlotTree tree = new SlotTree(root);

			assertEquals(4, tree.upgradeSlotCount());
		}

		@Test
		@DisplayName("Size counts all nodes including structure parts")
		void size_countsAllNodes() {
			SlotNode upgrade = createLeafNode("upgrade", SlotNodeType.UPGRADE_SLOT);
			SlotNode part = createNodeWithChildren("part", SlotNodeType.STRUCTURE_PART, List.of(upgrade));
			SlotNode root = createNodeWithChildren("root", SlotNodeType.ROOT, List.of(part));

			SlotTree tree = new SlotTree(root);

			assertEquals(3, tree.size());
		}
	}

	@Nested
	@DisplayName("Layout Engine with Deep Nesting")
	class LayoutEngineDeepNestingTests {

		@Test
		@DisplayName("Three level tree positions increase Y with depth")
		void threeLevels_yIncreases() {
			SlotNode grandchild = createLeafNode("grandchild", SlotNodeType.UPGRADE_SLOT);
			SlotNode child = createNodeWithChildren("child", SlotNodeType.UPGRADE_SLOT, List.of(grandchild));
			SlotNode root = createNodeWithChildren("root", SlotNodeType.ROOT, List.of(child));

			SlotTree tree = new SlotTree(root);
			SlotLayoutEngine engine = new SlotLayoutEngine(tree, 80, 20);

			SlotPosition rootPos = engine.getPosition(OpenIdentifier.of("root"));
			SlotPosition childPos = engine.getPosition(OpenIdentifier.of("child"));
			SlotPosition grandchildPos = engine.getPosition(OpenIdentifier.of("grandchild"));

			// Y should increase with each level
			assertTrue(childPos.y() > rootPos.y(), "Child Y should be greater than root Y");
			assertTrue(grandchildPos.y() > childPos.y(), "Grandchild Y should be greater than child Y");

			// Verify spacing
			assertEquals(SlotLayoutEngine.VERTICAL_SPACING, childPos.y() - rootPos.y());
			assertEquals(SlotLayoutEngine.VERTICAL_SPACING, grandchildPos.y() - childPos.y());
		}

		@Test
		@DisplayName("Four level tree has no overlapping slots")
		void fourLevels_noOverlap() {
			// Create a 4-level tree
			SlotNode level4 = createLeafNode("level4", SlotNodeType.UPGRADE_SLOT);
			SlotNode level3 = createNodeWithChildren("level3", SlotNodeType.UPGRADE_SLOT, List.of(level4));
			SlotNode level2 = createNodeWithChildren("level2", SlotNodeType.STRUCTURE_PART, List.of(level3));
			SlotNode root = createNodeWithChildren("root", SlotNodeType.ROOT, List.of(level2));

			SlotTree tree = new SlotTree(root);
			SlotLayoutEngine engine = new SlotLayoutEngine(tree, 80, 20);

			// Collect all positions
			Set<String> positionKeys = new HashSet<>();
			tree.traverse(node -> {
				SlotPosition pos = engine.getPosition(node.id());
				String key = pos.x() + "," + pos.y();
				assertFalse(positionKeys.contains(key),
						"Position " + key + " used by multiple slots (overlap detected)");
				positionKeys.add(key);
			});
		}

		@Test
		@DisplayName("Wide tree with multiple branches at each level")
		void wideTree_branchesNoOverlap() {
			// Create tree with multiple branches:
			// root
			// ├── branch1
			// │   ├── leaf1a
			// │   └── leaf1b
			// ├── branch2
			// │   └── leaf2
			// └── branch3

			SlotNode leaf1a = createLeafNode("leaf1a", SlotNodeType.UPGRADE_SLOT);
			SlotNode leaf1b = createLeafNode("leaf1b", SlotNodeType.UPGRADE_SLOT);
			SlotNode leaf2 = createLeafNode("leaf2", SlotNodeType.UPGRADE_SLOT);
			SlotNode branch1 = createNodeWithChildren("branch1", SlotNodeType.STRUCTURE_PART, List.of(leaf1a, leaf1b));
			SlotNode branch2 = createNodeWithChildren("branch2", SlotNodeType.STRUCTURE_PART, List.of(leaf2));
			SlotNode branch3 = createLeafNode("branch3", SlotNodeType.UPGRADE_SLOT);
			SlotNode root = createNodeWithChildren("root", SlotNodeType.ROOT, List.of(branch1, branch2, branch3));

			SlotTree tree = new SlotTree(root);
			SlotLayoutEngine engine = new SlotLayoutEngine(tree, 80, 20);

			// Verify no overlapping X positions at same Y level
			Map<Integer, Set<Integer>> positionsByY = new HashMap<>();
			tree.traverse(node -> {
				SlotPosition pos = engine.getPosition(node.id());
				positionsByY.computeIfAbsent(pos.y(), k -> new HashSet<>());
				Set<Integer> xPositions = positionsByY.get(pos.y());

				// Check for overlap (slots are 18 pixels wide)
				for (int existingX : xPositions) {
					int distance = Math.abs(pos.x() - existingX);
					assertTrue(distance >= SlotLayoutEngine.SLOT_SIZE,
							"Slots overlap at Y=" + pos.y() + ": X positions " + existingX + " and " + pos.x());
				}
				xPositions.add(pos.x());
			});
		}

		@Test
		@DisplayName("Total height accounts for deepest branch")
		void totalHeight_deepestBranch() {
			// Deep branch: 4 levels below root (a, b, c, d)
			SlotNode d = createLeafNode("d", SlotNodeType.UPGRADE_SLOT);
			SlotNode c = createNodeWithChildren("c", SlotNodeType.UPGRADE_SLOT, List.of(d));
			SlotNode b = createNodeWithChildren("b", SlotNodeType.UPGRADE_SLOT, List.of(c));
			SlotNode a = createNodeWithChildren("a", SlotNodeType.STRUCTURE_PART, List.of(b));

			// Shallow branch: 1 level below root (x)
			SlotNode x = createLeafNode("x", SlotNodeType.UPGRADE_SLOT);

			SlotNode root = createNodeWithChildren("root", SlotNodeType.ROOT, List.of(a, x));

			SlotTree tree = new SlotTree(root);
			SlotLayoutEngine engine = new SlotLayoutEngine(tree, 80, 20);

			// Height should be based on deepest branch
			// 4 levels below root = 4 vertical spacings + slot size
			int expectedHeight = 4 * SlotLayoutEngine.VERTICAL_SPACING + SlotLayoutEngine.SLOT_SIZE;
			assertEquals(expectedHeight, engine.getTotalHeight());
		}
	}

	@Nested
	@DisplayName("Upgrade Slot Filtering")
	class UpgradeSlotFilteringTests {

		@Test
		@DisplayName("isUpgradeSlot correctly identifies upgrade slots")
		void isUpgradeSlot_correctIdentification() {
			SlotNode upgrade = createLeafNode("upgrade", SlotNodeType.UPGRADE_SLOT);
			SlotNode part = createLeafNode("part", SlotNodeType.STRUCTURE_PART);
			SlotNode root = createLeafNode("root", SlotNodeType.ROOT);

			assertTrue(upgrade.isUpgradeSlot());
			assertFalse(part.isUpgradeSlot());
			assertFalse(root.isUpgradeSlot());
		}

		@Test
		@DisplayName("Nested upgrade slots in filled slots are counted")
		void nestedUpgradeSlots_counted() {
			// Scenario: A gem is installed in a slot, and that gem itself has upgrade slots
			// gem_slot (UPGRADE_SLOT) → filled with gem component
			//   └── gem_inner_slot (UPGRADE_SLOT) → slot on the gem for enchantments

			SlotNode gemInnerSlot = createLeafNode("gem_inner_slot", SlotNodeType.UPGRADE_SLOT);
			SlotNode gemSlot = createFilledNode("gem_slot", SlotNodeType.UPGRADE_SLOT, List.of(gemInnerSlot));
			SlotNode root = createNodeWithChildren("root", SlotNodeType.ROOT, List.of(gemSlot));

			SlotTree tree = new SlotTree(root);

			// Both gem_slot and gem_inner_slot should be counted
			assertEquals(2, tree.upgradeSlotCount());
		}
	}

	@Nested
	@DisplayName("Edge Cases")
	class EdgeCaseTests {

		@Test
		@DisplayName("Empty tree handles gracefully")
		void emptyTree_graceful() {
			SlotTree tree = SlotTree.empty();

			assertTrue(tree.isEmpty());
			assertEquals(0, tree.size());
			assertEquals(0, tree.upgradeSlotCount());

			// Traverse should not fail on empty tree
			AtomicInteger count = new AtomicInteger(0);
			tree.traverse(node -> count.incrementAndGet());
			assertEquals(0, count.get());
		}

		@Test
		@DisplayName("Single node tree works correctly")
		void singleNode_works() {
			SlotNode root = createLeafNode("root", SlotNodeType.ROOT);
			SlotTree tree = new SlotTree(root);

			assertEquals(1, tree.size());
			assertEquals(0, tree.upgradeSlotCount()); // ROOT is not an upgrade slot
		}

		@Test
		@DisplayName("Very deep tree (10 levels) calculates correctly")
		void veryDeepTree_works() {
			// Create a chain of 10 nested nodes
			SlotNode current = createLeafNode("level10", SlotNodeType.UPGRADE_SLOT);
			for (int i = 9; i >= 1; i--) {
				current = createNodeWithChildren("level" + i, SlotNodeType.UPGRADE_SLOT, List.of(current));
			}
			SlotNode root = createNodeWithChildren("root", SlotNodeType.ROOT, List.of(current));

			SlotTree tree = new SlotTree(root);

			assertEquals(11, tree.size()); // root + 10 levels
			assertEquals(10, tree.upgradeSlotCount()); // Only upgrade slots, not root
			assertEquals(11, root.depth()); // 11 levels deep
		}

		@Test
		@DisplayName("Wide tree (100 siblings) handles correctly")
		void wideTree_works() {
			List<SlotNode> children = new ArrayList<>();
			for (int i = 0; i < 100; i++) {
				children.add(createLeafNode("slot" + i, SlotNodeType.UPGRADE_SLOT));
			}
			SlotNode root = createNodeWithChildren("root", SlotNodeType.ROOT, children);

			SlotTree tree = new SlotTree(root);

			assertEquals(101, tree.size());
			assertEquals(100, tree.upgradeSlotCount());
			assertEquals(100, root.width()); // Width is number of leaves
		}
	}

	// ========== HELPER METHODS ==========

	private SlotNode createLeafNode(String name, SlotNodeType type) {
		return SlotNode.leaf(
				OpenIdentifier.of(name),
				type,
				Optional.empty(),
				null
		);
	}

	private SlotNode createFilledNode(String name, SlotNodeType type, List<SlotNode> children) {
		// Create a mock component to simulate filled slot
		return new SlotNode(
				OpenIdentifier.of(name),
				type,
				Optional.of(new MockComponent(name)),
				null,
				children
		);
	}

	private SlotNode createNodeWithChildren(String name, SlotNodeType type, List<SlotNode> children) {
		return new SlotNode(
				OpenIdentifier.of(name),
				type,
				Optional.empty(),
				null,
				children
		);
	}

	/**
	 * Minimal mock component for testing filled slots.
	 */
	private static class MockComponent implements com.sigmundgranaas.forgero.core.component.api.Component {
		private final String name;

		MockComponent(String name) {
			this.name = name;
		}

		@Override
		public OpenIdentifier getTypeIdentifier() {
			return OpenIdentifier.of("mock");
		}

		@Override
		public OpenIdentifier id() {
			return OpenIdentifier.of(name);
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
		public com.sigmundgranaas.forgero.core.component.api.Component withProperties(
				java.util.Map<String, java.util.List<?>> newProperties) {
			return this;
		}
	}
}
