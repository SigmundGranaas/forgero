package com.sigmundgranaas.forgero.model.registry.impl;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;

import com.sigmundgranaas.forgero.model.registry.api.item.ItemModelRegistrationService;
import com.sigmundgranaas.forgero.model.registry.api.item.ItemModelRegistry;
import com.sigmundgranaas.forgero.utility.resource.loader.implementation.ClassPathResourceProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ModelRegistrationServiceTest {
	@Test
	void testRegisterModels() {
		ItemModelRegistry registry = new MapBackedModelRegistry();
		// ClassPathResourceProvider now points to the root asset folder.
		// DefaultModelRegistrationService expects a ResourceProvider, not a specific loader.
		ItemModelRegistrationService service = new DefaultModelRegistrationService(registry, new ClassPathResourceProvider("/assets"));

		// Register models for the 'forgero' namespace.
		// The service will look for models under /assets/forgero/models/
		service.registerModels();

		// Assertions for default models using their file-based IDs:
		assertTrue(registry.find(new OpenIdentifier("forgero", "parts/oak-handle")).isPresent(), "oak-handle model should be present");
		assertTrue(registry.find(new OpenIdentifier("forgero", "parts/iron-pickaxe_head")).isPresent(), "iron-pickaxe_head model should be present");
		assertTrue(registry.find(new OpenIdentifier("forgero", "equipment/iron-pickaxe")).isPresent(), "iron-pickaxe tool model should be present");
		assertTrue(registry.find(new OpenIdentifier("forgero", "parts/leather-binding")).isPresent(), "leather-binding model should be present");
		assertTrue(registry.find(new OpenIdentifier("forgero", "common/empty")).isPresent(), "empty model should be present");

		// Assertions for contextual models using the new lookup method:
		assertTrue(registry.find(new OpenIdentifier("forgero", "iron"), "pickaxe_head_reinforcement").isPresent(), "iron-pickaxe_head_reinforcement model should be present");
		assertTrue(registry.find(new OpenIdentifier("forgero", "iron"), "mastercrafted_pickaxe_head_reinforcement").isPresent(), "iron-mastercrafted_pickaxe_head_reinforcement model should be present");
		assertTrue(registry.find(new OpenIdentifier("forgero", "diamond"), "pickaxe_head_reinforcement").isPresent(), "diamond-pickaxe_head_reinforcement model should be present");
		assertTrue(registry.find(new OpenIdentifier("forgero", "diamond"), "mastercrafted_pickaxe_head_reinforcement").isPresent(), "diamond-mastercrafted_pickaxe_head_reinforcement model should be present");
		assertTrue(registry.find(new OpenIdentifier("forgero", "leather"), "grip").isPresent(), "leather-grip model should be present");
		assertTrue(registry.find(new OpenIdentifier("forgero", "emerald"), "gem").isPresent(), "emerald-gem model should be present");
	}
}
