package com.sigmundgranaas.forgero.identifier;

import com.sigmundgranaas.forgero.core.identifier.PatternType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenIdentifierPatternTest {

	private final OpenIdentifier simpleId = new OpenIdentifier("forgero", "iron_pickaxe");
	private final OpenIdentifier complexId = new OpenIdentifier("minecraft", "textures/items/diamond_sword.png");
	private final OpenIdentifier tagId = new OpenIdentifier("forgero", "tags/materials/metal");

	@Test
	void matchesNamespace() {
		assertTrue(simpleId.matches(PatternType.NAMESPACE, "forgero"));
		assertFalse(simpleId.matches(PatternType.NAMESPACE, "minecraft"));
		assertTrue(complexId.matches(PatternType.NAMESPACE, "minecraft"));
	}

	@Test
	void matchesLocation() {
		assertTrue(simpleId.matches(PatternType.LOCATION, "iron_pickaxe"));
		assertTrue(complexId.matches(PatternType.LOCATION, "textures/items/diamond_sword.png"));
		assertFalse(complexId.matches(PatternType.LOCATION, "textures/items"));
	}

	@Test
	void matchesPath() {
		// ID has no slashes, so its path is empty
		assertTrue(simpleId.matches(PatternType.PATH, ""));

		// Complex ID has a nested path
		assertTrue(complexId.matches(PatternType.PATH, "textures/items"));
		assertFalse(complexId.matches(PatternType.PATH, "textures"));
	}

	@Test
	void matchesName() {
		// Name is the full string because there's no extension
		assertTrue(simpleId.matches(PatternType.NAME, "iron_pickaxe"));
		assertFalse(simpleId.matches(PatternType.NAME, "iron_pickaxe.json"));

		// Name is the part before the extension
		assertTrue(complexId.matches(PatternType.NAME, "diamond_sword"));
		assertFalse(complexId.matches(PatternType.NAME, "diamond_sword.png"));

		// Name for a tag-like ID (no extension)
		assertTrue(tagId.matches(PatternType.NAME, "metal"));
	}

	@Test
	void matchesFileType() {
		// ID has no extension, so filetype is empty
		assertTrue(simpleId.matches(PatternType.FILETYPE, ""));

		// Filetype is "png"
		assertTrue(complexId.matches(PatternType.FILETYPE, "png"));
		assertFalse(complexId.matches(PatternType.FILETYPE, ".png"));
		assertFalse(complexId.matches(PatternType.FILETYPE, ""));

		// Tag-like ID has no extension
		assertTrue(tagId.matches(PatternType.FILETYPE, ""));
	}
}
