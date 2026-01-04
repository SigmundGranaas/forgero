package com.sigmundgranaas.forgero.common.tag;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.tags.api.Taggable;
import com.sigmundgranaas.forgero.common.tags.engine.TagGraph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class TagGraphTest {

	private TagGraph graph;
	private OpenIdentifier material, metal, toolMaterial, wood, pickaxe, tool;

	// A simple record implementing Taggable for testing purposes
	private record TaggableItem(Set<OpenIdentifier> tags) implements Taggable {
		@Override
		public Set<OpenIdentifier> getTags() {
			return tags;
		}
	}

	@BeforeEach
	void setUp() {
		// IDs
		material = new OpenIdentifier("forgero", "material");
		metal = new OpenIdentifier("forgero", "metal");
		toolMaterial = new OpenIdentifier("forgero", "tool_material");
		wood = new OpenIdentifier("forgero", "wood");
		pickaxe = new OpenIdentifier("forgero", "pickaxe");
		tool = new OpenIdentifier("forgero", "tool");

		// Hierarchy:
		// material
		//  |- metal
		//  |- tool_material
		// tool
		//  |- pickaxe
		// wood (no parent)

		Map<OpenIdentifier, Set<OpenIdentifier>> parentRelationships = new HashMap<>();
		parentRelationships.put(metal, Set.of(material));
		parentRelationships.put(toolMaterial, Set.of(material));
		parentRelationships.put(pickaxe, Set.of(tool));

		graph = new TagGraph(parentRelationships);
	}

	@Test
	void isTaggedDirectly() {
		Taggable item = new TaggableItem(Set.of(metal));
		assertTrue(graph.isTagged(item, metal));
	}

	@Test
	void isTaggedInherited() {
		Taggable item = new TaggableItem(Set.of(metal));
		assertTrue(graph.isTagged(item, material));
	}

	@Test
	void isNotTaggedSibling() {
		Taggable item = new TaggableItem(Set.of(metal));
		assertFalse(graph.isTagged(item, toolMaterial));
	}

	@Test
	void isNotTaggedUnrelated() {
		Taggable item = new TaggableItem(Set.of(metal));
		assertFalse(graph.isTagged(item, wood));
	}

	@Test
	void isTaggedWithMultipleTags() {
		Taggable item = new TaggableItem(Set.of(pickaxe, metal));
		assertTrue(graph.isTagged(item, tool));
		assertTrue(graph.isTagged(item, material));
		assertTrue(graph.isTagged(item, pickaxe));
		assertFalse(graph.isTagged(item, wood));
	}

	@Test
	void findTaggedReturnsCorrectItems() {
		TaggableItem iron = new TaggableItem(Set.of(metal));
		TaggableItem diamond = new TaggableItem(Set.of(toolMaterial));
		TaggableItem stick = new TaggableItem(Set.of(wood));
		List<TaggableItem> items = List.of(iron, diamond, stick);

		List<TaggableItem> materials = graph.findTagged(material, items);
		assertEquals(2, materials.size());
		assertTrue(materials.contains(iron));
		assertTrue(materials.contains(diamond));

		List<TaggableItem> metals = graph.findTagged(metal, items);
		assertEquals(1, metals.size());
		assertTrue(metals.contains(iron));

		List<TaggableItem> woods = graph.findTagged(wood, items);
		assertEquals(1, woods.size());
		assertTrue(woods.contains(stick));
	}

	@Test
	void findDirectlyTaggedIgnoresInheritance() {
		TaggableItem iron = new TaggableItem(Set.of(metal));
		TaggableItem diamond = new TaggableItem(Set.of(toolMaterial));
		TaggableItem stick = new TaggableItem(Set.of(wood));
		List<TaggableItem> items = List.of(iron, diamond, stick);

		List<TaggableItem> directlyTaggedMaterial = graph.findDirectlyTagged(material, items);
		assertTrue(directlyTaggedMaterial.isEmpty());

		List<TaggableItem> directlyTaggedMetal = graph.findDirectlyTagged(metal, items);
		assertEquals(1, directlyTaggedMetal.size());
		assertTrue(directlyTaggedMetal.contains(iron));
	}

	@Test
	void getAllIdentifiersContainsAllTags() {
		Set<OpenIdentifier> allIds = graph.getAllIdentifiers();
		assertTrue(allIds.contains(material));
		assertTrue(allIds.contains(metal));
		assertTrue(allIds.contains(toolMaterial));
		assertTrue(allIds.contains(pickaxe));
		assertTrue(allIds.contains(tool));
		assertEquals(5, allIds.size());
	}

	@Test
	void getDescendantsFindsAllChildren() {
		Set<OpenIdentifier> materialDescendants = graph.getDescendants(material);
		assertTrue(materialDescendants.containsAll(Set.of(material, metal, toolMaterial)));
		assertEquals(3, materialDescendants.size());

		Set<OpenIdentifier> toolDescendants = graph.getDescendants(tool);
		assertTrue(toolDescendants.containsAll(Set.of(tool, pickaxe)));
		assertEquals(2, toolDescendants.size());

		Set<OpenIdentifier> metalDescendants = graph.getDescendants(metal);
		assertTrue(metalDescendants.contains(metal));
		assertEquals(1, metalDescendants.size());
	}

	@Test
	void getDescendantsForNonExistentTag() {
		Set<OpenIdentifier> woodDescendants = graph.getDescendants(wood);
		assertTrue(woodDescendants.contains(wood));
		assertEquals(1, woodDescendants.size());
	}

	@Test
	void getParentsReturnsCorrectTags() {
		Set<OpenIdentifier> metalParents = graph.getParents(metal);
		assertTrue(metalParents.contains(material));
		assertEquals(1, metalParents.size());

		Set<OpenIdentifier> materialParents = graph.getParents(material);
		assertTrue(materialParents.isEmpty());
	}

	@Test
	void getParentsForNonExistentTag() {
		Set<OpenIdentifier> woodParents = graph.getParents(wood);
		assertTrue(woodParents.isEmpty());
	}

	@Test
	void constructorWithEmptyMap() {
		TagGraph emptyGraph = new TagGraph(Collections.emptyMap());
		assertNotNull(emptyGraph);
		assertTrue(emptyGraph.getAllIdentifiers().isEmpty());
	}

	@Test
	void constructorIsImmutable() {
		Map<OpenIdentifier, Set<OpenIdentifier>> originalMap = new HashMap<>();
		originalMap.put(metal, new HashSet<>(Set.of(material)));
		TagGraph graphFromMutableMap = new TagGraph(originalMap);

		// Modify the original map after creating the graph
		originalMap.get(metal).add(tool);

		// The graph should not have changed
		Set<OpenIdentifier> parents = graphFromMutableMap.getParents(metal);
		assertEquals(1, parents.size());
		assertTrue(parents.contains(material));
		assertFalse(parents.contains(tool));
	}

	// ========== Robustness Tests (Phase 4.3) ==========

	@Test
	void handlesCircularDependencies() {
		// Create a circular dependency: A -> B -> C -> A
		OpenIdentifier tagA = new OpenIdentifier("test", "tag_a");
		OpenIdentifier tagB = new OpenIdentifier("test", "tag_b");
		OpenIdentifier tagC = new OpenIdentifier("test", "tag_c");

		Map<OpenIdentifier, Set<OpenIdentifier>> circular = new HashMap<>();
		circular.put(tagA, Set.of(tagB));
		circular.put(tagB, Set.of(tagC));
		circular.put(tagC, Set.of(tagA)); // Creates cycle

		// Constructor should not throw
		TagGraph circularGraph = new TagGraph(circular);
		assertNotNull(circularGraph);

		// Should be able to get descendants without infinite loop
		Set<OpenIdentifier> descendants = circularGraph.getDescendants(tagA);
		assertFalse(descendants.isEmpty());
		assertTrue(descendants.size() <= 3, "Should not infinitely loop through circular dependency");
	}

	@Test
	void preventsInfiniteLoopsInTraversal() {
		// Create complex graph with potential for infinite traversal
		OpenIdentifier root = new OpenIdentifier("test", "root");
		OpenIdentifier child1 = new OpenIdentifier("test", "child1");
		OpenIdentifier child2 = new OpenIdentifier("test", "child2");
		OpenIdentifier grandchild = new OpenIdentifier("test", "grandchild");

		Map<OpenIdentifier, Set<OpenIdentifier>> relationships = new HashMap<>();
		relationships.put(child1, Set.of(root));
		relationships.put(child2, Set.of(root));
		relationships.put(grandchild, Set.of(child1, child2)); // Diamond shape

		TagGraph diamondGraph = new TagGraph(relationships);

		// Traversal should visit each node only once
		Taggable item = new TaggableItem(Set.of(grandchild));

		// Should correctly identify all tags without infinite loop
		assertTrue(diamondGraph.isTagged(item, grandchild));
		assertTrue(diamondGraph.isTagged(item, child1));
		assertTrue(diamondGraph.isTagged(item, child2));
		assertTrue(diamondGraph.isTagged(item, root));
	}

	@Test
	void handlesLargeGraphs() {
		// Create a large graph with 1000+ tags in a deep hierarchy
		Map<OpenIdentifier, Set<OpenIdentifier>> largeGraph = new HashMap<>();
		OpenIdentifier previous = new OpenIdentifier("test", "root");

		for (int i = 0; i < 1000; i++) {
			OpenIdentifier current = new OpenIdentifier("test", "tag_" + i);
			largeGraph.put(current, Set.of(previous));
			previous = current;
		}

		// Constructor and operations should handle large graph efficiently
		TagGraph bigGraph = new TagGraph(largeGraph);
		// Should have 1000 tags (tag_0 through tag_999) + 1 root = 1001 total,
		// but root may not be included if it has no parents
		assertTrue(bigGraph.getAllIdentifiers().size() >= 1000,
			"Should handle at least 1000 tags efficiently");

		// Should be able to query deep hierarchy
		OpenIdentifier leaf = new OpenIdentifier("test", "tag_999");
		OpenIdentifier root = new OpenIdentifier("test", "root");
		Taggable item = new TaggableItem(Set.of(leaf));

		// Should find tag at root of deep hierarchy without stack overflow
		assertTrue(bigGraph.isTagged(item, root));
	}

	@Test
	void mergeWithConflictingRelationships() {
		// Create first graph: metal -> material
		Map<OpenIdentifier, Set<OpenIdentifier>> graph1 = new HashMap<>();
		graph1.put(metal, Set.of(material));
		TagGraph tagGraph1 = new TagGraph(graph1);

		// Create second graph: metal -> tool (conflicting parent)
		Map<OpenIdentifier, Set<OpenIdentifier>> graph2 = new HashMap<>();
		graph2.put(metal, Set.of(tool));
		TagGraph tagGraph2 = new TagGraph(graph2);

		// Merge graphs
		var merged = tagGraph1.merge(tagGraph2);

		// After merge, metal should have both parents
		Set<OpenIdentifier> metalParents = merged.getParents(metal);
		assertTrue(metalParents.contains(material), "Should preserve original parent");
		assertTrue(metalParents.contains(tool), "Should include merged parent");
		assertEquals(2, metalParents.size());
	}

	@Test
	void visitorSetPreventsDuplicateVisits() {
		// Create a graph where multiple paths lead to the same tag
		OpenIdentifier common = new OpenIdentifier("test", "common");
		OpenIdentifier path1 = new OpenIdentifier("test", "path1");
		OpenIdentifier path2 = new OpenIdentifier("test", "path2");
		OpenIdentifier leaf = new OpenIdentifier("test", "leaf");

		Map<OpenIdentifier, Set<OpenIdentifier>> relationships = new HashMap<>();
		relationships.put(path1, Set.of(common));
		relationships.put(path2, Set.of(common));
		relationships.put(leaf, Set.of(path1, path2)); // Both paths lead to common

		TagGraph multiPathGraph = new TagGraph(relationships);

		// Get descendants should visit common only once
		Set<OpenIdentifier> descendants = multiPathGraph.getDescendants(common);
		assertTrue(descendants.contains(common));
		assertTrue(descendants.contains(path1));
		assertTrue(descendants.contains(path2));
		assertTrue(descendants.contains(leaf));
		assertEquals(4, descendants.size(), "Each tag should be visited exactly once");
	}

	// ========== End Robustness Tests ==========
}
