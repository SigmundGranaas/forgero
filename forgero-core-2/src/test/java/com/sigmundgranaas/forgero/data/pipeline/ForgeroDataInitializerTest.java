package com.sigmundgranaas.forgero.data.pipeline;

import com.sigmundgranaas.forgero.common.identifier.api.IdentifierFactory;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.data.loading.api.data.host.HostData;
import com.sigmundgranaas.forgero.data.pipeline.api.ForgeroDataBundle;
import com.sigmundgranaas.forgero.data.pipeline.api.ForgeroDataInitializer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ForgeroDataInitializerTest {

	private static final IdentifierFactory idFactory = new IdentifierFactory.Builder().defaultNamespace("forgero").build();

	@Test
	void testInitializeData() {
		ForgeroDataInitializer initializer = new ForgeroDataInitializer("forgero");
		ForgeroDataBundle bundle = initializer.getDataBundle();

		assertNotNull(bundle);
		assertNotNull(bundle.componentRegistry());
		assertNotNull(bundle.tagGraph());
		assertNotNull(bundle.hostItemMap());

		// Check for static mapping from iron.json
		OpenIdentifier ironId = idFactory.of("forgero:iron");
		assertTrue(bundle.hostItemMap().containsKey(ironId));
		HostData ironHostData = bundle.hostItemMap().get(ironId);
		assertNotNull(ironHostData.identifiers());
		assertEquals(2, ironHostData.identifiers().size());
		assertEquals("minecraft:iron_ingot", ironHostData.identifiers().get(0).id().toString());
		assertEquals("c:iron_ingots", ironHostData.identifiers().get(1).id().toString());
		assertNull(ironHostData.create());

		// Check for static part mapping from static_oak_handle.json
		OpenIdentifier handleId = idFactory.of("forgero:static_oak_handle");
		assertTrue(bundle.hostItemMap().containsKey(handleId));
		HostData handleHostData = bundle.hostItemMap().get(handleId);
		assertNotNull(handleHostData.identifiers());
		assertEquals(1, handleHostData.identifiers().size());
		assertEquals("minecraft:stick", handleHostData.identifiers().get(0).id().toString());

		// Check for generated part mapping from pickaxe_head_template.json
		OpenIdentifier ironPickaxeHeadId = idFactory.of("forgero:iron-pickaxe_head");
		assertTrue(bundle.hostItemMap().containsKey(ironPickaxeHeadId));
		HostData generatedPartHostData = bundle.hostItemMap().get(ironPickaxeHeadId);
		assertNotNull(generatedPartHostData.create());
		assertEquals("forgero:iron-pickaxe_head", generatedPartHostData.create().id().toString());
		assertEquals("forgero:part_item", generatedPartHostData.create().itemClass());

		// Check for generated equipment mapping from pickaxe_template.json
		OpenIdentifier ironPickaxeId = idFactory.of("forgero:iron-pickaxe");
		assertTrue(bundle.hostItemMap().containsKey(ironPickaxeId));
		HostData generatedEquipmentHostData = bundle.hostItemMap().get(ironPickaxeId);
		assertNotNull(generatedEquipmentHostData.create());
		assertEquals("forgero:iron-pickaxe", generatedEquipmentHostData.create().id().toString());
		assertEquals("forgero:tool_item", generatedEquipmentHostData.create().itemClass());
		assertEquals("forgero:tools", generatedEquipmentHostData.create().item_group());

	}
}
