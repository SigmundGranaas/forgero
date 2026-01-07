package com.sigmundgranaas.forgero.utility.resource;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceFilter;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourcePath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ResourcePath Behavior")
class ResourcePathBehaviorTest {

	@Nested
	@DisplayName("Path Construction")
	class PathConstruction {

		@Test
		@DisplayName("constructs file path with all components")
		void constructsFilePathWithAllComponents() {
			ResourcePath path = ResourcePath.file("forgero", "materials/metal", "iron", "json");

			assertEquals("forgero", path.namespace());
			assertEquals("materials/metal", path.directory());
			assertEquals("iron", path.fileName());
			assertEquals("json", path.extension());
		}

		@Test
		@DisplayName("constructs directory path")
		void constructsDirectoryPath() {
			ResourcePath path = ResourcePath.directory("forgero", "materials");

			assertEquals("forgero", path.namespace());
			assertEquals("materials", path.directory());
			assertEquals("", path.fileName());
			assertEquals("", path.extension());
			assertTrue(path.isDirectory());
		}

		@Test
		@DisplayName("parses full path string")
		void parsesFullPathString() {
			ResourcePath path = ResourcePath.parse("forgero:materials/iron.json");

			assertEquals("forgero", path.namespace());
			assertEquals("materials", path.directory());
			assertEquals("iron", path.fileName());
			assertEquals("json", path.extension());
		}

		@Test
		@DisplayName("constructs from OpenIdentifier")
		void constructsFromOpenIdentifier() {
			OpenIdentifier id = new OpenIdentifier("minecraft", "textures/item/diamond.png");
			ResourcePath path = ResourcePath.fromIdentifier(id);

			assertEquals("minecraft", path.namespace());
			assertEquals("textures/item", path.directory());
			assertEquals("diamond", path.fileName());
			assertEquals("png", path.extension());
		}

		@Test
		@DisplayName("normalizes backslashes to forward slashes")
		void normalizesBackslashesToForwardSlashes() {
			ResourcePath path = ResourcePath.file("forgero", "materials\\metal", "iron", "json");

			assertEquals("materials/metal", path.directory());
		}

		@Test
		@DisplayName("removes leading and trailing slashes from directory")
		void removesLeadingAndTrailingSlashesFromDirectory() {
			ResourcePath path = ResourcePath.file("forgero", "/materials/", "iron", "json");

			assertEquals("materials", path.directory());
		}

		@Test
		@DisplayName("throws on empty namespace")
		void throwsOnEmptyNamespace() {
			assertThrows(IllegalArgumentException.class, () -> {
				ResourcePath.file("", "dir", "file", "json");
			});
		}

		@Test
		@DisplayName("throws on null namespace")
		void throwsOnNullNamespace() {
			assertThrows(NullPointerException.class, () -> {
				ResourcePath.file(null, "dir", "file", "json");
			});
		}
	}

	@Nested
	@DisplayName("Path Conversion")
	class PathConversion {

		@Test
		@DisplayName("converts to OpenIdentifier")
		void convertsToOpenIdentifier() {
			ResourcePath path = ResourcePath.file("forgero", "materials", "iron", "json");
			OpenIdentifier id = path.toIdentifier();

			assertEquals("forgero", id.namespace());
			assertEquals("materials/iron.json", id.path());
		}

		@Test
		@DisplayName("fullPath includes all components")
		void fullPathIncludesAllComponents() {
			ResourcePath path = ResourcePath.file("forgero", "a/b/c", "file", "json");

			assertEquals("a/b/c/file.json", path.fullPath());
		}

		@Test
		@DisplayName("fullPath handles empty directory")
		void fullPathHandlesEmptyDirectory() {
			ResourcePath path = ResourcePath.file("forgero", "", "file", "json");

			assertEquals("file.json", path.fullPath());
		}

		@Test
		@DisplayName("fullPath handles empty extension")
		void fullPathHandlesEmptyExtension() {
			ResourcePath path = ResourcePath.file("forgero", "dir", "file", "");

			assertEquals("dir/file", path.fullPath());
		}

		@Test
		@DisplayName("toString includes namespace and path")
		void toStringIncludesNamespaceAndPath() {
			ResourcePath path = ResourcePath.file("forgero", "materials", "iron", "json");

			assertEquals("forgero:materials/iron.json", path.toString());
		}

