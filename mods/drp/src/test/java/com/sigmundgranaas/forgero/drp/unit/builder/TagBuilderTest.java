package com.sigmundgranaas.forgero.drp.unit.builder;

import com.sigmundgranaas.forgero.drp.api.tag.ItemTagBuilder;
import com.sigmundgranaas.forgero.drp.api.tag.BlockTagBuilder;
import com.sigmundgranaas.forgero.drp.api.tag.TagBuilder;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TagBuilder implementations.
 */
class TagBuilderTest {

	@Test
	void testCreateItemTagBuilder() {
		ItemTagBuilder builder = TagBuilder.items("forgero:pickaxes");

		assertNotNull(builder);
		assertEquals("forgero", builder.getId().getNamespace());
		assertEquals("pickaxes", builder.getId().getPath());
		assertEquals("items", builder.getType());
	}

	@Test
	void testCreateBlockTagBuilder() {
		BlockTagBuilder builder = TagBuilder.blocks("minecraft:mineable/pickaxe");

		assertNotNull(builder);
		assertEquals("minecraft", builder.getId().getNamespace());
		assertEquals("mineable/pickaxe", builder.getId().getPath());
		assertEquals("blocks", builder.getType());
	}

	@Test
	void testAddSingleEntry() {
		ItemTagBuilder builder = TagBuilder.items("test:tag")
				.add("minecraft:diamond_pickaxe");

		List<TagBuilder.TagEntry> entries = builder.getEntries();
		assertEquals(1, entries.size());
		assertEquals("minecraft", entries.get(0).id().getNamespace());
		assertEquals("diamond_pickaxe", entries.get(0).id().getPath());
		assertFalse(entries.get(0).isTag());
		assertFalse(entries.get(0).isOptional());
	}

	@Test
	void testAddMultipleEntries() {
		ItemTagBuilder builder = TagBuilder.items("test:tag")
				.add("minecraft:diamond_pickaxe")
				.add("minecraft:iron_pickaxe")
				.add("minecraft:stone_pickaxe");

		assertEquals(3, builder.getEntries().size());
	}

	@Test
	void testAddAllVarargs() {
		ItemTagBuilder builder = TagBuilder.items("test:tag")
				.addAll("minecraft:a", "minecraft:b", "minecraft:c");

		assertEquals(3, builder.getEntries().size());
	}

	@Test
	void testAddAllList() {
		ItemTagBuilder builder = TagBuilder.items("test:tag")
				.addAll(List.of(
						new Identifier("minecraft", "a"),
						new Identifier("minecraft", "b")
				));

		assertEquals(2, builder.getEntries().size());
	}

	@Test
	void testIncludeTag() {
		ItemTagBuilder builder = TagBuilder.items("test:tag")
				.includeTag("minecraft:pickaxes");

		List<TagBuilder.TagEntry> entries = builder.getEntries();
		assertEquals(1, entries.size());
		assertTrue(entries.get(0).isTag());
		assertEquals("minecraft", entries.get(0).id().getNamespace());
		assertEquals("pickaxes", entries.get(0).id().getPath());
	}

	@Test
	void testIncludeTagWithHashPrefix() {
		ItemTagBuilder builder = TagBuilder.items("test:tag")
				.includeTag("#minecraft:pickaxes");

		List<TagBuilder.TagEntry> entries = builder.getEntries();
		assertEquals(1, entries.size());
		assertTrue(entries.get(0).isTag());
		assertEquals("pickaxes", entries.get(0).id().getPath());
	}

	@Test
	void testSetReplace() {
		ItemTagBuilder builder = TagBuilder.items("test:tag")
				.setReplace(true);

		assertTrue(builder.isReplace());
	}

	@Test
	void testDefaultNotReplace() {
		ItemTagBuilder builder = TagBuilder.items("test:tag");

		assertFalse(builder.isReplace());
	}

	@Test
	void testAddOptional() {
		ItemTagBuilder builder = TagBuilder.items("test:tag")
				.addOptional(new Identifier("mod", "maybe_exists"));

		List<TagBuilder.TagEntry> entries = builder.getEntries();
		assertEquals(1, entries.size());
		assertTrue(entries.get(0).isOptional());
		assertFalse(entries.get(0).isTag());
	}

	@Test
	void testAddOptionalString() {
		ItemTagBuilder builder = TagBuilder.items("test:tag")
				.addOptional("mod:maybe_exists");

		List<TagBuilder.TagEntry> entries = builder.getEntries();
		assertEquals(1, entries.size());
		assertTrue(entries.get(0).isOptional());
	}

	@Test
	void testIncludeOptionalTag() {
		ItemTagBuilder builder = TagBuilder.items("test:tag")
				.includeOptionalTag(new Identifier("mod", "maybe_tag"));

		List<TagBuilder.TagEntry> entries = builder.getEntries();
		assertEquals(1, entries.size());
		assertTrue(entries.get(0).isOptional());
		assertTrue(entries.get(0).isTag());
	}

	@Test
	void testFluentChaining() {
		ItemTagBuilder builder = TagBuilder.items("forgero:tools")
				.add("forgero:iron_pickaxe")
				.add("forgero:diamond_pickaxe")
				.includeTag("minecraft:pickaxes")
				.addOptional("mod:custom_tool")
				.setReplace(false);

		assertEquals(4, builder.getEntries().size());
		assertFalse(builder.isReplace());
	}

	@Test
	void testEntriesAreImmutable() {
		ItemTagBuilder builder = TagBuilder.items("test:tag")
				.add("minecraft:item");

		List<TagBuilder.TagEntry> entries = builder.getEntries();
		assertThrows(UnsupportedOperationException.class, () ->
				entries.add(null));
	}
}
