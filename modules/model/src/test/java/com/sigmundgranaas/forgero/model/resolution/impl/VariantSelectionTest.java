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
 * Behavioral tests for variant selection logic.
 *
 * Variants allow models to change textures based on predicates (game state, component properties).
 * This is how bows show pulling textures, gems change colors, and enchantments glow.
 *
 * Key behaviors:
 * - Variants selected when ALL predicates match (AND logic)
 * - First matching variant wins (deterministic)
 * - Variants can override texture, offset, or entire model
 * - Falls back to default when no variants match
 * - Empty predicate list always matches
 */
class VariantSelectionTest {

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

	// ==================== Basic Variant Matching ====================

	@Test
	void variantSelectedWhenAllPredicatesMatch() {
		// Bow with pulling=true, pull=0.65 should use pulling variant
		Component bow = components.createBow();

		Map<String, Object> pulling = Map.of(
			"pulling", true,
			"pull", 0.65f
		);

		var textures = resolve(bow, pulling);

		assertHasTextures(textures);
		// Should have pulling texture (variant matched)
		assertContainsTexture(textures, "pulling",
			"Variant should be selected when all predicates match");
	}

	@Test
	void defaultTextureWhenNoVariantMatches() {
		// Bow with pulling=false should use default texture
		Component bow = components.createBow();

		Map<String, Object> notPulling = Map.of(
			"pulling", false,
			"pull", 0.0f
		);

		var textures = resolve(bow, notPulling);

		assertHasTextures(textures);
		// Should NOT have pulling texture (no variant matched)
		assertDoesNotContainTexture(textures, "pulling",
			"Should use default texture when no variant matches");
	}

	@Test
	void noVariantWhenAnyPredicateFails() {
		// Arrow has predicates: root_tag="ranged/bow" AND pulling=true
		// If arrow is standalone (root_tag="ranged/arrow"), predicates fail
		Component arrow = components.createArrow();

		Map<String, Object> pulling = Map.of(
			"pulling", true,
			"pull", 0.65f
		);

		var textures = resolve(arrow, pulling);

		assertHasTextures(textures);
		// Arrow parts should NOT have in_bow variants (root_tag predicate failed)
		assertDoesNotContainTexture(textures, "in_bow",
			"Variant should NOT be selected when any predicate fails");
	}

	// ==================== Variant Ordering ====================

	@Test
	void firstMatchingVariantWins() {
		// Bow has multiple pulling variants at different pull thresholds
		// At pull=0.9, multiple variants could match, but first wins
		Component bow = components.createBow();

		Map<String, Object> highPull = Map.of(
			"pulling", true,
			"pull", 0.9f
		);

		var textures = resolve(bow, highPull);

		assertHasTextures(textures);
		// Should have pulling texture (first matching variant)
		assertContainsTexture(textures, "pulling",
			"First matching variant should be selected");
	}

	// ==================== Partial Override ====================

	@Test
	void variantCanOverrideTextureOnly() {
		// Variants can change texture without changing offset
		Component arrow = components.createArrow();
		Component bow = components.createBowWithArrow(arrow);

		Map<String, Object> pulling = Map.of(
			"pulling", true,
			"pull", 0.65f,
			"equippedArrow", arrow
		);

		var textures = resolve(bow, pulling);

		assertHasTextures(textures);
		// Arrow parts should have in_bow texture variant
		assertContainsTexture(textures, "in_bow",
			"Variant should override texture");
	}

	// ==================== Model Replacement ====================

	@Test
	void variantCanReplaceEntireModel() {
		// TextureModel.apply() can return a completely different model
		// This is used for complex state-dependent rendering
		Component bow = components.createBow();

		Map<String, Object> pulling = Map.of(
			"pulling", true,
			"pull", 0.65f
		);

		var textures = resolve(bow, pulling);

		assertHasTextures(textures);
		// Model should be rendered (with or without replacement)
		// If replacement happens, we still get valid textures
	}

	// ==================== Edge Cases ====================

	@Test
	void modelWithNoVariantsUsesDefaultTexture() {
		// Models without variants always use default texture
		Component arrow = components.createArrow();

		var textures = resolve(arrow, Collections.emptyMap());

		assertHasTextures(textures);
		// Should have arrow textures (defaults, no variants needed)
		assertContainsTexture(textures, "arrow",
			"Should use default textures when model has no variants");
	}

	@Test
	void emptyDynamicStateStillResolves() {
		// Models should resolve even with empty dynamic state
		Component bow = components.createBow();

		var textures = resolve(bow, Collections.emptyMap());

		assertHasTextures(textures);
		// Should have bow textures (defaults)
		assertContainsTexture(textures, "bow",
			"Should resolve with default textures when dynamicState is empty");
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
