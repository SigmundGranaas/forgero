package com.sigmundgranaas.forgero.model.integration;

import com.sigmundgranaas.forgero.cof.ComponentTypeRegistry;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.StructuredComponent;
import com.sigmundgranaas.forgero.core.component.api.slot.SlotValidator;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentPart;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentStructure;
import com.sigmundgranaas.forgero.core.component.impl.StaticComponent;
import com.sigmundgranaas.forgero.model.api.RenderableTexture;
import com.sigmundgranaas.forgero.model.loading.impl.FileModelProvider;
import com.sigmundgranaas.forgero.model.registry.api.item.ItemModelRegistry;
import com.sigmundgranaas.forgero.model.registry.impl.MapBackedModelRegistry;
import com.sigmundgranaas.forgero.model.rendering.impl.AwtTextureCompositor;
import com.sigmundgranaas.forgero.model.rendering.impl.ClassPathResourceTextureProvider;
import com.sigmundgranaas.forgero.model.rendering.api.TextureCompositor;
import com.sigmundgranaas.forgero.model.rendering.impl.PngWriter;
import com.sigmundgranaas.forgero.model.resolution.impl.RecursiveModelResolver;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceProvider;
import com.sigmundgranaas.forgero.utility.resource.loader.implementation.ClassPathResourceProvider;
import com.sigmundgranaas.forgero.utility.resource.loader.implementation.ResourceLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class FullPickaxeRenderTest {

	private ItemModelRegistry registry;
	private RecursiveModelResolver resolver;
	private TextureCompositor compositor;

	@BeforeEach
	void setUp() {
		// Setup Model Registry
		registry = new MapBackedModelRegistry();
		ResourceProvider resourceProvider = new ClassPathResourceProvider("/assets");
		var fileModelProvider = new FileModelProvider();
		new ResourceLoader<>(resourceProvider, fileModelProvider)
				.load(OpenIdentifier.of("forgero", "forgero_models"), true)
				.forEach(registry::register);

		// Setup Resolver and Compositor
		resolver = new RecursiveModelResolver(registry);
		compositor = new AwtTextureCompositor(new ClassPathResourceTextureProvider());
	}


	@Test
	void testRenderComplexPickaxe() throws IOException {
		// 1. Assemble the component hierarchy from the inside out
		Component diamond = mockComponent("forgero:diamond");
		Component leather = mockComponent("forgero:leather");
		Component emerald = mockComponent("forgero:emerald");

		StructuredComponent leatherBinding = mockStructuredComponent(
				"forgero:parts/leather-binding",
				Map.of("gem_slot", emerald)
		);

		StructuredComponent oakHandle = mockStructuredComponent(
				"forgero:parts/oak-handle",
				Map.of("grip_slot", leather)
		);

		StructuredComponent mastercraftedHead = mockStructuredComponent(
				"forgero:parts/iron-mastercrafted_pickaxe_head",
				Map.of("reinforcement_slot", diamond)
		);

		StructuredComponent pickaxe = mockStructuredComponent(
				"forgero:equipment/iron-pickaxe",
				Map.of("head", mastercraftedHead, "handle", oakHandle, "binding", leatherBinding),
				"pickaxe" // Add root tag for variants
		);


		// 2. Resolve the model to get the list of textures to render
		Optional<List<RenderableTexture>> resolvedTexturesOpt = resolver.resolve(pickaxe);
		assertTrue(resolvedTexturesOpt.isPresent(), "The resolver should find a model for the pickaxe");
		List<RenderableTexture> textures = resolvedTexturesOpt.get();

		// 3. Assert the correct textures were resolved
		assertEquals(6, textures.size(), "Should resolve to exactly 6 textures");

		Set<String> expectedTextures = Set.of(
				"forgero:item/mastercrafted-iron-pickaxe_head",  // Base head
				"forgero:item/diamond-mastercrafted_pickaxe_head_reinforcement", // Contextual diamond reinforcement
				"forgero:item/oak-pickaxe_handle", // Contextual handle variant for pickaxes
				"forgero:item/leather-grip", // Grip upgrade
				"forgero:item/leather-pickaxe_binding", // Base binding
				"forgero:item/emerald-gem" // Gem upgrade
		);

		Set<String> actualTextures = textures.stream()
				.map(RenderableTexture::texture)
				.collect(Collectors.toSet());

		assertEquals(expectedTextures, actualTextures, "The set of resolved textures should match the expected set");


		// 4. Render the final image
		BufferedImage result = compositor.render(textures);

		// 5. Assert basic image properties and save for manual verification
		assertNotNull(result);
		assertEquals(16, result.getWidth());
		assertEquals(16, result.getHeight());

		PngWriter.save(result, "build/test_results/full_pickaxe_render.png");
		System.out.println("Saved complex pickaxe render to build/test_results/full_pickaxe_render.png");
	}


	// Helper methods
	private Component mockComponent(String id, String... tags) {
		Set<OpenIdentifier> tagSet = Arrays.stream(tags)
				.map(tag -> OpenIdentifier.of("forgero", tag))
				.collect(Collectors.toSet());
		return new StaticComponent(OpenIdentifier.parse(id), tagSet, new HashMap<>());
	}

	private StructuredComponent mockStructuredComponent(String id, Map<String, Component> parts, String... tags) {
		List<ComponentPart> slots = new ArrayList<>();
		parts.forEach((slotIdPath, component) -> {
			var openSlotId = OpenIdentifier.of("forgero", slotIdPath);
			// Use component ID as slot type for simplicity in this mock
			slots.add(new ComponentPart(openSlotId, component.id(), "description", SlotValidator.ACCEPT_ALL, component));
		});
		ComponentStructure structure = ComponentStructure.of(slots);
		Set<OpenIdentifier> tagSet = Arrays.stream(tags)
				.map(tag -> OpenIdentifier.of("forgero", tag))
				.collect(Collectors.toSet());
		return new MockStructuredEquipment(OpenIdentifier.parse(id), tagSet, new HashMap<>(), structure);
	}

	private record MockStructuredEquipment(OpenIdentifier id, Set<OpenIdentifier> tags, Map<String, List<?>> properties, ComponentStructure structure) implements StructuredComponent {
		@Override
		public Component withStructure(ComponentStructure newStructure) {
			return new MockStructuredEquipment(id, tags, properties, newStructure);
		}

		@Override
		public Component withProperties(Map<String, List<?>> newProperties) {
			return new MockStructuredEquipment(id, tags, newProperties, structure);
		}

		@Override
		public  Map<String, List<?>> propertiesAsMap() {
			return properties;
		}

		@Override
		public Set<OpenIdentifier> getTags() {
			return tags;
		}

		@Override
		public OpenIdentifier getTypeIdentifier() {
			return ComponentTypeRegistry.STRUCTURED_EQUIPMENT;
		}
	}
}
