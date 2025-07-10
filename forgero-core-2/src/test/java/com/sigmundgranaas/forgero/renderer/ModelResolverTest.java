package com.sigmundgranaas.forgero.renderer;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentStructure;
import com.sigmundgranaas.forgero.core.component.api.structure.StructureSlot;
import com.sigmundgranaas.forgero.core.component.impl.StaticComponent;
import com.sigmundgranaas.forgero.core.component.impl.StructuredPart;
import com.sigmundgranaas.forgero.model.api.LayeredTexture;
import com.sigmundgranaas.forgero.model.api.ModelRegistry;
import com.sigmundgranaas.forgero.model.api.ModelResolver;
import com.sigmundgranaas.forgero.model.api.TextureLayer;
import com.sigmundgranaas.forgero.model.impl.DefaultModelRegistrationService;
import com.sigmundgranaas.forgero.model.impl.MapBackedModelRegistry;
import com.sigmundgranaas.forgero.model.impl.RecursiveModelResolver;
import com.sigmundgranaas.forgero.utility.resource.loader.implementation.ClassPathResourceProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class ModelResolverTest {
	private ModelResolver resolver;

	@BeforeEach
	void setUp() {
		ModelRegistry registry = new MapBackedModelRegistry();
		new DefaultModelRegistrationService(registry, new ClassPathResourceProvider("assets")).registerModels("forgero");
		resolver = new RecursiveModelResolver(registry);
	}

	@Test
	void resolveToolWithModelSelector() {
		Component handle = new StaticComponent(new OpenIdentifier("forgero", "oak-handle"), Set.of(), List.of());
		Component head = new StaticComponent(new OpenIdentifier("forgero", "iron-pickaxe_head"), Set.of(), List.of());
		var headSlot = new StructureSlot(new OpenIdentifier("forgero", "head"), new OpenIdentifier("forgero", "head"), "", head);
		var handleSlot = new StructureSlot(new OpenIdentifier("forgero", "handle"), new OpenIdentifier("forgero", "handle"), "", handle);
		Component pickaxe = new StructuredPart(new OpenIdentifier("forgero:iron-pickaxe"), Set.of(), List.of(), new ComponentStructure(Map.of(headSlot.id(), headSlot, handleSlot.id(), handleSlot)));

		Optional<LayeredTexture> resolved = resolver.resolve(pickaxe);
		assertTrue(resolved.isPresent(), "The model resolver should find a model for the pickaxe.");
		List<TextureLayer> layers = resolved.get().layers();

		assertEquals(2, layers.size());
		assertEquals("forgero:item/special_pickaxe_handle", layers.get(0).texture(), "Handle model should be overridden by model_selector");
		assertEquals(0, layers.get(0).order());
		// Corrected the expected texture path for the pickaxe head
		assertEquals("forgero:item/iron-pickaxe_head", layers.get(1).texture());
		assertEquals(21, layers.get(1).order());
	}
}