		@Test
		@DisplayName("fileNameWithExtension returns combined name")
		void fileNameWithExtensionReturnsCombinedName() {
			ResourcePath path = ResourcePath.file("forgero", "dir", "myfile", "txt");

			assertEquals("myfile.txt", path.fileNameWithExtension());
		}

		@Test
		@DisplayName("fileNameWithExtension handles empty extension")
		void fileNameWithExtensionHandlesEmptyExtension() {
			ResourcePath path = ResourcePath.file("forgero", "dir", "myfile", "");

			assertEquals("myfile", path.fileNameWithExtension());
		}
	}

	@Nested
	@DisplayName("Path Queries")
	class PathQueries {

		@Test
		@DisplayName("hasExtension matches case-insensitively")
		void hasExtensionMatchesCaseInsensitively() {
			ResourcePath path = ResourcePath.file("forgero", "dir", "file", "JSON");

			assertTrue(path.hasExtension("json"));
			assertTrue(path.hasExtension("JSON"));
			assertTrue(path.hasExtension("Json"));
		}

		@Test
		@DisplayName("isInDirectory matches exact directory")
		void isInDirectoryMatchesExactDirectory() {
			ResourcePath path = ResourcePath.file("forgero", "materials", "iron", "json");

			assertTrue(path.isInDirectory("materials"));
			assertFalse(path.isInDirectory("schematics"));
		}

		@Test
		@DisplayName("isInDirectory matches parent directories")
		void isInDirectoryMatchesParentDirectories() {
			ResourcePath path = ResourcePath.file("forgero", "a/b/c", "file", "json");

			assertTrue(path.isInDirectory("a"));
			assertTrue(path.isInDirectory("a/b"));
			assertTrue(path.isInDirectory("a/b/c"));
		}

		@Test
		@DisplayName("isInDirectory with empty matches all")
		void isInDirectoryWithEmptyMatchesAll() {
			ResourcePath path = ResourcePath.file("forgero", "deep/nested/dir", "file", "json");

			assertTrue(path.isInDirectory(""));
		}

		@Test
		@DisplayName("isFile returns true for file paths")
		void isFileReturnsTrueForFilePaths() {
			ResourcePath file = ResourcePath.file("forgero", "dir", "file", "json");
			ResourcePath dir = ResourcePath.directory("forgero", "dir");

			assertTrue(file.isFile());
			assertFalse(dir.isFile());
		}

		@Test
		@DisplayName("isDirectory returns true for directory paths")
		void isDirectoryReturnsTrueForDirectoryPaths() {
			ResourcePath file = ResourcePath.file("forgero", "dir", "file", "json");
			ResourcePath dir = ResourcePath.directory("forgero", "dir");

			assertFalse(file.isDirectory());
			assertTrue(dir.isDirectory());
		}

		@Test
		@DisplayName("depth counts directory levels")
		void depthCountsDirectoryLevels() {
			assertEquals(0, ResourcePath.file("forgero", "", "file", "json").depth());
			assertEquals(1, ResourcePath.file("forgero", "a", "file", "json").depth());
			assertEquals(2, ResourcePath.file("forgero", "a/b", "file", "json").depth());
			assertEquals(3, ResourcePath.file("forgero", "a/b/c", "file", "json").depth());
		}
	}

	@Nested
	@DisplayName("Path Manipulation")
	class PathManipulation {

		@Test
		@DisplayName("withNamespace creates new path with changed namespace")
		void withNamespaceCreatesNewPathWithChangedNamespace() {
			ResourcePath original = ResourcePath.file("forgero", "dir", "file", "json");
			ResourcePath modified = original.withNamespace("minecraft");

			assertEquals("minecraft", modified.namespace());
			assertEquals("dir", modified.directory());
			assertEquals("file", modified.fileName());
		}

		@Test
		@DisplayName("withDirectory creates new path with changed directory")
		void withDirectoryCreatesNewPathWithChangedDirectory() {
			ResourcePath original = ResourcePath.file("forgero", "old", "file", "json");
			ResourcePath modified = original.withDirectory("new/path");

			assertEquals("new/path", modified.directory());
			assertEquals("forgero", modified.namespace());
		}

