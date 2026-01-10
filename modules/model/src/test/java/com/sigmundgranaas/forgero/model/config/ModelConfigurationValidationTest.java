package com.sigmundgranaas.forgero.model.config;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.model.api.RenderableTexture;
import com.sigmundgranaas.forgero.model.config.TestComponentBuilder.TestComponent;
import com.sigmundgranaas.forgero.model.loading.impl.FileModelProvider;
import com.sigmundgranaas.forgero.model.registry.api.item.ItemModelRegistry;
import com.sigmundgranaas.forgero.model.registry.impl.MapBackedModelRegistry;
import com.sigmundgranaas.forgero.model.resolution.impl.RecursiveModelResolver;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceProvider;
import com.sigmundgranaas.forgero.utility.resource.loader.implementation.ClassPathResourceProvider;
import com.sigmundgranaas.forgero.utility.resource.loader.implementation.ResourceLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static com.sigmundgranaas.forgero.model.config.TestComponentBuilder.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Model Configuration Validation")
class ModelConfigurationValidationTest {

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

	@Nested
	@DisplayName("Basic Part Model Resolution")
	class BasicPartTests {

		@Test
		@DisplayName("Iron pickaxe head resolves to correct texture")
		void iron_pickaxe_head_resolves() {
			TestComponent head = simpleHead("iron", "pickaxe_head");

			Optional<List<RenderableTexture>> result = resolver.resolve(head);

			assertTrue(result.isPresent(), "Model should resolve");
			List<RenderableTexture> textures = result.get();
			assertFalse(textures.isEmpty(), "Should have at least one texture");
			assertTrue(textures.stream().anyMatch(t -> t.texture().contains("iron-pickaxe_head")),
					"Should contain iron-pickaxe_head texture");
		}

		@Test
		@DisplayName("Oak handle resolves to correct texture")
		void oak_handle_resolves() {
			TestComponent handle = simpleHandle("oak");

			Optional<List<RenderableTexture>> result = resolver.resolve(handle);

			assertTrue(result.isPresent(), "Model should resolve");
			List<RenderableTexture> textures = result.get();
			assertFalse(textures.isEmpty(), "Should have at least one texture");
			assertTrue(textures.stream().anyMatch(t -> t.texture().contains("oak-handle") || t.texture().contains("oak-pickaxe_handle")),
					"Should contain oak handle texture");
		}

		@Test
		@DisplayName("Leather binding resolves to correct texture")
		void leather_binding_resolves() {
			TestComponent binding = simpleBinding("leather");

			Optional<List<RenderableTexture>> result = resolver.resolve(binding);

			assertTrue(result.isPresent(), "Model should resolve");
			List<RenderableTexture> textures = result.get();
			assertFalse(textures.isEmpty(), "Should have at least one texture");
			assertTrue(textures.stream().anyMatch(t -> t.texture().contains("leather") && t.texture().contains("binding")),
					"Should contain leather binding texture, got: " + textures);
		}
	}

	@Nested
	@DisplayName("Equipment Assembly Model Resolution")
	class EquipmentAssemblyTests {

		@Test
		@DisplayName("Pickaxe with iron head and oak handle resolves both textures")
		void pickaxe_resolves_head_and_handle() {
			TestComponent pickaxe = create("forgero:equipment/iron-pickaxe")
					.withTags("forgero:tools/pickaxe", "forgero:tools", "forgero:pickaxe")
					.withPart("head", simpleHead("iron", "pickaxe_head"))
					.withPart("handle", simpleHandle("oak"))
					.buildEquipment();

			Optional<List<RenderableTexture>> result = resolver.resolve(pickaxe);

			assertTrue(result.isPresent(), "Pickaxe model should resolve");
			List<RenderableTexture> textures = result.get();
			assertFalse(textures.isEmpty(), "Should have textures");

			assertTrue(textures.stream().anyMatch(t -> t.texture().contains("iron-pickaxe_head")),
					"Should contain pickaxe head texture");
			assertTrue(textures.stream().anyMatch(t -> t.texture().contains("handle")),
					"Should contain handle texture");
		}

		@Test
		@DisplayName("Pickaxe with binding resolves all three textures")
		void pickaxe_with_binding_resolves_all_textures() {
			TestComponent pickaxe = create("forgero:equipment/iron-pickaxe")
					.withTags("forgero:tools/pickaxe", "forgero:tools", "forgero:pickaxe")
					.withPart("head", simpleHead("iron", "pickaxe_head"))
					.withPart("handle", simpleHandle("oak"))
					.withPart("binding", simpleBinding("leather"))
					.buildEquipment();

			Optional<List<RenderableTexture>> result = resolver.resolve(pickaxe);

			assertTrue(result.isPresent(), "Pickaxe model should resolve");
			List<RenderableTexture> textures = result.get();

			assertTrue(textures.stream().anyMatch(t -> t.texture().contains("iron-pickaxe_head")),
					"Should contain pickaxe head texture");
			assertTrue(textures.stream().anyMatch(t -> t.texture().contains("handle")),
					"Should contain handle texture");
			assertTrue(textures.stream().anyMatch(t -> t.texture().contains("leather") && t.texture().contains("binding")),
					"Should contain binding texture, got: " + textures);
		}
	}

	@Nested
	@DisplayName("Contextual Upgrade Resolution")
	class ContextualUpgradeTests {

