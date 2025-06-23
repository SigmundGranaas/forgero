package com.sigmundgranaas.forgero.tag.engine;

import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.tags.api.Taggable;
import com.sigmundgranaas.forgero.core.tags.engine.TagGraph;
import com.sigmundgranaas.forgero.core.tags.engine.TagGraphBuilder;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TagGraphTest {
	private static final OpenIdentifier SWORD_BLADE = new OpenIdentifier("forgero", "sword_blade");
	private static final OpenIdentifier WEAPON_HEAD = new OpenIdentifier("forgero", "weapon_head");
	private static final OpenIdentifier PART = new OpenIdentifier("forgero", "part");
	private static final OpenIdentifier PICKAXE_HEAD = new OpenIdentifier("forgero", "pickaxe_head");

	@Test
	void itemWithDirectTagIsTaggedCorrectly() {
		TagGraph graph = new TagGraph(Map.of());
		Taggable swordSchematic = () -> Set.of(SWORD_BLADE);
		assertTrue(graph.isTagged(swordSchematic, SWORD_BLADE));
		assertFalse(graph.isTagged(swordSchematic, PICKAXE_HEAD));
	}

	@Test
	void itemInheritsSingleParentTag() {
		TagGraphBuilder builder = new TagGraphBuilder();
		builder.add(SWORD_BLADE, Set.of(WEAPON_HEAD));
		TagGraph graph = builder.build();

		Taggable swordSchematic = () -> Set.of(SWORD_BLADE);

		assertTrue(graph.isTagged(swordSchematic, SWORD_BLADE));
		assertTrue(graph.isTagged(swordSchematic, WEAPON_HEAD));
		assertFalse(graph.isTagged(swordSchematic, PART));
	}

	@Test
	void itemInheritsFromMultipleParents() {
		OpenIdentifier magical = new OpenIdentifier("forgero", "magical_item");
		TagGraphBuilder builder = new TagGraphBuilder();
		builder.add(SWORD_BLADE, Set.of(WEAPON_HEAD, magical));
		TagGraph graph = builder.build();

		Taggable item = () -> Set.of(SWORD_BLADE);

		assertTrue(graph.isTagged(item, WEAPON_HEAD));
		assertTrue(graph.isTagged(item, magical));
	}

	@Test
	void isTaggedWorksForTransitiveAndComplexPaths() {
		OpenIdentifier undeadSlaying = new OpenIdentifier("forgero", "undead_slaying");
		OpenIdentifier metaTag = new OpenIdentifier("forgero", "undead_slaying_weapon_head");

		TagGraphBuilder builder = new TagGraphBuilder();
		builder.add(WEAPON_HEAD, Set.of(PART));
		builder.add(metaTag, Set.of(WEAPON_HEAD, undeadSlaying));
		TagGraph graph = builder.build();

		Taggable item = () -> Set.of(metaTag);

		assertTrue(graph.isTagged(item, metaTag));
		assertTrue(graph.isTagged(item, WEAPON_HEAD));
		assertTrue(graph.isTagged(item, undeadSlaying));
		assertTrue(graph.isTagged(item, PART));
	}

	@Test
	void findTaggedFindsItemsViaDirectAndInheritedTags() {
		OpenIdentifier maceHead = new OpenIdentifier("forgero", "mace_head");
		TagGraphBuilder builder = new TagGraphBuilder();
		builder.add(SWORD_BLADE, Set.of(WEAPON_HEAD));
		builder.add(maceHead, Set.of(WEAPON_HEAD));
		TagGraph graph = builder.build();

		Taggable sword = () -> Set.of(SWORD_BLADE);
		Taggable mace = () -> Set.of(maceHead);
		Taggable pickaxe = () -> Set.of(PICKAXE_HEAD);
		Taggable genericHead = () -> Set.of(WEAPON_HEAD);

		List<Taggable> allItems = List.of(sword, mace, pickaxe, genericHead);
		List<Taggable> weaponHeads = graph.findTagged(WEAPON_HEAD, allItems);

		assertEquals(3, weaponHeads.size());
		assertTrue(weaponHeads.containsAll(List.of(sword, mace, genericHead)));
	}

	@Test
	void findDirectlyTaggedFindsOnlyExactMatches() {
		TagGraphBuilder builder = new TagGraphBuilder();
		builder.add(SWORD_BLADE, Set.of(WEAPON_HEAD));
		TagGraph graph = builder.build();

		Taggable sword = () -> Set.of(SWORD_BLADE);
		Taggable genericHead = () -> Set.of(WEAPON_HEAD);
		List<Taggable> allItems = List.of(sword, genericHead);

		List<Taggable> weaponHeads = graph.findDirectlyTagged(WEAPON_HEAD, allItems);

		assertEquals(1, weaponHeads.size());
		assertTrue(weaponHeads.contains(genericHead));
	}

	@Test
	void graphExposesStructureViaPublicApi() {
		OpenIdentifier magical = new OpenIdentifier("forgero", "magical");
		TagGraphBuilder builder = new TagGraphBuilder();
		builder.add(WEAPON_HEAD, Set.of(PART));
		builder.add(SWORD_BLADE, Set.of(WEAPON_HEAD, magical));
		TagGraph graph = builder.build();

		Set<OpenIdentifier> allTags = graph.getAllIdentifiers();
		assertEquals(4, allTags.size());
		assertTrue(allTags.containsAll(Set.of(SWORD_BLADE, WEAPON_HEAD, PART, magical)));

		assertEquals(Set.of(WEAPON_HEAD, magical), graph.getParents(SWORD_BLADE));
		assertEquals(Set.of(PART), graph.getParents(WEAPON_HEAD));
		assertTrue(graph.getParents(PART).isEmpty());
		assertTrue(graph.getParents(new OpenIdentifier("forgero", "nonexistent")).isEmpty());
	}
}
