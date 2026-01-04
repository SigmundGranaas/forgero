package com.sigmundgranaas.forgero.utility.resource.loader;

import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceFilter;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourcePath;
import com.sigmundgranaas.forgero.utility.resource.loader.implementation.CompositeResourceProvider;
import com.sigmundgranaas.forgero.utility.resource.loader.implementation.CompositeResourceProvider.ConflictStrategy;
import com.sigmundgranaas.forgero.utility.resource.loader.implementation.ProgrammaticResourceProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class CompositeResourceProviderTest {

	private ProgrammaticResourceProvider lowPriority;
	private ProgrammaticResourceProvider highPriority;

	@BeforeEach
	void setUp() {
		lowPriority = new ProgrammaticResourceProvider("low", 10);
		highPriority = new ProgrammaticResourceProvider("high", 100);
	}

	@Test
	void builder_createsProvider() {
		CompositeResourceProvider composite = CompositeResourceProvider.builder()
				.add(lowPriority)
				.add(highPriority)
				.build();

		assertNotNull(composite);
		assertEquals(2, composite.getProviders().size());
	}

	@Test
	void read_lastWins_usesHigherPriority() {
		ResourcePath path = ResourcePath.file("forgero", "materials", "iron", "json");

		lowPriority.register(path, "{\"source\": \"low\"}".getBytes(StandardCharsets.UTF_8));
		highPriority.register(path, "{\"source\": \"high\"}".getBytes(StandardCharsets.UTF_8));

		CompositeResourceProvider composite = CompositeResourceProvider.builder()
				.add(lowPriority)
				.add(highPriority)
				.withConflictStrategy(ConflictStrategy.LAST_WINS)
				.build();

		String content = readStream(composite.read(path).orElseThrow());
		assertEquals("{\"source\": \"high\"}", content);
	}

	@Test
	void read_alwaysUsesHighestPriority() {
		// Note: ConflictStrategy only affects listing, not reading.
		// Reading always uses highest priority provider.
		ResourcePath path = ResourcePath.file("forgero", "materials", "iron", "json");

		lowPriority.register(path, "{\"source\": \"low\"}".getBytes(StandardCharsets.UTF_8));
		highPriority.register(path, "{\"source\": \"high\"}".getBytes(StandardCharsets.UTF_8));

		CompositeResourceProvider composite = CompositeResourceProvider.builder()
				.add(lowPriority)
				.add(highPriority)
				.withConflictStrategy(ConflictStrategy.FIRST_WINS)
				.build();

		// Even with FIRST_WINS strategy, read() uses highest priority
		String content = readStream(composite.read(path).orElseThrow());
		assertEquals("{\"source\": \"high\"}", content);
	}

	@Test
	void read_fallsBackWhenNotInPrimary() {
		ResourcePath lowOnlyPath = ResourcePath.file("forgero", "materials", "iron", "json");
		ResourcePath highOnlyPath = ResourcePath.file("forgero", "materials", "gold", "json");

		lowPriority.register(lowOnlyPath, "{\"material\": \"iron\"}".getBytes(StandardCharsets.UTF_8));
		highPriority.register(highOnlyPath, "{\"material\": \"gold\"}".getBytes(StandardCharsets.UTF_8));

		CompositeResourceProvider composite = CompositeResourceProvider.builder()
				.add(lowPriority)
				.add(highPriority)
				.build();

		// Both should be found
		assertTrue(composite.read(lowOnlyPath).isPresent());
		assertTrue(composite.read(highOnlyPath).isPresent());
	}

	@Test
	void list_includeAll_returnsBothSources() {
		ResourcePath ironLow = ResourcePath.file("forgero", "materials", "iron_low", "json");
		ResourcePath ironHigh = ResourcePath.file("forgero", "materials", "iron_high", "json");

		lowPriority.register(ironLow, "{}".getBytes(StandardCharsets.UTF_8));
		highPriority.register(ironHigh, "{}".getBytes(StandardCharsets.UTF_8));

		CompositeResourceProvider composite = CompositeResourceProvider.builder()
				.add(lowPriority)
				.add(highPriority)
				.withConflictStrategy(ConflictStrategy.INCLUDE_ALL)
				.build();

		List<ResourcePath> materials = composite.list(
				ResourcePath.directory("forgero", "materials"),
				true,
				ResourceFilter.JSON
		).toList();

		assertEquals(2, materials.size());
	}

	@Test
	void list_lastWins_deduplicatesOnPath() {
		ResourcePath samePath = ResourcePath.file("forgero", "materials", "iron", "json");

		lowPriority.register(samePath, "{\"v\": 1}".getBytes(StandardCharsets.UTF_8));
		highPriority.register(samePath, "{\"v\": 2}".getBytes(StandardCharsets.UTF_8));

		CompositeResourceProvider composite = CompositeResourceProvider.builder()
				.add(lowPriority)
				.add(highPriority)
				.withConflictStrategy(ConflictStrategy.LAST_WINS)
				.build();

		List<ResourcePath> materials = composite.list(
				ResourcePath.directory("forgero", "materials"),
				true,
				ResourceFilter.JSON
		).toList();

		// Should have only one entry (deduplicated)
		assertEquals(1, materials.size());
	}

	@Test
	void getNamespaces_aggregatesFromAllProviders() {
		ProgrammaticResourceProvider forgeroProvider = new ProgrammaticResourceProvider("forgero-only", 10);
		ProgrammaticResourceProvider minecraftProvider = new ProgrammaticResourceProvider("minecraft-only", 20);

		forgeroProvider.registerJson("forgero", "test", "{}");
		minecraftProvider.registerJson("minecraft", "test", "{}");

		CompositeResourceProvider composite = CompositeResourceProvider.builder()
				.add(forgeroProvider)
				.add(minecraftProvider)
				.build();

		assertTrue(composite.getNamespaces().contains("forgero"));
		assertTrue(composite.getNamespaces().contains("minecraft"));
	}

	@Test
	void priority_returnsHighestPriority() {
		CompositeResourceProvider composite = CompositeResourceProvider.builder()
				.add(lowPriority)   // priority 10
				.add(highPriority) // priority 100
				.build();

		assertEquals(100, composite.priority());
	}

	@Test
	void exists_checksAllProviders() {
		ResourcePath inLow = ResourcePath.file("forgero", "materials", "iron", "json");
		ResourcePath inHigh = ResourcePath.file("forgero", "materials", "gold", "json");
		ResourcePath inNeither = ResourcePath.file("forgero", "materials", "platinum", "json");

		lowPriority.register(inLow, "{}".getBytes(StandardCharsets.UTF_8));
		highPriority.register(inHigh, "{}".getBytes(StandardCharsets.UTF_8));

		CompositeResourceProvider composite = CompositeResourceProvider.builder()
				.add(lowPriority)
				.add(highPriority)
				.build();

		assertTrue(composite.exists(inLow));
		assertTrue(composite.exists(inHigh));
		assertFalse(composite.exists(inNeither));
	}

	@Test
	void name_describesComposition() {
		CompositeResourceProvider composite = CompositeResourceProvider.builder()
				.add(lowPriority)
				.add(highPriority)
				.build();

		String name = composite.name();
		assertTrue(name.contains("Composite"));
		// Name includes provider names
		assertTrue(name.contains("low") || name.contains("high"));
	}

	@Test
	void emptyBuilder_throwsException() {
		// CompositeResourceProvider requires at least one provider
		assertThrows(IllegalStateException.class, () -> {
			CompositeResourceProvider.builder().build();
		});
	}

	private String readStream(InputStream stream) {
		return new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
				.lines()
				.collect(Collectors.joining("\n"));
	}
}
