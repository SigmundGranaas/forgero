package com.sigmundgranaas.forgero.renderer;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;

import com.sigmundgranaas.forgero.model.api.ModelRegistrationService;
import com.sigmundgranaas.forgero.model.api.ModelRegistry;
import com.sigmundgranaas.forgero.model.impl.DefaultModelRegistrationService;
import com.sigmundgranaas.forgero.model.impl.MapBackedModelRegistry;
import com.sigmundgranaas.forgero.utility.resource.loader.implementation.ClassPathResourceProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ModelRegistrationServiceTest {
	@Test
	void testRegisterModels() {
		ModelRegistry registry = new MapBackedModelRegistry();
		ModelRegistrationService service = new DefaultModelRegistrationService(registry, new ClassPathResourceProvider("assets"));
		service.registerModels("forgero");

		assertTrue(registry.find(new OpenIdentifier("forgero:oak-handle")).isPresent());
		assertTrue(registry.find(new OpenIdentifier("forgero:iron-pickaxe_head")).isPresent());
		assertTrue(registry.find(new OpenIdentifier("forgero:iron-pickaxe")).isPresent());
		assertTrue(registry.find(new OpenIdentifier("forgero:special_pickaxe_handle")).isPresent());
	}
}