		@Test
		@DisplayName("Reinforcement on pickaxe head uses contextual model")
		void reinforcement_on_pickaxe_head_uses_context() {
			TestComponent reinforcementMaterial = material("iron");

			TestComponent headWithReinforcement = create("forgero:parts/iron-pickaxe_head")
					.withTags("forgero:parts/head", "forgero:parts/types/pickaxe_head")
					.withPart("material", material("iron"))
					.withUpgrade("reinforcement_slot", reinforcementMaterial)
					.buildPart();

			Optional<List<RenderableTexture>> result = resolver.resolve(headWithReinforcement);

			assertTrue(result.isPresent(), "Head with reinforcement should resolve");
			List<RenderableTexture> textures = result.get();

			assertTrue(textures.stream().anyMatch(t -> t.texture().contains("iron-pickaxe_head")),
					"Should contain base head texture");
			assertTrue(textures.stream().anyMatch(t -> t.texture().contains("reinforcement")),
					"Should contain reinforcement texture");
		}

		@Test
		@DisplayName("Diamond reinforcement on iron head uses diamond texture")
		void diamond_reinforcement_on_iron_head() {
			TestComponent diamondReinforcement = material("diamond");

			TestComponent headWithDiamond = create("forgero:parts/iron-pickaxe_head")
					.withTags("forgero:parts/head", "forgero:parts/types/pickaxe_head")
					.withPart("material", material("iron"))
					.withUpgrade("reinforcement_slot", diamondReinforcement)
					.buildPart();

			Optional<List<RenderableTexture>> result = resolver.resolve(headWithDiamond);

			assertTrue(result.isPresent(), "Head with diamond reinforcement should resolve");
			List<RenderableTexture> textures = result.get();

			assertTrue(textures.stream().anyMatch(t -> t.texture().contains("iron-pickaxe_head")),
					"Base head should be iron");
			assertTrue(textures.stream().anyMatch(t ->
							t.texture().contains("diamond") && t.texture().contains("reinforcement")),
					"Reinforcement should be diamond");
		}
	}

	@Nested
	@DisplayName("Layer Ordering")
	class LayerOrderingTests {

		@Test
		@DisplayName("Handle renders before head (lower order)")
		void handle_renders_before_head() {
			TestComponent pickaxe = create("forgero:equipment/iron-pickaxe")
					.withTags("forgero:tools/pickaxe", "forgero:tools", "forgero:pickaxe")
					.withPart("head", simpleHead("iron", "pickaxe_head"))
					.withPart("handle", simpleHandle("oak"))
					.buildEquipment();

			Optional<List<RenderableTexture>> result = resolver.resolve(pickaxe);

			assertTrue(result.isPresent());
			List<RenderableTexture> textures = result.get();

			RenderableTexture handleTexture = textures.stream()
					.filter(t -> t.texture().contains("handle"))
					.findFirst()
					.orElseThrow(() -> new AssertionError("Handle texture not found"));

			RenderableTexture headTexture = textures.stream()
					.filter(t -> t.texture().contains("pickaxe_head"))
					.findFirst()
					.orElseThrow(() -> new AssertionError("Head texture not found"));

			assertTrue(handleTexture.order() < headTexture.order(),
					"Handle (order=" + handleTexture.order() + ") should render before head (order=" + headTexture.order() + ")");
		}

		@Test
		@DisplayName("Binding renders after handle and before or with head")
		void binding_layer_order() {
			TestComponent pickaxe = create("forgero:equipment/iron-pickaxe")
					.withTags("forgero:tools/pickaxe", "forgero:tools", "forgero:pickaxe")
					.withPart("head", simpleHead("iron", "pickaxe_head"))
					.withPart("handle", simpleHandle("oak"))
					.withPart("binding", simpleBinding("leather"))
					.buildEquipment();

			Optional<List<RenderableTexture>> result = resolver.resolve(pickaxe);

			assertTrue(result.isPresent());
			List<RenderableTexture> textures = result.get();

			int handleOrder = textures.stream()
					.filter(t -> t.texture().contains("handle"))
					.mapToInt(RenderableTexture::order)
					.findFirst()
					.orElse(-1);

			int bindingOrder = textures.stream()
					.filter(t -> t.texture().contains("binding"))
					.mapToInt(RenderableTexture::order)
					.findFirst()
					.orElse(-1);

			assertTrue(handleOrder >= 0, "Handle texture should exist");
			assertTrue(bindingOrder >= 0, "Binding texture should exist");
			assertTrue(handleOrder < bindingOrder,
					"Handle should render before binding");
		}
	}

	@Nested
	@DisplayName("Negative Cases")
	class NegativeCases {

		@Test
		@DisplayName("Unknown component ID returns empty")
		void unknown_component_returns_empty() {
			TestComponent unknown = create("forgero:nonexistent-thing")
					.withTags("forgero:unknown")
					.buildSimple();

			Optional<List<RenderableTexture>> result = resolver.resolve(unknown);

			assertTrue(result.isEmpty() || result.get().isEmpty(),
					"Unknown component should return empty result");
		}

		@Test
		@DisplayName("Component without model returns empty")
		void component_without_model_returns_empty() {
			TestComponent noModel = create("forgero:parts/custom-material-without-model")
					.withTags("forgero:materials/custom")
					.buildSimple();

			Optional<List<RenderableTexture>> result = resolver.resolve(noModel);

			assertTrue(result.isEmpty() || result.get().isEmpty(),
					"Component without model should return empty result");
		}
	}
}
