package com.sigmundgranaas.forgero.data.pipeline;

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

	@Test
	void testInitializeData() {
		ForgeroDataInitializer.Config config = new ForgeroDataInitializer.Config("forgero", testResourceProvider(), new HashMap<>(), new HashMap<>(), new HashMap<>());
		// Updated to use the new constructor with a test-specific ResourceProvider
		ForgeroDataInitializer initializer = new ForgeroDataInitializer(config);
		ForgeroDataBundle bundle = initializer.getDataBundle();

		assertNotNull(bundle);
		assertNotNull(bundle.componentRegistry());
		assertNotNull(bundle.tagGraph());
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
		OpenIdentifier handleId = idFactory.of("forgero:static_oak_handle");
		assertTrue(bundle.hostItemMap().containsKey(handleId), "Host map should contain static part 'forgero:static_oak_handle'");
		HostData handleHostData = bundle.hostItemMap().get(handleId);
		assertNotNull(handleHostData.identifiers());
		assertEquals(1, handleHostData.identifiers().size());
		assertEquals("minecraft:stick", handleHostData.identifiers().get(0).id().toString());

		// Check for generated part mapping from pickaxe_head_template.json
		// This combines "iron" material with "pickaxe_head" shape
		OpenIdentifier ironPickaxeHeadId = idFactory.of("forgero:iron-pickaxe_head");
		assertTrue(bundle.hostItemMap().containsKey(ironPickaxeHeadId), "Host map should contain generated part 'forgero:iron-pickaxe_head'");
		HostData generatedPartHostData = bundle.hostItemMap().get(ironPickaxeHeadId);
		assertNull(generatedPartHostData.identifiers(), "Generated part from template should not have 'identifiers'");
		assertNotNull(generatedPartHostData.create(), "Generated part from template should have 'create' data");
		assertEquals("forgero:iron-pickaxe_head", generatedPartHostData.create().id().toString());
		assertEquals("forgero:part_item", generatedPartHostData.create().itemClass());

		// Check for generated equipment mapping from pickaxe_template.json
		// This combines "iron-pickaxe_head" part with "static_oak_handle" part
		OpenIdentifier ironPickaxeId = idFactory.of("forgero:iron-pickaxe");
		assertTrue(bundle.hostItemMap().containsKey(ironPickaxeId), "Host map should contain generated equipment 'forgero:iron-pickaxe'");
		HostData generatedEquipmentHostData = bundle.hostItemMap().get(ironPickaxeId);
		assertNull(generatedEquipmentHostData.identifiers(), "Generated equipment from template should not have 'identifiers'");
		assertNotNull(generatedEquipmentHostData.create(), "Generated equipment from template should have 'create' data");
		assertEquals("forgero:iron-pickaxe", generatedEquipmentHostData.create().id().toString());
		assertEquals("forgero:pickaxe_item", generatedEquipmentHostData.create().itemClass());
		assertEquals("minecraft:tools", generatedEquipmentHostData.create().itemGroup());
	}
}
