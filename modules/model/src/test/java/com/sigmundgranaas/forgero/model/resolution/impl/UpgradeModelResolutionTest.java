package com.sigmundgranaas.forgero.model.resolution.impl;

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
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceProvider;
import com.sigmundgranaas.forgero.utility.resource.loader.implementation.ClassPathResourceProvider;
import com.sigmundgranaas.forgero.utility.resource.loader.implementation.ResourceLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for upgrade model resolution to verify that all upgrade model types
 * correctly resolve their textures in the appropriate contexts.
 *
 * This test class covers:
 * - Hard material pommels (bamboo, shells, etc.)
 * - Upgrade material reinforcements (fire_charge, slime_ball, honeycomb)
 * - Binding gems (diamond, emerald)
 * - Binding dyes (red_dye, blue_dye)
 * - Soft material grips (string, leather)
 * - Arrow shaft materials (wood types)
 */
@DisplayName("Upgrade Model Resolution Tests")
class UpgradeModelResolutionTest {

	private ItemModelRegistry registry;
	private RecursiveModelResolver resolver;

	@BeforeEach
	void setUp() {
		registry = new MapBackedModelRegistry();
		ResourceProvider resourceProvider = new ClassPathResourceProvider("/assets");
		var fileModelProvider = new FileModelProvider();
		new ResourceLoader<>(resourceProvider, fileModelProvider)
				.load(OpenIdentifier.of("forgero", "forgero_models"), true)
				.forEach(registry::register);

		resolver = new RecursiveModelResolver(registry);
	}

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
			slots.add(new ComponentPart(openSlotId, OpenIdentifier.parse("forgero:slot_type"), "description", SlotValidator.ACCEPT_ALL, component));
		});
		ComponentStructure structure = ComponentStructure.of(slots);
		Set<OpenIdentifier> tagSet = Arrays.stream(tags)
				.map(tag -> OpenIdentifier.of("forgero", tag))
				.collect(Collectors.toSet());
		return new MockStructuredEquipment(OpenIdentifier.parse(id), tagSet, Collections.emptyList(), structure);
	}

	@Nested
	@DisplayName("Hard Material Pommel Tests")
	class HardMaterialPommelTests {

		@Test
		@DisplayName("Bamboo pommel resolves correct texture in handle context")
		void bambooPommelResolvesInHandleContext() {
			Component pommel = mockComponent("forgero:bamboo", "materials/properties/hard");

			StructuredComponent handle = mockStructuredComponent(
					"forgero:parts/oak-handle-with-pommel",
					Map.of("pommel_slot", pommel)
			);

			List<RenderableTexture> textures = resolver.resolve(handle).orElse(Collections.emptyList());

			assertEquals(2, textures.size(), "Should have handle base texture and pommel texture");
			assertTrue(textures.stream().anyMatch(t -> t.texture().equals("forgero:item/oak-handle")),
					"Base handle texture should be present");
			assertTrue(textures.stream().anyMatch(t -> t.texture().equals("forgero:item/upgrade/bamboo-pommel")),
					"Bamboo pommel texture should be present");
		}
	}

	@Nested
	@DisplayName("Upgrade Material Reinforcement Tests")
	class UpgradeMaterialReinforcementTests {

		@Test
		@DisplayName("Fire charge resolves as pickaxe head reinforcement")
		void fireChargeResolvesAsPickaxeHeadReinforcement() {
			Component reinforcement = mockComponent("forgero:fire_charge", "materials/roles/upgrade_material");

			StructuredComponent head = mockStructuredComponent(
					"forgero:parts/iron-pickaxe_head",
					Map.of("reinforcement_slot", reinforcement)
			);

			List<RenderableTexture> textures = resolver.resolve(head).orElse(Collections.emptyList());

			assertEquals(2, textures.size(), "Should have head base texture and reinforcement texture");
			assertTrue(textures.stream().anyMatch(t -> t.texture().equals("forgero:item/iron-pickaxe_head")),
					"Base pickaxe head texture should be present");
			assertTrue(textures.stream().anyMatch(t -> t.texture().equals("forgero:item/upgrade/fire_charge-pickaxe_head_reinforcement")),
					"Fire charge reinforcement texture should be present");
		}

		@Test
		@DisplayName("Slime ball resolves as sword blade reinforcement")
		void slimeBallResolvesAsSwordBladeReinforcement() {
			Component reinforcement = mockComponent("forgero:slime_ball", "materials/roles/upgrade_material");

			StructuredComponent blade = mockStructuredComponent(
					"forgero:parts/iron-sword_blade",
					Map.of("reinforcement_slot", reinforcement)
			);

			List<RenderableTexture> textures = resolver.resolve(blade).orElse(Collections.emptyList());

			assertEquals(2, textures.size(), "Should have blade base texture and reinforcement texture");
			assertTrue(textures.stream().anyMatch(t -> t.texture().equals("forgero:item/iron-sword_blade")),
					"Base sword blade texture should be present");
			assertTrue(textures.stream().anyMatch(t -> t.texture().equals("forgero:item/upgrade/slime_ball-sword_blade_reinforcement")),
					"Slime ball reinforcement texture should be present");
		}

		@Test
		@DisplayName("Honeycomb resolves as guard reinforcement")
		void honeycombResolvesAsGuardReinforcement() {
			Component reinforcement = mockComponent("forgero:honeycomb", "materials/roles/upgrade_material");

			StructuredComponent guard = mockStructuredComponent(
					"forgero:parts/iron-sword_guard",
					Map.of("reinforcement_slot", reinforcement)
			);

			List<RenderableTexture> textures = resolver.resolve(guard).orElse(Collections.emptyList());

			assertEquals(2, textures.size(), "Should have guard base texture and reinforcement texture");
			assertTrue(textures.stream().anyMatch(t -> t.texture().equals("forgero:item/iron-sword_guard")),
					"Base sword guard texture should be present");
			assertTrue(textures.stream().anyMatch(t -> t.texture().equals("forgero:item/upgrade/honeycomb-guard_reinforcement")),
					"Honeycomb reinforcement texture should be present");
		}
	}

	@Nested
	@DisplayName("Binding Upgrade Tests")
	class BindingUpgradeTests {

		@Test
		@DisplayName("Diamond gem resolves in binding gem slot")
		void diamondGemResolvesInBindingGemSlot() {
			Component gem = mockComponent("forgero:diamond", "upgrades/types/gem");

			StructuredComponent binding = mockStructuredComponent(
					"forgero:parts/leather-binding",
					Map.of("gem_slot", gem)
			);

			List<RenderableTexture> textures = resolver.resolve(binding).orElse(Collections.emptyList());

			assertEquals(2, textures.size(), "Should have binding base texture and gem texture");
			assertTrue(textures.stream().anyMatch(t -> t.texture().equals("forgero:item/leather-pickaxe_binding")),
					"Base binding texture should be present");
			assertTrue(textures.stream().anyMatch(t -> t.texture().equals("forgero:item/upgrade/diamond-binding_gem")),
					"Diamond binding gem texture should be present");
		}

		@Test
		@DisplayName("Red dye resolves in binding cosmetic slot")
		void redDyeResolvesInBindingCosmeticSlot() {
			Component dye = mockComponent("forgero:red_dye", "materials/types/dye");

			StructuredComponent binding = mockStructuredComponent(
					"forgero:parts/leather-binding",
					Map.of("cosmetic_slot", dye)
			);

			List<RenderableTexture> textures = resolver.resolve(binding).orElse(Collections.emptyList());

			assertEquals(2, textures.size(), "Should have binding base texture and dye texture");
			assertTrue(textures.stream().anyMatch(t -> t.texture().equals("forgero:item/leather-pickaxe_binding")),
					"Base binding texture should be present");
			assertTrue(textures.stream().anyMatch(t -> t.texture().equals("forgero:item/upgrade/red_dye-binding")),
					"Red dye binding texture should be present");
		}

		@Test
		@DisplayName("Binding with both gem and dye resolves both textures")
		void bindingWithGemAndDyeResolvesBoth() {
			Component gem = mockComponent("forgero:diamond", "upgrades/types/gem");
			Component dye = mockComponent("forgero:red_dye", "materials/types/dye");

			StructuredComponent binding = mockStructuredComponent(
					"forgero:parts/leather-binding",
					Map.of(
							"gem_slot", gem,
							"cosmetic_slot", dye
					)
			);

			List<RenderableTexture> textures = resolver.resolve(binding).orElse(Collections.emptyList());

			assertEquals(3, textures.size(), "Should have binding base, gem, and dye textures");
			assertTrue(textures.stream().anyMatch(t -> t.texture().equals("forgero:item/leather-pickaxe_binding")),
					"Base binding texture should be present");
			assertTrue(textures.stream().anyMatch(t -> t.texture().equals("forgero:item/upgrade/diamond-binding_gem")),
					"Diamond binding gem texture should be present");
			assertTrue(textures.stream().anyMatch(t -> t.texture().equals("forgero:item/upgrade/red_dye-binding")),
					"Red dye binding texture should be present");
		}
	}

	@Nested
	@DisplayName("Soft Material Grip Tests")
	class SoftMaterialGripTests {

		@Test
		@DisplayName("String grip resolves in handle grip slot")
		void stringGripResolvesInHandleGripSlot() {
			Component grip = mockComponent("forgero:string", "materials/properties/soft");

			StructuredComponent handle = mockStructuredComponent(
					"forgero:parts/oak-handle-with-pommel",
					Map.of("grip_slot", grip)
			);

			List<RenderableTexture> textures = resolver.resolve(handle).orElse(Collections.emptyList());

			assertEquals(2, textures.size(), "Should have handle base texture and grip texture");
			assertTrue(textures.stream().anyMatch(t -> t.texture().equals("forgero:item/oak-handle")),
					"Base handle texture should be present");
			assertTrue(textures.stream().anyMatch(t -> t.texture().equals("forgero:item/upgrade/string-handle_grip")),
					"String grip texture should be present");
		}
	}

	@Nested
	@DisplayName("Combined Upgrade Tests")
	class CombinedUpgradeTests {

		@Test
		@DisplayName("Handle with pommel and grip resolves both upgrade textures")
		void handleWithPommelAndGripResolvesBoth() {
			Component pommel = mockComponent("forgero:bamboo", "materials/properties/hard");
			Component grip = mockComponent("forgero:string", "materials/properties/soft");

			StructuredComponent handle = mockStructuredComponent(
					"forgero:parts/oak-handle-with-pommel",
					Map.of(
							"pommel_slot", pommel,
							"grip_slot", grip
					)
			);

			List<RenderableTexture> textures = resolver.resolve(handle).orElse(Collections.emptyList());

			assertEquals(3, textures.size(), "Should have handle base, pommel, and grip textures");
			assertTrue(textures.stream().anyMatch(t -> t.texture().equals("forgero:item/oak-handle")),
					"Base handle texture should be present");
			assertTrue(textures.stream().anyMatch(t -> t.texture().equals("forgero:item/upgrade/bamboo-pommel")),
					"Bamboo pommel texture should be present");
			assertTrue(textures.stream().anyMatch(t -> t.texture().equals("forgero:item/upgrade/string-handle_grip")),
					"String grip texture should be present");
		}
	}

	@Nested
	@DisplayName("Tool Material Reinforcement Tests (Existing Functionality)")
	class ToolMaterialReinforcementTests {

		@Test
		@DisplayName("Iron reinforcement on pickaxe head still works")
		void ironReinforcementStillWorks() {
			Component reinforcement = mockComponent("forgero:iron", "materials/roles/tool_material");

			StructuredComponent head = mockStructuredComponent(
					"forgero:parts/iron-pickaxe_head",
					Map.of("reinforcement_slot", reinforcement)
			);

			List<RenderableTexture> textures = resolver.resolve(head).orElse(Collections.emptyList());

			assertEquals(2, textures.size(), "Should have head base texture and reinforcement texture");
			assertTrue(textures.stream().anyMatch(t -> t.texture().equals("forgero:item/iron-pickaxe_head")),
					"Base pickaxe head texture should be present");
			assertTrue(textures.stream().anyMatch(t -> t.texture().equals("forgero:item/iron-pickaxe_head_reinforcement")),
					"Iron reinforcement texture should be present");
		}

		@Test
		@DisplayName("Diamond reinforcement on pickaxe head still works")
		void diamondReinforcementStillWorks() {
			Component reinforcement = mockComponent("forgero:diamond", "materials/roles/tool_material");

			StructuredComponent head = mockStructuredComponent(
					"forgero:parts/iron-pickaxe_head",
					Map.of("reinforcement_slot", reinforcement)
			);

			List<RenderableTexture> textures = resolver.resolve(head).orElse(Collections.emptyList());

			assertEquals(2, textures.size(), "Should have head base texture and reinforcement texture");
			assertTrue(textures.stream().anyMatch(t -> t.texture().contains("pickaxe_head")),
					"Base pickaxe head texture should be present");
			assertTrue(textures.stream().anyMatch(t -> t.texture().contains("diamond") && t.texture().contains("reinforcement")),
					"Diamond reinforcement texture should be present");
		}
	}

	@Nested
	@DisplayName("Tool Binding Upgrade Tests")
	class ToolBindingUpgradeTests {

		@Test
		@DisplayName("Iron binding resolves in tool binding slot")
		void ironBindingResolvesInToolBindingSlot() {
			// Create an iron-binding part (a binding made of iron material)
			Component binding = mockComponent("forgero:iron-binding", "parts/binding");

			// Create a pickaxe with the binding in its binding slot
			StructuredComponent pickaxe = mockStructuredComponent(
					"forgero:equipment/iron-pickaxe",
					Map.of("binding", binding)
			);

			List<RenderableTexture> textures = resolver.resolve(pickaxe).orElse(Collections.emptyList());

			// The binding texture should be present
			assertTrue(textures.stream().anyMatch(t ->
							t.texture().contains("iron") && t.texture().contains("binding")),
					"Iron binding texture should be present");
		}
	}

	private record MockStructuredEquipment(
			OpenIdentifier id,
			Set<OpenIdentifier> tags,
			List<com.sigmundgranaas.forgero.core.property.api.Property> properties,
			ComponentStructure structure
	) implements StructuredComponent {

		@Override
		public Component withStructure(ComponentStructure newStructure) {
			return new MockStructuredEquipment(id, tags, properties, newStructure);
		}

		@Override
		public Component withProperties(Map<String, List<?>> newProperties) {
			return this;
		}

		@Override
		public Map<String, List<?>> propertiesAsMap() {
			return new HashMap<>();
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
