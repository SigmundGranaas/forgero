package com.sigmundgranaas.forgero.model.resolution.impl;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.StructuredComponent;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentStructure;
import com.sigmundgranaas.forgero.core.component.api.structure.StructureSlot;
import com.sigmundgranaas.forgero.core.component.impl.StaticComponent;
import com.sigmundgranaas.forgero.core.property.api.Property;
import com.sigmundgranaas.forgero.model.api.RenderableTexture;
import com.sigmundgranaas.forgero.model.loading.impl.FileModelProvider;
import com.sigmundgranaas.forgero.model.registry.api.item.ItemModelRegistry;
import com.sigmundgranaas.forgero.model.registry.impl.MapBackedModelRegistry;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceProvider;
import com.sigmundgranaas.forgero.utility.resource.loader.implementation.ClassPathResourceProvider;
import com.sigmundgranaas.forgero.utility.resource.loader.implementation.ResourceLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModelResolverTest {

	private ItemModelRegistry registry;
	private RecursiveModelResolver resolver;

	@BeforeEach
	void setUp() {
		// Setup Model Registry
		registry = new MapBackedModelRegistry();
		ResourceProvider resourceProvider = new ClassPathResourceProvider("/assets");
		var fileModelProvider = new FileModelProvider(resourceProvider);
		new ResourceLoader<>(resourceProvider, fileModelProvider)
				.load(new OpenIdentifier("forgero", "models"), true)
				.forEach(registry::register);

		// Setup Resolver
		resolver = new RecursiveModelResolver(registry);
	}

	private Component mockComponent(String id, String... tags) {
		Set<OpenIdentifier> tagSet = Arrays.stream(tags)
				.map(tag -> new OpenIdentifier("forgero", tag))
				.collect(Collectors.toSet());
		return new StaticComponent(new OpenIdentifier(id), tagSet, new HashMap<>());
	}

	private StructuredComponent mockStructuredComponent(String id, Map<String, Component> parts, String... tags) {
		Map<OpenIdentifier, StructureSlot> slotMap = new HashMap<>();
		parts.forEach((slotIdPath, component) -> {
			var openSlotId = new OpenIdentifier("forgero", slotIdPath);
			slotMap.put(openSlotId, new StructureSlot(openSlotId, new OpenIdentifier("forgero:slot_type"), "description", component));
		});
		ComponentStructure structure = new ComponentStructure(slotMap);
		Set<OpenIdentifier> tagSet = Arrays.stream(tags)
				.map(tag -> new OpenIdentifier("forgero", tag))
				.collect(Collectors.toSet());
		return new MockStructuredEquipment(new OpenIdentifier(id), tagSet, Collections.emptyList(), structure);
	}

	@Test
	void testContextualReinforcement() {
		// The component ID represents the base material/item.
		Component reinforcement = mockComponent("forgero:iron");

		StructuredComponent head = mockStructuredComponent(
				"forgero:parts/iron-pickaxe_head", // This model provides the context "pickaxe_head_reinforcement"
				Map.of("reinforcement_slot", reinforcement)
		);

		List<RenderableTexture> textures = resolver.resolve(head).orElse(Collections.emptyList());
		textures.sort(Comparator.naturalOrder());

		assertEquals(2, textures.size());
		assertTrue(textures.stream().anyMatch(t -> t.texture().equals("forgero:item/iron-pickaxe_head")), "Base head texture should be present.");
		// The resolver should have found the model for 'iron' in the 'pickaxe_head_reinforcement' context.
		assertTrue(textures.stream().anyMatch(t -> t.texture().equals("forgero:item/iron-pickaxe_head_reinforcement")), "Reinforcement texture should be the contextual variant.");
	}

	@Test
	void testMastercraftedContextualReinforcement() {
		Component reinforcement = mockComponent("forgero:iron");
		StructuredComponent head = mockStructuredComponent(
				"forgero:parts/iron-mastercrafted_pickaxe_head", // This model provides the context "mastercrafted_pickaxe_head_reinforcement"
				Map.of("reinforcement_slot", reinforcement)
		);

		List<RenderableTexture> textures = resolver.resolve(head).orElse(Collections.emptyList());
		textures.sort(Comparator.naturalOrder());

		assertEquals(2, textures.size());
		assertTrue(textures.stream().anyMatch(t -> t.texture().equals("forgero:item/mastercrafted-iron-pickaxe_head")), "Base mastercrafted head texture should be present.");
		// The resolver should have found the model for 'iron' in the 'mastercrafted_pickaxe_head_reinforcement' context.
		assertTrue(textures.stream().anyMatch(t -> t.texture().equals("forgero:item/iron-mastercrafted_pickaxe_head_reinforcement")), "Reinforcement texture should be the mastercrafted contextual variant.");
	}

	private record MockStructuredEquipment(OpenIdentifier id, Set<OpenIdentifier> tags, List<com.sigmundgranaas.forgero.core.property.api.Property> properties, ComponentStructure structure) implements StructuredComponent {
		@Override
		public Component withStructure(ComponentStructure newStructure) {
			return new MockStructuredEquipment(id, tags, properties, newStructure);
		}

		@Override
		public Map<String, List<?>> propertiesAsMap() {
			return new HashMap<>();
		}

		@Override
		public Set<OpenIdentifier> getTags() {
			return tags;
		}
	}
}