		@Test
		@DisplayName("withFileName creates new path with changed filename")
		void withFileNameCreatesNewPathWithChangedFilename() {
			ResourcePath original = ResourcePath.file("forgero", "dir", "old", "json");
			ResourcePath modified = original.withFileName("new");

			assertEquals("new", modified.fileName());
			assertEquals("dir", modified.directory());
		}

		@Test
		@DisplayName("withExtension creates new path with changed extension")
		void withExtensionCreatesNewPathWithChangedExtension() {
			ResourcePath original = ResourcePath.file("forgero", "dir", "file", "json");
			ResourcePath modified = original.withExtension("yaml");

			assertEquals("yaml", modified.extension());
			assertEquals("file", modified.fileName());
		}

		@Test
		@DisplayName("child creates nested directory path")
		void childCreatesNestedDirectoryPath() {
			ResourcePath parent = ResourcePath.directory("forgero", "materials");
			ResourcePath child = parent.child("metals");

			assertEquals("materials/metals", child.directory());
			assertTrue(child.isDirectory());
		}

		@Test
		@DisplayName("child throws for file paths")
		void childThrowsForFilePaths() {
			ResourcePath file = ResourcePath.file("forgero", "dir", "file", "json");

			assertThrows(IllegalStateException.class, () -> file.child("sub"));
		}

		@Test
		@DisplayName("parent returns parent directory")
		void parentReturnsParentDirectory() {
			ResourcePath path = ResourcePath.file("forgero", "a/b/c", "file", "json");

			ResourcePath parent = path.parent();
			assertEquals("a/b/c", parent.directory());
			assertTrue(parent.isDirectory());

			ResourcePath grandparent = parent.parent();
			assertEquals("a/b", grandparent.directory());
		}

		@Test
		@DisplayName("parent of root returns empty directory")
		void parentOfRootReturnsEmptyDirectory() {
			ResourcePath root = ResourcePath.directory("forgero", "single");

			ResourcePath parent = root.parent();
			assertEquals("", parent.directory());
		}
	}

	@Nested
	@DisplayName("ResourceFilter")
	class ResourceFilterTests {

		@Test
		@DisplayName("JSON filter accepts json files")
		void jsonFilterAcceptsJsonFiles() {
			ResourcePath json = ResourcePath.file("forgero", "dir", "file", "json");
			ResourcePath png = ResourcePath.file("forgero", "dir", "file", "png");

			assertTrue(ResourceFilter.JSON.test(json));
			assertFalse(ResourceFilter.JSON.test(png));
		}

		@Test
		@DisplayName("PNG filter accepts png files")
		void pngFilterAcceptsPngFiles() {
			ResourcePath png = ResourcePath.file("forgero", "dir", "file", "png");
			ResourcePath json = ResourcePath.file("forgero", "dir", "file", "json");

			assertTrue(ResourceFilter.PNG.test(png));
			assertFalse(ResourceFilter.PNG.test(json));
		}

		@Test
		@DisplayName("ALL filter accepts everything")
		void allFilterAcceptsEverything() {
			assertTrue(ResourceFilter.ALL.test(ResourcePath.file("a", "b", "c", "d")));
			assertTrue(ResourceFilter.ALL.test(ResourcePath.directory("x", "y")));
		}

		@Test
		@DisplayName("NONE filter rejects everything")
		void noneFilterRejectsEverything() {
			assertFalse(ResourceFilter.NONE.test(ResourcePath.file("a", "b", "c", "d")));
			assertFalse(ResourceFilter.NONE.test(ResourcePath.directory("x", "y")));
		}

		@Test
		@DisplayName("extensions filter accepts multiple extensions")
		void extensionsFilterAcceptsMultipleExtensions() {
			ResourceFilter filter = ResourceFilter.extensions("json", "yaml", "yml");

			assertTrue(filter.test(ResourcePath.file("n", "d", "f", "json")));
			assertTrue(filter.test(ResourcePath.file("n", "d", "f", "yaml")));
			assertTrue(filter.test(ResourcePath.file("n", "d", "f", "yml")));
			assertFalse(filter.test(ResourcePath.file("n", "d", "f", "png")));
		}

