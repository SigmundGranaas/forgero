package com.sigmundgranaas.forgero.model.loading.impl;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.model.api.item.CompositeModel;
import com.sigmundgranaas.forgero.model.api.item.EmptyModel;
import com.sigmundgranaas.forgero.model.api.item.Model;
import com.sigmundgranaas.forgero.model.api.item.TextureModel;
import com.sigmundgranaas.forgero.model.registry.api.item.ItemModelRegistry;
import com.sigmundgranaas.forgero.model.registry.impl.MapBackedModelRegistry;
import com.sigmundgranaas.forgero.utility.resource.loader.implementation.ClassPathResourceProvider;
import com.sigmundgranaas.forgero.utility.resource.loader.implementation.ResourceLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FileModelProviderTest {

	private ItemModelRegistry registry;
	private FileModelProvider provider;

	@BeforeEach
	void setUp() {
		registry = new MapBackedModelRegistry();
		// Use filesystem-based provider for tests since classpath directory enumeration is unreliable
		java.nio.file.Path assetsPath = java.nio.file.Paths.get("modules/core/build/resources/test/assets");
		if (!java.nio.file.Files.exists(assetsPath)) {
			// Fallback for different working directories
			assetsPath = java.nio.file.Paths.get("build/resources/test/assets");
		}
		var resourceProvider = new ClassPathResourceProvider(assetsPath);
		provider = new FileModelProvider();

		// Use ResourceLoader to load models, acting as the orchestrator
		ResourceLoader<Model> modelLoader = new ResourceLoader<>(resourceProvider, provider);
		modelLoader.load(new OpenIdentifier("forgero", "forgero_models"), true)
				.forEach(registry::register);
	}

	@Test
	void testLoadAllModels() {
		// Based on the provided directory structure
		assertTrue(registry.models().size() >= 12, "Should load all specified models from the test resources");
	}

	@Test
	void testIronPickaxeModelLoaded() {
		OpenIdentifier id = new OpenIdentifier("forgero", "equipment/iron-pickaxe");
		assertTrue(registry.find(id).isPresent(),
			"Model not found. Registry contains " + registry.models().size() + " models");
		assertInstanceOf(CompositeModel.class, registry.find(id).get());
		CompositeModel model = (CompositeModel) registry.find(id).get();
		assertEquals(3, model.slots().size()); // head, handle, binding
	}

	@Test
	void testContextualModelIsRegisteredCorrectly() {
		OpenIdentifier targetId = new OpenIdentifier("forgero", "iron");
		String context = "pickaxe_head_reinforcement";
		assertTrue(registry.find(targetId, context).isPresent(), "Contextual model for iron reinforcement should be found");
		assertInstanceOf(TextureModel.class, registry.find(targetId, context).get());
	}

	@Test
	void testOakHandleModelLoaded() {
		OpenIdentifier id = new OpenIdentifier("forgero", "parts/oak-handle");
		assertTrue(registry.find(id).isPresent());
		assertInstanceOf(CompositeModel.class, registry.find(id).get());
		CompositeModel model = (CompositeModel) registry.find(id).get();
		assertEquals(1, model.layers().size());
		assertEquals(2, model.slots().size()); // grip_slot, pommel_slot
		assertEquals("forgero:item/oak-handle", model.layers().get(0).texture());
		assertFalse(model.layers().get(0).variants().isEmpty()); // Should have variants for root_tag
	}

	@Test
	void testLeatherBindingUpgradeModelLoaded() {
		// This is a default upgrade, not a contextual one
		OpenIdentifier id = new OpenIdentifier("forgero", "parts/leather-binding");
		assertTrue(registry.find(id).isPresent());
		assertInstanceOf(CompositeModel.class, registry.find(id).get());
		CompositeModel model = (CompositeModel) registry.find(id).get();
		assertEquals("forgero:item/leather-pickaxe_binding", model.layers().get(0).texture());
	}

	@Test
	void testEmptyModelLoaded() {
		OpenIdentifier id = new OpenIdentifier("forgero", "common/empty");
		assertTrue(registry.find(id).isPresent());
		assertInstanceOf(EmptyModel.class, registry.find(id).get());
	}
}
