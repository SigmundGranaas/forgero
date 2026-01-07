package com.sigmundgranaas.forgero.data.pipeline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sigmundgranaas.forgero.common.identifier.api.IdentifierFactory;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.data.loading.api.data.host.HostData;
import com.sigmundgranaas.forgero.data.pipeline.api.ForgeroDataBundle;
import com.sigmundgranaas.forgero.data.pipeline.api.ForgeroDataInitializer;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceProvider;
import com.sigmundgranaas.forgero.utility.resource.loader.implementation.ClassPathResourceProvider;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class ForgeroDataInitializerTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(ForgeroDataInitializerTest.class);

	private static final IdentifierFactory idFactory = new IdentifierFactory.Builder().defaultNamespace("forgero").build();

	public static ResourceProvider testResourceProvider() {
		try {
			// Get the path to the 'data' directory in test resources
			Path dataPath = Paths.get(Objects.requireNonNull(ForgeroDataInitializerTest.class.getResource("/data/")).toURI());
			return new ClassPathResourceProvider(dataPath);
		} catch (URISyntaxException | NullPointerException e) {
			throw new RuntimeException("Could not create test resource provider", e);
		}
	}

	/**
	 * Tests that the ForgeroDataInitializer correctly loads static definitions.
	 *
	 * Note: Template generation (generating iron-pickaxe_head from templates) requires
	 * condition codecs to be registered (e.g., for "forgero:in_slot_type"). In unit tests
	 * without these codecs, templates are parsed but conditions cannot be evaluated,
	 * so only static definitions with host mappings are created.
	 *
	 * Template generation is fully tested via game tests (ComponentSmokeTest) which
	 * have access to the complete Minecraft/mod runtime environment.
	 */
	@Test
	void testInitializeData() {
		var tagMap = new HashMap<OpenIdentifier, java.util.Set<OpenIdentifier>>();
		tagMap.put(idFactory.of("forgero:armor_plate_shape"), new java.util.HashSet<>());
		tagMap.put(idFactory.of("forgero:pickaxe_head_shape"), new java.util.HashSet<>());
		tagMap.put(idFactory.of("forgero:default_pickaxe_head"), new java.util.HashSet<>());
		tagMap.put(idFactory.of("forgero:default_armor_plate"), new java.util.HashSet<>());
		tagMap.put(idFactory.of("forgero:base_shape"), new java.util.HashSet<>());
		tagMap.put(idFactory.of("forgero:mastercrafted"), new java.util.HashSet<>());
		tagMap.put(idFactory.of("forgero:default_handle"), new java.util.HashSet<>());
		tagMap.put(idFactory.of("forgero:handle"), new java.util.HashSet<>());
		tagMap.put(OpenIdentifier.parse("forgero:materials/tool_material"), new java.util.HashSet<>());
		tagMap.put(OpenIdentifier.parse("forgero:materials/metal"), new java.util.HashSet<>());
		tagMap.put(OpenIdentifier.parse("forgero:materials/armor_material"), new java.util.HashSet<>());
		tagMap.put(OpenIdentifier.parse("forgero:parts/handle_type"), new java.util.HashSet<>());
		tagMap.put(OpenIdentifier.parse("forgero:parts/pickaxe_head_type"), new java.util.HashSet<>());
		tagMap.put(OpenIdentifier.parse("forgero:parts/armor_plate_type"), new java.util.HashSet<>());
		tagMap.put(OpenIdentifier.parse("forgero:tools/pickaxe"), new java.util.HashSet<>());
		tagMap.put(OpenIdentifier.parse("forgero:armor/chest_plate"), new java.util.HashSet<>());
		var tagGraph = new com.sigmundgranaas.forgero.common.tags.engine.TagGraph(tagMap);

		// Note: Empty condition codec maps mean template conditions (like in_slot_type)
		// won't be parsed, limiting this test to static definition loading only
		ForgeroDataInitializer.Config config = new ForgeroDataInitializer.Config(
				"forgero", testResourceProvider(), tagGraph,
				new HashMap<>(), new HashMap<>(), new HashMap<>());
		ForgeroDataInitializer initializer = new ForgeroDataInitializer(config);
		ForgeroDataBundle bundle = initializer.getDataBundle();

		assertNotNull(bundle);
		assertNotNull(bundle.componentRegistry());
		assertNotNull(bundle.tagResolver());
		assertNotNull(bundle.hostItemMap());

		// Check for static mapping from iron.json
		OpenIdentifier ironId = idFactory.of("forgero:iron");
		assertTrue(bundle.hostItemMap().containsKey(ironId), "Host map should contain static material 'forgero:iron'");
		HostData ironHostData = bundle.hostItemMap().get(ironId);
		assertNotNull(ironHostData.identifiers());
		assertEquals(2, ironHostData.identifiers().size());
		assertEquals("minecraft:iron_ingot", ironHostData.identifiers().get(0).id().toString());
		assertEquals("c:iron_ingots", ironHostData.identifiers().get(1).id().toString());
		assertNull(ironHostData.create());

		// Check for static part mapping from static_oak_handle.json
		OpenIdentifier staticOakHandleId = idFactory.of("forgero:static_oak_handle");
		assertTrue(bundle.hostItemMap().containsKey(staticOakHandleId), "Host map should contain static part 'forgero:static_oak_handle'");
		HostData staticOakHandleData = bundle.hostItemMap().get(staticOakHandleId);
		assertNotNull(staticOakHandleData.identifiers());
		assertEquals(1, staticOakHandleData.identifiers().size());
		assertEquals("minecraft:stick", staticOakHandleData.identifiers().get(0).id().toString());

		// Check for wooden_handle (the default handle for all tools)
		OpenIdentifier woodenHandleId = idFactory.of("forgero:wooden_handle");
		assertTrue(bundle.hostItemMap().containsKey(woodenHandleId), "Host map should contain static part 'forgero:wooden_handle'");
		HostData woodenHandleData = bundle.hostItemMap().get(woodenHandleId);
		assertNotNull(woodenHandleData.identifiers());
		assertEquals(1, woodenHandleData.identifiers().size());
		assertEquals("minecraft:stick", woodenHandleData.identifiers().get(0).id().toString());

		assertEquals(3, bundle.hostItemMap().size(),
				"Should have 3 static host items (iron material + static_oak_handle + wooden_handle)");
	}
}
