package com.sigmundgranaas.forgero.utility.resource.loader;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceConverter;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceProvider;
import com.sigmundgranaas.forgero.utility.resource.loader.implementation.ResourceLoader;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourceLoaderTest {

	// A simple DTO for testing purposes.
	record TestResource(OpenIdentifier id, String content) {
	}

	// Mock implementation of ResourceProvider to simulate a file system in memory.
	static class MockResourceProvider implements ResourceProvider {
		private final Map<OpenIdentifier, String> resources;

		public MockResourceProvider(Map<OpenIdentifier, String> resources) {
			this.resources = resources;
		}

		@Override
		public java.util.Set<String> getNamespaces() {
			return resources.keySet().stream()
					.map(OpenIdentifier::namespace)
					.collect(Collectors.toSet());
		}

		@Override
		public Stream<OpenIdentifier> list(OpenIdentifier path, boolean recursive) {
			return resources.keySet().stream()
					.filter(id -> id.namespace().equals(path.namespace()))
					.filter(id -> {
						if (recursive) {
							return id.path().startsWith(path.path());
						} else {
							String parentPath = id.path().substring(0, id.path().lastIndexOf('/'));
							return parentPath.equals(path.path());
						}
					});
		}

		@Override
		public Optional<InputStream> read(OpenIdentifier identifier) {
			String content = resources.get(identifier);
			if (content == null) {
				return Optional.empty();
			}
			return Optional.of(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
		}
	}

	@Test
	void loadsResourcesFromRoot() {
		Map<OpenIdentifier, String> fileSystem = Map.of(
				new OpenIdentifier("forgero", "data/schematics/pickaxe.json"), "pickaxe_content",
				new OpenIdentifier("forgero", "data/schematics/sword.json"), "sword_content"
		);
		ResourceProvider provider = new MockResourceProvider(fileSystem);

		ResourceConverter<TestResource> converter = (stream, id) -> {
			try {
				return Optional.of(new TestResource(id, new String(stream.readAllBytes())));
			} catch (IOException e) {
				return Optional.empty();
			}
		};

		ResourceLoader<TestResource> loader = new ResourceLoader<>(provider, converter);
		OpenIdentifier root = new OpenIdentifier("forgero", "data/schematics");

		List<TestResource> loadedResources = loader.load(root, false).collect(Collectors.toList());

		assertEquals(2, loadedResources.size());
		assertTrue(loadedResources.stream().anyMatch(r -> r.id.path().equals("data/schematics/pickaxe.json") && r.content.equals("pickaxe_content")));
		assertTrue(loadedResources.stream().anyMatch(r -> r.id.path().equals("data/schematics/sword.json") && r.content.equals("sword_content")));
	}

	@Test
	void loadsResourcesRecursively() {
		Map<OpenIdentifier, String> fileSystem = Map.of(
				new OpenIdentifier("forgero", "data/tags/materials/metal.json"), "metal_tag",
				new OpenIdentifier("forgero", "data/tags/materials/wood/oak.json"), "oak_tag",
				new OpenIdentifier("forgero", "data/tags/parts/handle.json"), "handle_tag"
		);
		ResourceProvider provider = new MockResourceProvider(fileSystem);
		ResourceConverter<String> converter = (stream, id) -> Optional.of(id.toString());
		ResourceLoader<String> loader = new ResourceLoader<>(provider, converter);

		OpenIdentifier root = new OpenIdentifier("forgero", "data/tags");
		List<String> loadedResources = loader.load(root, true).sorted().collect(Collectors.toList());

		assertEquals(3, loadedResources.size());
		assertEquals("forgero:data/tags/materials/metal.json", loadedResources.get(0));
		assertEquals("forgero:data/tags/materials/wood/oak.json", loadedResources.get(1));
		assertEquals("forgero:data/tags/parts/handle.json", loadedResources.get(2));
	}

	@Test
	void handlesEmptyDirectory() {
		ResourceProvider provider = new MockResourceProvider(Map.of());
		ResourceConverter<TestResource> converter = (stream, id) -> Optional.empty();
		ResourceLoader<TestResource> loader = new ResourceLoader<>(provider, converter);

		OpenIdentifier root = new OpenIdentifier("forgero", "data/empty");
		long count = loader.load(root, true).count();

		assertEquals(0, count);
	}

	@Test
	void skipsFileIfConverterFails() {
		Map<OpenIdentifier, String> fileSystem = Map.of(
				new OpenIdentifier("forgero", "data/schematics/valid.json"), "valid",
				new OpenIdentifier("forgero", "data/schematics/invalid.json"), "invalid"
		);
		ResourceProvider provider = new MockResourceProvider(fileSystem);

		// This converter will fail for the "invalid" file
		ResourceConverter<String> converter = (stream, id) -> {
			if (id.path().contains("invalid")) {
				throw new RuntimeException("Simulated parsing failure");
			}
			return Optional.of(id.toString());
		};

		ResourceLoader<String> loader = new ResourceLoader<>(provider, converter);
		OpenIdentifier root = new OpenIdentifier("forgero", "data/schematics");

		List<String> loadedResources = loader.load(root, false).collect(Collectors.toList());

		assertEquals(1, loadedResources.size());
		assertEquals("forgero:data/schematics/valid.json", loadedResources.get(0));
	}
}
