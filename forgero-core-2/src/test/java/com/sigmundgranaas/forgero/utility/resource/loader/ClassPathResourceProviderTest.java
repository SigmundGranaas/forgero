package com.sigmundgranaas.forgero.utility.resource.loader;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.utility.resource.loader.implementation.ClassPathResourceProvider;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class ClassPathResourceProviderTest {

	@Test
	void readsExistingDataFile() {
		var provider = new ClassPathResourceProvider("data");
		var identifier = new OpenIdentifier("test-pack", "tags/metal.json");

		Optional<InputStream> streamOpt = provider.read(identifier);
		assertTrue(streamOpt.isPresent());

		String content = new BufferedReader(new InputStreamReader(streamOpt.get(), StandardCharsets.UTF_8))
				.lines()
				.collect(Collectors.joining("\n"));

		assertTrue(content.contains("metal tag"));
	}

	@Test
	void readsExistingAssetsFile() {
		var provider = new ClassPathResourceProvider("assets");
		var identifier = new OpenIdentifier("test-pack", "textures/sword.png");

		Optional<InputStream> streamOpt = provider.read(identifier);
		assertTrue(streamOpt.isPresent(), "The asset file should be found");
	}

	@Test
	void returnsEmptyForNonExistentFile() {
		var provider = new ClassPathResourceProvider("data");
		var identifier = new OpenIdentifier("test-pack", "tags/non_existent.json");
		Optional<InputStream> streamOpt = provider.read(identifier);
		assertFalse(streamOpt.isPresent());
	}

	@Test
	void listsFilesNonRecursively() {
		var provider = new ClassPathResourceProvider("data");
		var path = new OpenIdentifier("test-pack", "tags");

		List<OpenIdentifier> resources = provider.list(path, false).toList();
		assertEquals(1, resources.size());
		assertEquals("test-pack:tags/metal.json", resources.get(0).toString());
	}

	@Test
	void listsFilesRecursively() {
		var provider = new ClassPathResourceProvider("data");
		var path = new OpenIdentifier("test-pack", "tags");

		List<OpenIdentifier> resources = provider.list(path, true)
				.map(OpenIdentifier::toString)
				.sorted()
				.map(s -> new OpenIdentifier(s.split(":")[0], s.split(":")[1]))
				.toList();

		assertEquals(2, resources.size());
		assertEquals("test-pack:tags/metal.json", resources.get(0).toString());
		assertEquals("test-pack:tags/wood/oak.json", resources.get(1).toString());
	}

	@Test
	void listReturnsEmptyForNonExistentPath() {
		var provider = new ClassPathResourceProvider("data");
		var path = new OpenIdentifier("test-pack", "non_existent_path");
		long count = provider.list(path, true).count();
		assertEquals(0, count);
	}
}
