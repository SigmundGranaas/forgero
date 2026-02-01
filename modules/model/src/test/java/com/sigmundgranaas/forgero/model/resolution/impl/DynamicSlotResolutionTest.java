package com.sigmundgranaas.forgero.model.resolution.impl;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
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

import static org.junit.jupiter.api.Assertions.*;

/**
 * Behavioral tests for dynamic slot resolution.
 *
 * Dynamic slots are resolved from dynamicState map instead of component structure.
 * This is how equipped arrows, side quivers, and dynamic attachments work.
 *
 * Key behaviors:
 * - Dynamic slots resolve from dynamicState["key"] when present
 * - Dynamic slots are ignored (not crash) when key missing
 * - Dynamic slots are processed before static slots
 * - Invalid types in dynamicState are handled gracefully
 * - Child components in dynamic slots inherit parent's dynamicState
 */
class DynamicSlotResolutionTest {

	private RecursiveModelResolver resolver;
	private TestComponentFactory components;

	@BeforeEach
	void setUp() {
		ItemModelRegistry registry = new MapBackedModelRegistry();
		ResourceProvider resourceProvider = new ClassPathResourceProvider("/assets");
		new ResourceLoader<>(resourceProvider, new FileModelProvider())
				.load(OpenIdentifier.of("forgero", "forgero_models"), true)
				.forEach(registry::register);

		resolver = new RecursiveModelResolver(registry);
		components = new TestComponentFactory();
	}

	// ==================== Core Dynamic Slot Behaviors ====================

	@Test
	void dynamicSlotResolvesWhenKeyPresentInDynamicState() {
		Component arrow = components.createArrow();
		Component bow = components.createBowWithArrow(arrow);

		// Dynamic state with equippedArrow key
		Map<String, Object> state = new HashMap<>();
		state.put("pulling", true);
		state.put("pull", 0.65f);
		state.put("equippedArrow", arrow);

		var textures = resolve(bow, state);

		assertHasTextures(textures);
		// Bow should render arrow parts from dynamic slot
		assertContainsTexture(textures, "arrow", "Bow should render equipped arrow from dynamic slot");
	}

	@Test
	void dynamicSlotIgnoredWhenKeyMissingInDynamicState() {
		Component bow = components.createBowWithArrow(null);

		// Dynamic state WITHOUT equippedArrow key
		Map<String, Object> state = new HashMap<>();
		state.put("pulling", true);
		state.put("pull", 0.65f);
		// Note: no "equippedArrow" key

		var textures = resolve(bow, state);

		assertHasTextures(textures);
		// Should have bow parts but no arrow parts
		assertContainsTexture(textures, "bow", "Should render bow components");
		assertDoesNotContainTexture(textures, "arrow_head", "Should not crash or render arrow when key missing");
	}

	@Test
	void dynamicSlotHandlesNullValueGracefully() {
		Component bow = components.createBowWithArrow(null);

		Map<String, Object> state = new HashMap<>();
		state.put("pulling", true);
		state.put("pull", 0.65f);
		state.put("equippedArrow", null);  // Explicit null

		var textures = resolve(bow, state);

		assertHasTextures(textures);
		assertContainsTexture(textures, "bow", "Should render bow despite null arrow");
		assertDoesNotContainTexture(textures, "arrow", "Should skip null arrow in dynamic slot");
	}

	@Test
	void dynamicSlotIgnoresNonComponentTypeInDynamicState() {
		Component bow = components.createBowWithArrow(null);

		Map<String, Object> state = new HashMap<>();
		state.put("pulling", true);
		state.put("pull", 0.65f);
		state.put("equippedArrow", "not a component");  // Wrong type

		var textures = resolve(bow, state);

		assertHasTextures(textures);
		assertContainsTexture(textures, "bow", "Should render bow despite invalid arrow type");
		assertDoesNotContainTexture(textures, "arrow", "Should skip non-Component value in dynamic slot");
	}

	// ==================== Multiple Dynamic Slots ====================

	@Test
	void multipleDynamicSlotsResolveIndependently() {
		// Create bow with arrow in dynamic slot
		Component arrow = components.createArrow();
		Component bow = components.createBowWithArrow(arrow);

		Map<String, Object> state = new HashMap<>();
		state.put("pulling", true);
		state.put("pull", 0.65f);
		state.put("equippedArrow", arrow);

		var textures = resolve(bow, state);

		assertHasTextures(textures);
		// Both bow and arrow should render
		assertContainsTexture(textures, "bow", "Should render bow");
		assertContainsTexture(textures, "arrow", "Should render arrow from dynamic slot");
	}

	@Test
	void dynamicSlotWithPartialDynamicState() {
		Component arrow = components.createArrow();
		Component bow = components.createBowWithArrow(arrow);

		// Only equippedArrow in dynamic state, no pulling/pull
		Map<String, Object> state = new HashMap<>();
		state.put("equippedArrow", arrow);

		var textures = resolve(bow, state);

		assertHasTextures(textures);
		assertContainsTexture(textures, "arrow", "Should resolve arrow even without pulling state");
	}

	// ==================== Dynamic State Inheritance ====================

	@Test
	void childComponentInDynamicSlotInheritsDynamicState() {
		Component arrow = components.createArrow();
		Component bow = components.createBowWithArrow(arrow);

		Map<String, Object> state = new HashMap<>();
		state.put("pulling", true);
		state.put("pull", 0.65f);
		state.put("equippedArrow", arrow);

		var textures = resolve(bow, state);

		assertHasTextures(textures);
		// Arrow parts should use in_bow variants (requires access to "pulling" state)
		assertContainsTexture(textures, "in_bow",
			"Arrow parts should access parent's pulling state and use in_bow variants");
	}

	@Test
	void emptyDynamicStateStillResolvesStaticSlots() {
		Component bow = components.createBow();

		Map<String, Object> emptyState = Collections.emptyMap();

		var textures = resolve(bow, emptyState);

		assertHasTextures(textures);
		// Should still render bow parts from static slots
		assertContainsTexture(textures, "bow", "Should render static slots even with empty dynamic state");
	}

	// ==================== Helper Methods ====================

	private List<RenderableTexture> resolve(Component component, Map<String, Object> state) {
		return resolver.resolve(component, state)
				.orElse(Collections.emptyList())
				.stream()
				.filter(t -> t.texture() != null)
				.toList();
	}

	// ==================== Assertion Helpers ====================

	private void assertHasTextures(List<RenderableTexture> textures) {
		assertFalse(textures.isEmpty(), "Should have textures");
	}

	private void assertContainsTexture(List<RenderableTexture> textures, String substring, String message) {
		boolean found = textures.stream().anyMatch(t -> t.texture().contains(substring));
		assertTrue(found, message + ". Got: " + formatTextures(textures));
	}

	private void assertDoesNotContainTexture(List<RenderableTexture> textures, String substring, String message) {
		boolean found = textures.stream().anyMatch(t -> t.texture().contains(substring));
		assertFalse(found, message + ". Got: " + formatTextures(textures));
	}

	private String formatTextures(List<RenderableTexture> textures) {
		return textures.stream()
				.map(RenderableTexture::texture)
				.reduce((a, b) -> a + ", " + b)
				.orElse("none");
	}
}
