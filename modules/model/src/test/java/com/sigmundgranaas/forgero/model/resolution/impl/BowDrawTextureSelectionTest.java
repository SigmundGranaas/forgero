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
 * Behavioral tests for bow/arrow texture selection during draw progression.
 *
 * Tests verify that the model resolution system correctly selects texture variants
 * based on dynamic state (pulling, pull progress) and component context (standalone vs equipped).
 *
 * Key behaviors:
 * - Bows change textures as they're drawn (pull progression 0.0 to 1.0)
 * - Arrows use different textures when equipped in a bow vs standalone
 * - Arrow parts (head, shaft, fletching) switch to "in_bow" variants when equipped
 */
class BowDrawTextureSelectionTest {

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

	// ==================== Bow Draw Progression Tests ====================

	@Test
	void bowShowsPullingTexturesWhenDrawn() {
		Component bow = components.createBow();

		var notDrawn = resolve(bow, dynamicState(false, 0.0f));
		var drawn = resolve(bow, dynamicState(true, 0.65f));

		assertHasTextures(notDrawn);
		assertHasTextures(drawn);
		assertContainsTexture(drawn, "pulling", "Bow should show pulling texture when drawn");
	}

	@Test
	void bowUsesDefaultTexturesWhenNotDrawn() {
		Component bow = components.createBow();

		var notDrawn = resolve(bow, dynamicState(false, 0.0f));

		assertHasTextures(notDrawn);
		assertDoesNotContainTexture(notDrawn, "pulling", "Bow should not show pulling texture when not drawn");
	}

	@Test
	void bowTexturesChangeAcrossFullDrawProgression() {
		Component bow = components.createBow();
		float[] pullValues = {0.0f, 0.1f, 0.5f, 0.65f, 0.9f, 1.0f};

		for (float pull : pullValues) {
			var textures = resolve(bow, dynamicState(true, pull));

			assertHasTextures(textures, "Bow should have textures at pull=" + pull);

			if (pull >= 0.1f) {
				assertContainsTexture(textures, "pulling",
					"Bow should show pulling texture at pull=" + pull);
			}
		}
	}

	// ==================== Standalone Arrow Tests ====================

	@Test
	void standaloneArrowUsesDefaultTextures() {
		Component arrow = components.createArrow();

		var textures = resolve(arrow, dynamicState(true, 0.65f));

		assertHasTextures(textures);
		assertDoesNotContainTexture(textures, "in_bow",
			"Standalone arrow should use default textures, not in_bow variants");
	}

	@Test
	void standaloneArrowRendersAllParts() {
		Component arrow = components.createArrow();

		var textures = resolve(arrow, dynamicState(false, 0.0f));

		assertHasTextures(textures);
		assertContainsTexture(textures, "arrow_head", "Arrow should render head");
		assertContainsTexture(textures, "arrow_shaft", "Arrow should render shaft");
		assertContainsTexture(textures, "fletching", "Arrow should render fletching");
	}

	// ==================== Equipped Arrow Tests ====================

	@Test
	void equippedArrowUsesInBowVariants() {
		Component arrow = components.createArrow();
		Component bow = components.createBowWithArrow(arrow);

		var textures = resolve(bow, dynamicStateWithArrow(true, 0.65f, arrow));

		assertHasTextures(textures);
		assertContainsTexture(textures, "in_bow",
			"Arrow parts should use in_bow variants when equipped in drawn bow");
	}

	@Test
	void equippedArrowShowsAllComponents() {
		Component arrow = components.createArrow();
		Component bow = components.createBowWithArrow(arrow);

		var textures = resolve(bow, dynamicStateWithArrow(true, 0.65f, arrow));

		assertHasTextures(textures);
		// Bow parts
		assertContainsTexture(textures, "bow", "Should render bow components");
		assertContainsTexture(textures, "string", "Should render bow string");
		// Arrow parts
		assertContainsTexture(textures, "arrow", "Should render arrow components");
	}

	@Test
	void equippedArrowUsesInBowVariantsEvenWhenNotDrawn() {
		Component arrow = components.createArrow();
		Component bow = components.createBowWithArrow(arrow);

		var textures = resolve(bow, dynamicStateWithArrow(false, 1.0f, arrow));

		assertHasTextures(textures);
		assertContainsTexture(textures, "in_bow",
			"Arrow should use in_bow variants when equipped, even if bow is not drawn");
	}

	// ==================== Helper Methods ====================

	private List<RenderableTexture> resolve(Component component, Map<String, Object> state) {
		return resolver.resolve(component, state)
				.orElse(Collections.emptyList())
				.stream()
				.filter(t -> t.texture() != null)
				.toList();
	}

	private Map<String, Object> dynamicState(boolean pulling, float pull) {
		Map<String, Object> state = new HashMap<>();
		state.put("pulling", pulling);
		state.put("pull", pull);
		return state;
	}

	private Map<String, Object> dynamicStateWithArrow(boolean pulling, float pull, Component arrow) {
		Map<String, Object> state = dynamicState(pulling, pull);
		state.put("equippedArrow", arrow);
		return state;
	}

	// ==================== Assertion Helpers ====================

	private void assertHasTextures(List<RenderableTexture> textures) {
		assertHasTextures(textures, "Should have textures");
	}

	private void assertHasTextures(List<RenderableTexture> textures, String message) {
		assertFalse(textures.isEmpty(), message);
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
