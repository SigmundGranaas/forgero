package com.sigmundgranaas.forgero.utility.resource.loader;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourcePath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResourcePathTest {

	@Test
	void fromIdentifier_parsesSimpleFile() {
		OpenIdentifier id = new OpenIdentifier("forgero", "materials/iron.json");
		ResourcePath path = ResourcePath.fromIdentifier(id);

		assertEquals("forgero", path.namespace());
		assertEquals("materials", path.directory());
		assertEquals("iron", path.fileName());
		assertEquals("json", path.extension());
	}

	@Test
	void fromIdentifier_parsesNestedDirectory() {
		OpenIdentifier id = new OpenIdentifier("minecraft", "textures/item/sword.png");
		ResourcePath path = ResourcePath.fromIdentifier(id);

		assertEquals("minecraft", path.namespace());
		assertEquals("textures/item", path.directory());
		assertEquals("sword", path.fileName());
		assertEquals("png", path.extension());
	}

	@Test
	void fromIdentifier_handlesNoExtension() {
		OpenIdentifier id = new OpenIdentifier("forgero", "materials/iron");
		ResourcePath path = ResourcePath.fromIdentifier(id);

		assertEquals("materials", path.directory());
		assertEquals("iron", path.fileName());
		assertEquals("", path.extension());
	}

	@Test
	void fromIdentifier_handlesRootFile() {
		OpenIdentifier id = new OpenIdentifier("forgero", "config.json");
		ResourcePath path = ResourcePath.fromIdentifier(id);

		assertEquals("", path.directory());
		assertEquals("config", path.fileName());
		assertEquals("json", path.extension());
	}

	@Test
	void directory_createsDirectoryPath() {
		ResourcePath path = ResourcePath.directory("forgero", "materials");

		assertEquals("forgero", path.namespace());
		assertEquals("materials", path.directory());
		assertEquals("", path.fileName());
		assertEquals("", path.extension());
	}

	@Test
	void file_createsFilePath() {
		ResourcePath path = ResourcePath.file("forgero", "materials", "iron", "json");

		assertEquals("forgero", path.namespace());
		assertEquals("materials", path.directory());
		assertEquals("iron", path.fileName());
		assertEquals("json", path.extension());
	}

	@Test
	void fullPath_combinesComponents() {
		ResourcePath path = ResourcePath.file("forgero", "materials", "iron", "json");
		assertEquals("materials/iron.json", path.fullPath());
	}

	@Test
	void fullPath_handlesEmptyDirectory() {
		ResourcePath path = ResourcePath.file("forgero", "", "config", "json");
		assertEquals("config.json", path.fullPath());
	}

	@Test
	void toIdentifier_roundTrips() {
		OpenIdentifier original = new OpenIdentifier("forgero", "materials/iron.json");
		ResourcePath path = ResourcePath.fromIdentifier(original);
		OpenIdentifier result = path.toIdentifier();

		assertEquals(original.namespace(), result.namespace());
		assertEquals(original.path(), result.path());
	}

	@Test
	void hasExtension_matchesCaseInsensitive() {
		ResourcePath path = ResourcePath.file("forgero", "materials", "iron", "JSON");

		assertTrue(path.hasExtension("json"));
		assertTrue(path.hasExtension("JSON"));
		assertTrue(path.hasExtension("Json"));
	}

	@Test
	void withExtension_createsNewPath() {
		ResourcePath path = ResourcePath.file("forgero", "materials", "iron", "json");
		ResourcePath newPath = path.withExtension("xml");

		assertEquals("json", path.extension()); // Original unchanged
		assertEquals("xml", newPath.extension());
		assertEquals("materials/iron.xml", newPath.fullPath());
	}

	@Test
	void isInDirectory_matchesExactDirectory() {
		ResourcePath path = ResourcePath.file("forgero", "materials", "iron", "json");

		assertTrue(path.isInDirectory("materials"));
		assertFalse(path.isInDirectory("shapes"));
	}

	@Test
	void isInDirectory_matchesParentDirectory() {
		ResourcePath path = ResourcePath.file("forgero", "materials/metals", "iron", "json");

		assertTrue(path.isInDirectory("materials"));
		assertTrue(path.isInDirectory("materials/metals"));
		assertFalse(path.isInDirectory("shapes"));
	}

	@Test
	void equality_worksCorrectly() {
		ResourcePath path1 = ResourcePath.file("forgero", "materials", "iron", "json");
		ResourcePath path2 = ResourcePath.file("forgero", "materials", "iron", "json");
		ResourcePath path3 = ResourcePath.file("forgero", "materials", "gold", "json");

		assertEquals(path1, path2);
		assertNotEquals(path1, path3);
		assertEquals(path1.hashCode(), path2.hashCode());
	}
}
