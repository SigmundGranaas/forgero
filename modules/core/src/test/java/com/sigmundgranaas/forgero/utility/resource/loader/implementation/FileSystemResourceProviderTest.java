package com.sigmundgranaas.forgero.utility.resource.loader.implementation;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.ForgeroTest;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceFilter;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourcePath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class FileSystemResourceProviderTest extends ForgeroTest {

	@TempDir
	Path tempDir;

	private FileSystemResourceProvider provider;

	@BeforeEach
	void setUp() throws IOException {
		// Create test directory structure:
		// tempDir/
		//   data/
		//     forgero/
		//       materials/
		//         iron.json
		//         gold.json
		//         wood/oak.json
		//       shapes/
		//         blade.json
		//     minecraft/
		//       items/
		//         stick.json

		Path dataDir = tempDir.resolve("data");
		Path forgeroMaterials = dataDir.resolve("forgero/materials");
		Path forgeroMaterialsWood = forgeroMaterials.resolve("wood");
		Path forgeroShapes = dataDir.resolve("forgero/shapes");
		Path minecraftItems = dataDir.resolve("minecraft/items");

		Files.createDirectories(forgeroMaterials);
		Files.createDirectories(forgeroMaterialsWood);
		Files.createDirectories(forgeroShapes);
		Files.createDirectories(minecraftItems);

		// Create test files
		Files.writeString(forgeroMaterials.resolve("iron.json"), "{\"type\": \"metal\"}");
		Files.writeString(forgeroMaterials.resolve("gold.json"), "{\"type\": \"metal\"}");
		Files.writeString(forgeroMaterialsWood.resolve("oak.json"), "{\"type\": \"wood\"}");
		Files.writeString(forgeroShapes.resolve("blade.json"), "{\"shape\": \"blade\"}");
		Files.writeString(forgeroShapes.resolve("blade.txt"), "text file"); // Non-JSON file
		Files.writeString(minecraftItems.resolve("stick.json"), "{\"item\": \"stick\"}");

		provider = new FileSystemResourceProvider(
				tempDir,
				"data",
				Set.of("forgero", "minecraft"),
				100
		);
	}

	@AfterEach
	void tearDown() {
		// TempDir is automatically cleaned up by JUnit
	}

	@Test
	void loadsFromValidDirectory() {
		// Test listing files in materials directory (non-recursive)
		Stream<ResourcePath> materials = provider.list(
				ResourcePath.directory("forgero", "materials"),
				false,
				ResourceFilter.JSON
		);

		List<ResourcePath> paths = materials.toList();
		assertEquals(2, paths.size(), "Should find 2 JSON files in materials directory (non-recursive)");

		// Verify the paths contain the expected files
		assertTrue(paths.stream().anyMatch(p -> p.fileNameWithExtension().equals("iron.json")));
		assertTrue(paths.stream().anyMatch(p -> p.fileNameWithExtension().equals("gold.json")));
	}

	@Test
	void handlesNonexistentDirectory() {
		// Test listing from a directory that doesn't exist
		Stream<ResourcePath> result = provider.list(
				ResourcePath.directory("forgero", "nonexistent"),
				false,
				ResourceFilter.JSON
		);

		List<ResourcePath> paths = result.toList();
		assertTrue(paths.isEmpty(), "Should return empty stream for nonexistent directory");
	}

	@Test
	void filtersFilesByExtension() {
		// Test that JSON filter only returns .json files
		Stream<ResourcePath> jsonOnly = provider.list(
				ResourcePath.directory("forgero", "shapes"),
				false,
				ResourceFilter.JSON
		);

		List<ResourcePath> paths = jsonOnly.toList();
		assertEquals(1, paths.size(), "Should find only 1 JSON file (blade.json), not blade.txt");
		assertEquals("blade.json", paths.get(0).fileNameWithExtension());

		// Test that ALL filter returns all files
		Stream<ResourcePath> allFiles = provider.list(
				ResourcePath.directory("forgero", "shapes"),
				false,
				ResourceFilter.ALL
		);

		List<ResourcePath> allPaths = allFiles.toList();
		assertEquals(2, allPaths.size(), "Should find 2 files total (blade.json and blade.txt)");
	}

	@Test
	void recursivelyScansSubdirectories() {
		// Test recursive listing in materials directory
		Stream<ResourcePath> materialsRecursive = provider.list(
				ResourcePath.directory("forgero", "materials"),
				true,
				ResourceFilter.JSON
		);

		List<ResourcePath> paths = materialsRecursive.toList();
		assertEquals(3, paths.size(), "Should find 3 JSON files recursively (iron.json, gold.json, wood/oak.json)");

		// Verify nested file is found
		assertTrue(paths.stream().anyMatch(p -> p.fullPath().contains("wood/oak.json")),
				"Should find oak.json in wood subdirectory");
	}

	@Test
	void readsFileContent() throws IOException {
		// Test reading a specific file
		ResourcePath ironPath = ResourcePath.file("forgero", "materials", "iron", "json");
		Optional<InputStream> result = provider.read(ironPath);

		assertTrue(result.isPresent(), "Should successfully read existing file");

		try (InputStream stream = result.get()) {
			String content = new String(stream.readAllBytes());
			assertEquals("{\"type\": \"metal\"}", content);
		}
	}

	@Test
	void readsFileByIdentifier() throws IOException {
		// Test reading using OpenIdentifier
		OpenIdentifier identifier = new OpenIdentifier("forgero", "shapes/blade.json");
		Optional<InputStream> result = provider.read(identifier);

		assertTrue(result.isPresent(), "Should successfully read file by identifier");

		try (InputStream stream = result.get()) {
			String content = new String(stream.readAllBytes());
			assertEquals("{\"shape\": \"blade\"}", content);
		}
	}

	@Test
	void returnsEmptyForNonexistentFile() {
		ResourcePath nonexistent = ResourcePath.file("forgero", "materials", "diamond", "json");
		Optional<InputStream> result = provider.read(nonexistent);

		assertFalse(result.isPresent(), "Should return empty Optional for nonexistent file");
	}

	@Test
	void checksFileExistence() {
		ResourcePath existing = ResourcePath.file("forgero", "materials", "iron", "json");
		assertTrue(provider.exists(existing), "Should return true for existing file");

		ResourcePath nonexistent = ResourcePath.file("forgero", "materials", "diamond", "json");
		assertFalse(provider.exists(nonexistent), "Should return false for nonexistent file");
	}

	@Test
	void providesCorrectMetadata() {
		assertEquals(100, provider.priority(), "Should return configured priority");
		assertEquals(Set.of("forgero", "minecraft"), provider.getNamespaces(), "Should return configured namespaces");
		assertTrue(provider.name().contains(tempDir.toString()), "Name should contain root path");
	}

	@Test
	void handlesMultipleNamespaces() {
		// Test listing from different namespaces
		Stream<ResourcePath> forgeroFiles = provider.list(
				ResourcePath.directory("forgero", "materials"),
				false,
				ResourceFilter.JSON
		);
		assertEquals(2, forgeroFiles.count(), "Should find forgero namespace files");

		Stream<ResourcePath> minecraftFiles = provider.list(
				ResourcePath.directory("minecraft", "items"),
				false,
				ResourceFilter.JSON
		);
		assertEquals(1, minecraftFiles.count(), "Should find minecraft namespace files");
	}

	@Test
	void normalizesTopLevelDirectory() {
		// Test that provider normalizes top-level directory (removes leading/trailing slashes)
		FileSystemResourceProvider providerWithSlashes = new FileSystemResourceProvider(
				tempDir,
				"/data/",
				Set.of("forgero"),
				100
		);

		Stream<ResourcePath> materials = providerWithSlashes.list(
				ResourcePath.directory("forgero", "materials"),
				false,
				ResourceFilter.JSON
		);

		assertEquals(2, materials.count(), "Should work correctly with normalized top-level directory");
	}

	@Test
	void worksWithoutTopLevelDirectory() throws IOException {
		// Create a provider with no top-level directory (namespace dirs directly under root)
		Path flatRoot = tempDir.resolve("flat");
		Path forgeroDir = flatRoot.resolve("forgero/test");
		Files.createDirectories(forgeroDir);
		Files.writeString(forgeroDir.resolve("test.json"), "{}");

		FileSystemResourceProvider flatProvider = new FileSystemResourceProvider(
				flatRoot,
				"",
				Set.of("forgero"),
				100
		);

		Stream<ResourcePath> files = flatProvider.list(
				ResourcePath.directory("forgero", "test"),
				false,
				ResourceFilter.JSON
		);

		assertEquals(1, files.count(), "Should work with empty top-level directory");
	}

	@Test
	void listsIdentifiersCorrectly() {
		// Test the list(OpenIdentifier, boolean) method
		OpenIdentifier materialsDir = new OpenIdentifier("forgero", "materials");
		Stream<OpenIdentifier> identifiers = provider.list(materialsDir, true);

		List<OpenIdentifier> ids = identifiers.toList();
		assertEquals(3, ids.size(), "Should find 3 JSON files as identifiers");

		// Verify identifiers have correct namespace
		assertTrue(ids.stream().allMatch(id -> id.namespace().equals("forgero")));
	}
}
