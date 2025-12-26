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
}
