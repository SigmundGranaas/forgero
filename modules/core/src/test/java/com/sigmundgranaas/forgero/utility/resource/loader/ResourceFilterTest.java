package com.sigmundgranaas.forgero.utility.resource.loader;

import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceFilter;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourcePath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResourceFilterTest {

	@Test
	void JSON_acceptsJsonFiles() {
		ResourcePath jsonPath = ResourcePath.file("forgero", "materials", "iron", "json");
		ResourcePath pngPath = ResourcePath.file("forgero", "textures", "sword", "png");

		assertTrue(ResourceFilter.JSON.test(jsonPath));
		assertFalse(ResourceFilter.JSON.test(pngPath));
	}

	@Test
	void PNG_acceptsPngFiles() {
		ResourcePath jsonPath = ResourcePath.file("forgero", "materials", "iron", "json");
		ResourcePath pngPath = ResourcePath.file("forgero", "textures", "sword", "png");

		assertFalse(ResourceFilter.PNG.test(jsonPath));
		assertTrue(ResourceFilter.PNG.test(pngPath));
	}

	@Test
	void ALL_acceptsAllFiles() {
		ResourcePath jsonPath = ResourcePath.file("forgero", "materials", "iron", "json");
		ResourcePath pngPath = ResourcePath.file("forgero", "textures", "sword", "png");
		ResourcePath noExt = ResourcePath.file("forgero", "config", "settings", "");

		assertTrue(ResourceFilter.ALL.test(jsonPath));
		assertTrue(ResourceFilter.ALL.test(pngPath));
		assertTrue(ResourceFilter.ALL.test(noExt));
	}

	@Test
	void NONE_rejectsAllFiles() {
		ResourcePath path = ResourcePath.file("forgero", "materials", "iron", "json");

		assertFalse(ResourceFilter.NONE.test(path));
	}

	@Test
	void extensions_acceptsMultipleExtensions() {
		ResourceFilter filter = ResourceFilter.extensions("json", "xml", "yaml");

		assertTrue(filter.test(ResourcePath.file("forgero", "config", "settings", "json")));
		assertTrue(filter.test(ResourcePath.file("forgero", "config", "settings", "xml")));
		assertTrue(filter.test(ResourcePath.file("forgero", "config", "settings", "yaml")));
		assertFalse(filter.test(ResourcePath.file("forgero", "config", "settings", "png")));
	}

	@Test
	void extension_acceptsSingleExtension() {
		ResourceFilter filter = ResourceFilter.extension("mcmeta");

		assertTrue(filter.test(ResourcePath.file("forgero", "pack", "pack", "mcmeta")));
		assertFalse(filter.test(ResourcePath.file("forgero", "config", "settings", "json")));
	}

	@Test
	void inDirectory_matchesExactDirectory() {
		ResourceFilter filter = ResourceFilter.inDirectory("materials");

		assertTrue(filter.test(ResourcePath.file("forgero", "materials", "iron", "json")));
		assertFalse(filter.test(ResourcePath.file("forgero", "materials/metals", "iron", "json")));
		assertFalse(filter.test(ResourcePath.file("forgero", "shapes", "sword", "json")));
	}

	@Test
	void inDirectoryRecursive_matchesSubdirectories() {
		ResourceFilter filter = ResourceFilter.inDirectoryRecursive("materials");

		assertTrue(filter.test(ResourcePath.file("forgero", "materials", "iron", "json")));
		assertTrue(filter.test(ResourcePath.file("forgero", "materials/metals", "iron", "json")));
		assertTrue(filter.test(ResourcePath.file("forgero", "materials/metals/rare", "gold", "json")));
		assertFalse(filter.test(ResourcePath.file("forgero", "shapes", "sword", "json")));
	}

	@Test
	void fileNameStartsWith_matchesPrefix() {
		ResourceFilter filter = ResourceFilter.fileNameStartsWith("iron");

		assertTrue(filter.test(ResourcePath.file("forgero", "materials", "iron", "json")));
		assertTrue(filter.test(ResourcePath.file("forgero", "materials", "iron_ingot", "json")));
		assertFalse(filter.test(ResourcePath.file("forgero", "materials", "gold", "json")));
	}

	@Test
	void fileNameEndsWith_matchesSuffix() {
		ResourceFilter filter = ResourceFilter.fileNameEndsWith("_blade");

		assertTrue(filter.test(ResourcePath.file("forgero", "parts", "sword_blade", "json")));
		assertTrue(filter.test(ResourcePath.file("forgero", "parts", "iron_sword_blade", "json")));
		assertFalse(filter.test(ResourcePath.file("forgero", "parts", "handle", "json")));
	}

	@Test
	void and_combinesFilters() {
		ResourceFilter jsonInMaterials = ResourceFilter.JSON.and(ResourceFilter.inDirectory("materials"));

		assertTrue(jsonInMaterials.test(ResourcePath.file("forgero", "materials", "iron", "json")));
		assertFalse(jsonInMaterials.test(ResourcePath.file("forgero", "materials", "sword", "png")));
		assertFalse(jsonInMaterials.test(ResourcePath.file("forgero", "shapes", "iron", "json")));
	}

	@Test
	void or_combinesFilters() {
		ResourceFilter jsonOrPng = ResourceFilter.JSON.or(ResourceFilter.PNG);

		assertTrue(jsonOrPng.test(ResourcePath.file("forgero", "materials", "iron", "json")));
		assertTrue(jsonOrPng.test(ResourcePath.file("forgero", "textures", "sword", "png")));
		assertFalse(jsonOrPng.test(ResourcePath.file("forgero", "sounds", "hit", "ogg")));
	}

	@Test
	void negate_invertsFilter() {
		ResourceFilter notJson = ResourceFilter.JSON.negate();

		assertFalse(notJson.test(ResourcePath.file("forgero", "materials", "iron", "json")));
		assertTrue(notJson.test(ResourcePath.file("forgero", "textures", "sword", "png")));
	}

	@Test
	void glob_matchesPattern() {
		ResourceFilter filter = ResourceFilter.glob("materials/*.json");

		assertTrue(filter.test(ResourcePath.file("forgero", "materials", "iron", "json")));
		assertFalse(filter.test(ResourcePath.file("forgero", "materials/metals", "iron", "json")));
	}

	@Test
	void regex_matchesPattern() {
		ResourceFilter filter = ResourceFilter.regex("materials/.*\\.json");

		assertTrue(filter.test(ResourcePath.file("forgero", "materials", "iron", "json")));
		assertTrue(filter.test(ResourcePath.file("forgero", "materials/metals", "iron", "json")));
		assertFalse(filter.test(ResourcePath.file("forgero", "shapes", "iron", "json")));
	}
}
