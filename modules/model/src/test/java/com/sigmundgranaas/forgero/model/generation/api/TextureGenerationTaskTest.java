package com.sigmundgranaas.forgero.model.generation.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TextureGenerationTask - data structure for texture generation.
 */
class TextureGenerationTaskTest {

	@Test
	void testCreate_validTask() {
		TextureGenerationTask task = new TextureGenerationTask(
				"forgero:texture_template/part/pickaxe_head",
				"forgero:palette/iron",
				"forgero:item/iron-pickaxe_head"
		);

		assertNotNull(task);
		assertEquals("forgero:texture_template/part/pickaxe_head", task.template());
		assertEquals("forgero:palette/iron", task.palette());
		assertEquals("forgero:item/iron-pickaxe_head", task.output());
	}

	@Test
	void testCreate_nullValues_accepted() {
		// Record allows null values - test that it doesn't throw
		TextureGenerationTask task = new TextureGenerationTask(null, null, null);

		assertNotNull(task);
		assertNull(task.template());
		assertNull(task.palette());
		assertNull(task.output());
	}

	@Test
	void testEquals_sameValues_areEqual() {
		TextureGenerationTask task1 = new TextureGenerationTask(
				"forgero:template/blade",
				"forgero:palette/oak",
				"forgero:item/oak_blade"
		);

		TextureGenerationTask task2 = new TextureGenerationTask(
				"forgero:template/blade",
				"forgero:palette/oak",
				"forgero:item/oak_blade"
		);

		assertEquals(task1, task2);
		assertEquals(task1.hashCode(), task2.hashCode());
	}

	@Test
	void testEquals_differentValues_areNotEqual() {
		TextureGenerationTask task1 = new TextureGenerationTask(
				"forgero:template/blade",
				"forgero:palette/oak",
				"forgero:item/oak_blade"
		);

		TextureGenerationTask task2 = new TextureGenerationTask(
				"forgero:template/blade",
				"forgero:palette/iron",  // Different palette
				"forgero:item/iron_blade"
		);

		assertNotEquals(task1, task2);
	}

	@Test
	void testToString_containsAllFields() {
		TextureGenerationTask task = new TextureGenerationTask(
				"template",
				"palette",
				"output"
		);

		String toString = task.toString();

		assertTrue(toString.contains("template"));
		assertTrue(toString.contains("palette"));
		assertTrue(toString.contains("output"));
	}

	@Test
	void testCreate_complexPaths() {
		TextureGenerationTask task = new TextureGenerationTask(
				"forgero:texture_template/tools/pickaxe/head",
				"forgero:palette/materials/metals/iron",
				"forgero:textures/items/tools/iron_pickaxe_head@2x"
		);

		assertEquals("forgero:texture_template/tools/pickaxe/head", task.template());
		assertEquals("forgero:palette/materials/metals/iron", task.palette());
		assertEquals("forgero:textures/items/tools/iron_pickaxe_head@2x", task.output());
	}

	@Test
	void testCreate_emptyStrings_accepted() {
		TextureGenerationTask task = new TextureGenerationTask("", "", "");

		assertNotNull(task);
		assertEquals("", task.template());
		assertEquals("", task.palette());
		assertEquals("", task.output());
	}

	@Test
	void testRecord_isImmutable() {
		TextureGenerationTask task = new TextureGenerationTask(
				"template",
				"palette",
				"output"
		);

		// Records are immutable - verify accessors return same values
		assertEquals("template", task.template());
		assertEquals("palette", task.palette());
		assertEquals("output", task.output());

		// Create another instance - should be different object but equal
		TextureGenerationTask copy = new TextureGenerationTask(
				task.template(),
				task.palette(),
				task.output()
		);

		assertNotSame(task, copy);
		assertEquals(task, copy);
	}
}
