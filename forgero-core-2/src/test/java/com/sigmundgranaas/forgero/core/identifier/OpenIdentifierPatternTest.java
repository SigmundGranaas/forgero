package com.sigmundgranaas.forgero.core.identifier;

import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.identifier.api.PatternType;
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
		// Name is the part after the last slash, before the extension
		assertTrue(simpleId.matches(PatternType.NAME, "iron_pickaxe"));
		assertTrue(complexId.matches(PatternType.NAME, "diamond_sword"));
		assertTrue(tagId.matches(PatternType.NAME, "metal"));
	}

	@Test
	void matchesFileType() {
		// FILETYPE is the first segment of the path.
		assertTrue(simpleId.matches(PatternType.FILETYPE, "iron_pickaxe"));
		assertTrue(complexId.matches(PatternType.FILETYPE, "textures"));
		assertTrue(tagId.matches(PatternType.FILETYPE, "tags"));
		assertFalse(complexId.matches(PatternType.FILETYPE, "items"));
		assertFalse(complexId.matches(PatternType.FILETYPE, "png"));
	}
}
