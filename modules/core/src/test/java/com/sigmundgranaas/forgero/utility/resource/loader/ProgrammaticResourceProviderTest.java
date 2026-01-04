package com.sigmundgranaas.forgero.utility.resource.loader;

import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceFilter;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourcePath;
import com.sigmundgranaas.forgero.utility.resource.loader.implementation.ProgrammaticResourceProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class ProgrammaticResourceProviderTest {

	private ProgrammaticResourceProvider provider;

	@BeforeEach
	void setUp() {
		provider = new ProgrammaticResourceProvider("test-provider");
	}

	@Test
	void register_staticResource() {
		ResourcePath path = ResourcePath.file("forgero", "materials", "test", "json");
		byte[] content = "{\"name\": \"test\"}".getBytes(StandardCharsets.UTF_8);

		provider.register(path, content);

		Optional<InputStream> stream = provider.read(path);
		assertTrue(stream.isPresent());

		String result = readStream(stream.get());
		assertEquals("{\"name\": \"test\"}", result);
	}

	@Test
	void registerJson_createsJsonResource() {
		String jsonContent = "{\"material\": \"iron\", \"durability\": 100}";
		provider.registerJson("forgero", "materials/iron", jsonContent);

		ResourcePath path = ResourcePath.file("forgero", "materials", "iron", "json");
		Optional<InputStream> stream = provider.read(path);
		assertTrue(stream.isPresent());

		String result = readStream(stream.get());
		assertEquals(jsonContent, result);
	}

	@Test
	void registerDynamic_createsLazyResource() {
		AtomicInteger callCount = new AtomicInteger(0);

		ResourcePath path = ResourcePath.file("forgero", "generated", "counter", "json");
		provider.registerDynamic(path, () -> {
			int count = callCount.incrementAndGet();
			return ("{\"count\": " + count + "}").getBytes(StandardCharsets.UTF_8);
		});

		// First read
		String result1 = readStream(provider.read(path).orElseThrow());
		assertEquals("{\"count\": 1}", result1);

		// Second read - should call supplier again
		String result2 = readStream(provider.read(path).orElseThrow());
		assertEquals("{\"count\": 2}", result2);

		assertEquals(2, callCount.get());
	}

	@Test
	void read_returnsEmptyForUnregistered() {
		ResourcePath path = ResourcePath.file("forgero", "materials", "nonexistent", "json");
		Optional<InputStream> stream = provider.read(path);
		assertFalse(stream.isPresent());
	}

	@Test
	void list_returnsRegisteredResources() {
		provider.registerJson("forgero", "materials/iron", "{}");
		provider.registerJson("forgero", "materials/gold", "{}");
		provider.registerJson("forgero", "shapes/sword", "{}");

		List<ResourcePath> materials = provider.list(
				ResourcePath.directory("forgero", "materials"),
				true,
				ResourceFilter.JSON
		).toList();

		assertEquals(2, materials.size());
		assertTrue(materials.stream().anyMatch(p -> p.fileName().equals("iron")));
		assertTrue(materials.stream().anyMatch(p -> p.fileName().equals("gold")));
	}

	@Test
	void list_respectsFilter() {
		provider.register(ResourcePath.file("forgero", "textures", "sword", "png"),
				new byte[]{0x00});
		provider.registerJson("forgero", "textures/sword", "{}");

		List<ResourcePath> pngOnly = provider.list(
				ResourcePath.directory("forgero", "textures"),
				true,
				ResourceFilter.PNG
		).toList();

		assertEquals(1, pngOnly.size());
		assertEquals("png", pngOnly.get(0).extension());
	}

	@Test
	void exists_checksRegistration() {
		ResourcePath registered = ResourcePath.file("forgero", "materials", "iron", "json");
		ResourcePath notRegistered = ResourcePath.file("forgero", "materials", "gold", "json");

		provider.register(registered, "{}".getBytes(StandardCharsets.UTF_8));

		assertTrue(provider.exists(registered));
		assertFalse(provider.exists(notRegistered));
	}

	@Test
	void priority_returnsConfiguredValue() {
		ProgrammaticResourceProvider highPriority = new ProgrammaticResourceProvider("high", 100);
		ProgrammaticResourceProvider lowPriority = new ProgrammaticResourceProvider("low", 10);

		assertEquals(100, highPriority.priority());
		assertEquals(10, lowPriority.priority());
	}

	@Test
	void name_returnsProviderName() {
		assertEquals("Programmatic[test-provider]", provider.name());
	}

	@Test
	void getNamespaces_includesRegisteredNamespaces() {
		provider.registerJson("forgero", "test/file", "{}");
		provider.registerJson("minecraft", "test/file", "{}");

		assertTrue(provider.getNamespaces().contains("forgero"));
		assertTrue(provider.getNamespaces().contains("minecraft"));
	}

	@Test
	void unregister_removesResource() {
		ResourcePath path = ResourcePath.file("forgero", "materials", "iron", "json");
		provider.register(path, "{}".getBytes(StandardCharsets.UTF_8));

		assertTrue(provider.exists(path));

		provider.unregister(path);

		assertFalse(provider.exists(path));
	}

	@Test
	void clear_removesAllResources() {
		provider.registerJson("forgero", "materials/iron", "{}");
		provider.registerJson("forgero", "materials/gold", "{}");

		assertEquals(2, provider.list(ResourcePath.directory("forgero", "materials"), true, ResourceFilter.ALL).count());

		provider.clear();

		assertEquals(0, provider.list(ResourcePath.directory("forgero", "materials"), true, ResourceFilter.ALL).count());
	}

	private String readStream(InputStream stream) {
		return new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
				.lines()
				.collect(Collectors.joining("\n"));
	}
}
