package com.sigmundgranaas.forgero.common.tag.engine;

import com.sigmundgranaas.forgero.common.identifier.api.Identifiable;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.tags.api.TagResolver;
import com.sigmundgranaas.forgero.common.tags.api.Taggable;
import com.sigmundgranaas.forgero.common.tags.engine.TagGraphBuilder;
import com.sigmundgranaas.forgero.common.tags.engine.TaggedRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TaggedRegistryTest {

	record TestResource(OpenIdentifier id, Set<OpenIdentifier> tags) implements Identifiable, Taggable {
		@Override
		public Set<OpenIdentifier> getTags() {
			return tags;
		}
	}

	private TagResolver resolver;
	private static final OpenIdentifier METAL = new OpenIdentifier("forgero", "metal");
	private static final OpenIdentifier IRON = new OpenIdentifier("forgero", "iron");
	private static final OpenIdentifier WOOD = new OpenIdentifier("forgero", "wood");
	private static final OpenIdentifier OAK = new OpenIdentifier("forgero", "oak");
	private static final OpenIdentifier PICKAXE = new OpenIdentifier("forgero", "pickaxe");

	@BeforeEach
	void setUp() {
		// Build a tag resolver for our tests
		TagGraphBuilder builder = new TagGraphBuilder();
		builder.add(IRON, Set.of(METAL));
		builder.add(OAK, Set.of(WOOD));
		builder.add(PICKAXE, Set.of());
		resolver = builder.build();
	}

	@Test
	void successfullyBuildsRegistryWithValidItems() {
		var builder = new TaggedRegistry.Builder<TestResource>(resolver);
		var ironPickaxe = new TestResource(new OpenIdentifier("test", "iron_pickaxe"), Set.of(IRON, PICKAXE));
		builder.add(ironPickaxe);
		TaggedRegistry<TestResource> registry = builder.build();

		assertEquals(1, registry.all().size());
		assertTrue(registry.find(ironPickaxe.id).isPresent());
	}

	@Test
	void builderFailsToAddResourceWithNonExistentTag() {
		var builder = new TaggedRegistry.Builder<TestResource>(resolver);
		var invalidTag = new OpenIdentifier("forgero", "non_existent_tag");
		var invalidResource = new TestResource(new OpenIdentifier("test", "invalid"), Set.of(invalidTag));

		var exception = assertThrows(IllegalArgumentException.class, () -> builder.add(invalidResource));
		assertTrue(exception.getMessage().contains("does not exist in the TagResolver"));
	}

	@Test
	void builderFailsToAddDuplicateResource() {
		var builder = new TaggedRegistry.Builder<TestResource>(resolver);
		var id = new OpenIdentifier("test", "duplicate");
		var resource1 = new TestResource(id, Set.of(IRON));
		var resource2 = new TestResource(id, Set.of(WOOD));

		builder.add(resource1);
		assertThrows(IllegalArgumentException.class, () -> builder.add(resource2));
	}

	@Test
	void getDirectlyTaggedFindsDirectlyTaggedResources() {
		var registry = new TaggedRegistry.Builder<TestResource>(resolver)
				.add(new TestResource(new OpenIdentifier("test", "iron_pickaxe"), Set.of(IRON, PICKAXE)))
				.add(new TestResource(new OpenIdentifier("test", "oak_handle"), Set.of(OAK)))
				.build();

		assertEquals(1, registry.getDirectlyTagged(IRON).size());
		assertEquals("test:iron_pickaxe", registry.getDirectlyTagged(IRON).get(0).id().toString());
		assertEquals(1, registry.getDirectlyTagged(PICKAXE).size());
		assertTrue(registry.getDirectlyTagged(METAL).isEmpty(), "getDirectlyTagged should not find inherited tags");
	}

	@Test
	void findByTagFindsInheritedTags() {
		var ironPickaxe = new TestResource(new OpenIdentifier("test", "iron_pickaxe"), Set.of(IRON, PICKAXE));
		var registry = new TaggedRegistry.Builder<TestResource>(resolver)
				.add(ironPickaxe)
				.add(new TestResource(new OpenIdentifier("test", "oak_handle"), Set.of(OAK)))
				.build();

		List<TestResource> metalItems = registry.findByTag(METAL);
		assertEquals(1, metalItems.size());
		assertTrue(metalItems.contains(ironPickaxe));

		List<TestResource> woodItems = registry.findByTag(WOOD);
		assertEquals(1, woodItems.size());
		assertEquals("test:oak_handle", woodItems.get(0).id().toString());
	}

	@Test
	void findByTagReturnsDirectAndInherited() {
		var ironPickaxe = new TestResource(new OpenIdentifier("test", "iron_pickaxe"), Set.of(IRON, PICKAXE));
		var rawMetal = new TestResource(new OpenIdentifier("test", "raw_metal"), Set.of(METAL)); // Directly tagged with parent
		var registry = new TaggedRegistry.Builder<TestResource>(resolver)
				.add(ironPickaxe)
				.add(rawMetal)
				.build();

		List<TestResource> metalItems = registry.findByTag(METAL);
		assertEquals(2, metalItems.size());
		assertTrue(metalItems.containsAll(List.of(ironPickaxe, rawMetal)));
	}

	@Test
	void allReturnsAllItems() {
		var registry = new TaggedRegistry.Builder<TestResource>(resolver)
				.add(new TestResource(new OpenIdentifier("test", "item1"), Set.of(IRON)))
				.add(new TestResource(new OpenIdentifier("test", "item2"), Set.of(OAK)))
				.build();

		assertEquals(2, registry.all().size());
	}

	@Test
	void findReturnsCorrectItem() {
		var item1ID = new OpenIdentifier("test", "item1");
		var registry = new TaggedRegistry.Builder<TestResource>(resolver)
				.add(new TestResource(item1ID, Set.of(IRON)))
				.build();

		Optional<TestResource> found = registry.find(item1ID);
		assertTrue(found.isPresent());
		assertEquals(item1ID, found.get().id());

		assertFalse(registry.find(new OpenIdentifier("test", "nonexistent")).isPresent());
	}
}