		@Test
		@DisplayName("extensions with empty array returns NONE")
		void extensionsWithEmptyArrayReturnsNone() {
			ResourceFilter filter = ResourceFilter.extensions();

			assertFalse(filter.test(ResourcePath.file("n", "d", "f", "json")));
		}

		@Test
		@DisplayName("extension filter matches single extension")
		void extensionFilterMatchesSingleExtension() {
			ResourceFilter filter = ResourceFilter.extension("txt");

			assertTrue(filter.test(ResourcePath.file("n", "d", "f", "txt")));
			assertFalse(filter.test(ResourcePath.file("n", "d", "f", "json")));
		}

		@Test
		@DisplayName("glob filter matches patterns")
		void globFilterMatchesPatterns() {
			ResourceFilter filter = ResourceFilter.glob("*.json");

			assertTrue(filter.test(ResourcePath.file("n", "", "file", "json")));
			assertFalse(filter.test(ResourcePath.file("n", "", "file", "png")));
		}

		@Test
		@DisplayName("inDirectory filter matches exact directory")
		void inDirectoryFilterMatchesExactDirectory() {
			ResourceFilter filter = ResourceFilter.inDirectory("materials");

			assertTrue(filter.test(ResourcePath.file("n", "materials", "iron", "json")));
			assertFalse(filter.test(ResourcePath.file("n", "materials/metal", "iron", "json")));
		}

		@Test
		@DisplayName("inDirectoryRecursive filter matches subdirectories")
		void inDirectoryRecursiveFilterMatchesSubdirectories() {
			ResourceFilter filter = ResourceFilter.inDirectoryRecursive("materials");

			assertTrue(filter.test(ResourcePath.file("n", "materials", "iron", "json")));
			assertTrue(filter.test(ResourcePath.file("n", "materials/metal", "iron", "json")));
			assertFalse(filter.test(ResourcePath.file("n", "other", "iron", "json")));
		}

		@Test
		@DisplayName("fileNameStartsWith filter matches prefix")
		void fileNameStartsWithFilterMatchesPrefix() {
			ResourceFilter filter = ResourceFilter.fileNameStartsWith("iron");

			assertTrue(filter.test(ResourcePath.file("n", "d", "iron_sword", "json")));
			assertTrue(filter.test(ResourcePath.file("n", "d", "iron", "json")));
			assertFalse(filter.test(ResourcePath.file("n", "d", "gold_sword", "json")));
		}

		@Test
		@DisplayName("fileNameEndsWith filter matches suffix")
		void fileNameEndsWithFilterMatchesSuffix() {
			ResourceFilter filter = ResourceFilter.fileNameEndsWith("_sword");

			assertTrue(filter.test(ResourcePath.file("n", "d", "iron_sword", "json")));
			assertFalse(filter.test(ResourcePath.file("n", "d", "iron_pickaxe", "json")));
		}

		@Test
		@DisplayName("and combines filters with AND logic")
		void andCombinesFiltersWithAndLogic() {
			ResourceFilter jsonInMaterials = ResourceFilter.JSON
					.and(ResourceFilter.inDirectory("materials"));

			assertTrue(jsonInMaterials.test(ResourcePath.file("n", "materials", "iron", "json")));
			assertFalse(jsonInMaterials.test(ResourcePath.file("n", "other", "iron", "json")));
			assertFalse(jsonInMaterials.test(ResourcePath.file("n", "materials", "iron", "png")));
		}

		@Test
		@DisplayName("or combines filters with OR logic")
		void orCombinesFiltersWithOrLogic() {
			ResourceFilter jsonOrPng = ResourceFilter.JSON.or(ResourceFilter.PNG);

			assertTrue(jsonOrPng.test(ResourcePath.file("n", "d", "f", "json")));
			assertTrue(jsonOrPng.test(ResourcePath.file("n", "d", "f", "png")));
			assertFalse(jsonOrPng.test(ResourcePath.file("n", "d", "f", "txt")));
		}

		@Test
		@DisplayName("negate inverts filter result")
		void negateInvertsFilterResult() {
			ResourceFilter notJson = ResourceFilter.JSON.negate();

			assertFalse(notJson.test(ResourcePath.file("n", "d", "f", "json")));
			assertTrue(notJson.test(ResourcePath.file("n", "d", "f", "png")));
		}
	}
}
