package com.sigmundgranaas.forgero.model.resolution.impl;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.impl.StaticComponent;
import com.sigmundgranaas.forgero.model.api.ModelResolutionContext;
import com.sigmundgranaas.forgero.model.api.RenderableTexture;
import com.sigmundgranaas.forgero.model.loading.impl.FileModelProvider;
import com.sigmundgranaas.forgero.model.match.predicate.BowPullPredicate;
import com.sigmundgranaas.forgero.model.match.predicate.ChildTagPredicate;
import com.sigmundgranaas.forgero.model.match.predicate.RootTagPredicate;
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
 * Behavioral tests for predicate evaluation.
 *
 * Predicates determine when variants should be selected. Different predicate types
 * check different conditions (dynamic state, component tags, etc.).
 *
 * Key predicate types:
 * - BowPullPredicate: Checks pulling (boolean) and pull (float threshold) in dynamicState
 * - RootTagPredicate: Checks if root component has a tag
 * - ChildTagPredicate: Checks if current component has a tag
 *
 * Key behaviors:
 * - Missing keys in dynamicState handled gracefully
 * - Wrong types in dynamicState handled gracefully
 * - Tag predicates work with and without TagResolver
 * - Boundary values handled correctly
 */
class PredicateEvaluationTest {

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

	// ==================== BowPullPredicate Tests ====================

	@Test
	void bowPullPredicateMatchesWhenPullingTrue() {
		// pulling=true AND pull >= threshold → MATCH
		Map<String, Object> state = Map.of(
			"pulling", true,
			"pull", 0.65f
		);

		BowPullPredicate predicate = new BowPullPredicate(0.1f, true);
		ModelResolutionContext context = createContext(state);

		assertTrue(predicate.test(context),
			"BowPullPredicate should match when pulling=true and pull >= threshold");
	}

	@Test
	void bowPullPredicateDoesNotMatchWhenPullingFalse() {
		// pulling=false → NO MATCH (regardless of pull value)
		Map<String, Object> state = Map.of(
			"pulling", false,
			"pull", 0.9f
		);

		BowPullPredicate predicate = new BowPullPredicate(0.1f, true);
		ModelResolutionContext context = createContext(state);

		assertFalse(predicate.test(context),
			"BowPullPredicate should NOT match when pulling=false");
	}

	@Test
	void bowPullPredicateAtZeroPullBoundary() {
		// Boundary case: pull=0.0 with threshold=0.0
		Map<String, Object> state = Map.of(
			"pulling", true,
			"pull", 0.0f
		);

		BowPullPredicate predicate = new BowPullPredicate(0.0f, true);
		ModelResolutionContext context = createContext(state);

		assertTrue(predicate.test(context),
			"BowPullPredicate should match at boundary (pull=0.0, threshold=0.0)");
	}

	@Test
	void bowPullPredicateBelowThreshold() {
		// pull < threshold → NO MATCH
		Map<String, Object> state = Map.of(
			"pulling", true,
			"pull", 0.5f
		);

		BowPullPredicate predicate = new BowPullPredicate(0.9f, true);
		ModelResolutionContext context = createContext(state);

		assertFalse(predicate.test(context),
			"BowPullPredicate should NOT match when pull < threshold");
	}

	@Test
	void bowPullPredicateWithMissingPullingKey() {
		// Missing "pulling" key → defaults to !threshold
		Map<String, Object> state = Map.of(
			"pull", 0.5f
		);

		BowPullPredicate predicate = new BowPullPredicate(0.0f, true);
		ModelResolutionContext context = createContext(state);

		// When "pulling" key missing, predicate uses !threshold (false when threshold=0.0)
		assertFalse(predicate.test(context),
			"BowPullPredicate should NOT match when 'pulling' key is missing");
	}

	@Test
	void bowPullPredicateWithMissingPullKey() {
		// Missing "pull" key → defaults to 0.0
		Map<String, Object> state = Map.of(
			"pulling", true
		);

		BowPullPredicate predicateLow = new BowPullPredicate(0.0f, true);
		BowPullPredicate predicateHigh = new BowPullPredicate(0.5f, true);
		ModelResolutionContext context = createContext(state);

		// Default pull=0.0 >= threshold=0.0 → MATCH
		assertTrue(predicateLow.test(context),
			"BowPullPredicate should match when missing 'pull' key and threshold=0.0");

		// Default pull=0.0 < threshold=0.5 → NO MATCH
		assertFalse(predicateHigh.test(context),
			"BowPullPredicate should NOT match when missing 'pull' key and threshold > 0.0");
	}

	// ==================== RootTagPredicate Tests ====================

	@Test
	void rootTagPredicateMatchesWhenRootHasTag() {
		// Root component with matching tag → MATCH
		Component bow = components.createBow();

		RootTagPredicate predicate = new RootTagPredicate(
			OpenIdentifier.of("forgero", "ranged/bow")
		);

		ModelResolutionContext context = createContextWithRoot(bow);

		assertTrue(predicate.test(context),
			"RootTagPredicate should match when root has the tag");
	}

	@Test
	void rootTagPredicateDoesNotMatchWhenRootLacksTag() {
		// Root component without matching tag → NO MATCH
		Component arrow = components.createArrow();

		RootTagPredicate predicate = new RootTagPredicate(
			OpenIdentifier.of("forgero", "ranged/bow")
		);

		ModelResolutionContext context = createContextWithRoot(arrow);

		assertFalse(predicate.test(context),
			"RootTagPredicate should NOT match when root lacks the tag");
	}

	// ==================== ChildTagPredicate Tests ====================

	@Test
	void childTagPredicateChecksCurrentComponent() {
		// ChildTagPredicate checks current component, not root
		Component arrow = components.createArrow();

		ChildTagPredicate predicate = new ChildTagPredicate(
			OpenIdentifier.of("forgero", "ranged/arrow")
		);

		ModelResolutionContext context = createContextWithCurrent(arrow);

		assertTrue(predicate.test(context),
			"ChildTagPredicate should match when current component has the tag");
	}

	@Test
	void childTagPredicateDoesNotMatchWhenCurrentLacksTag() {
		// Current component without tag → NO MATCH
		Component bow = components.createBow();

		ChildTagPredicate predicate = new ChildTagPredicate(
			OpenIdentifier.of("forgero", "ranged/arrow")
		);

		ModelResolutionContext context = createContextWithCurrent(bow);

		assertFalse(predicate.test(context),
			"ChildTagPredicate should NOT match when current component lacks the tag");
	}

	// ==================== Integration: Predicates in Real Models ====================

	@Test
	void arrowInBowUsesInBowVariantsViaPredicates() {
		// Real integration: Arrow in bow should use in_bow variants
		// Predicates: root_tag="ranged/bow" AND pulling=true
		Component arrow = components.createArrow();
		Component bow = components.createBowWithArrow(arrow);

		Map<String, Object> pulling = Map.of(
			"pulling", true,
			"pull", 0.65f,
			"equippedArrow", arrow
		);

		var textures = resolve(bow, pulling);

		assertHasTextures(textures);
		// Arrow parts should use in_bow variants (both predicates match)
		assertContainsTexture(textures, "in_bow",
			"Arrow in bow should use in_bow variants when predicates match");
	}

	@Test
	void arrowInBowUsesDefaultWhenNotPulling() {
		// Arrow in bow but NOT pulling → in_bow variants don't match
		Component arrow = components.createArrow();
		Component bow = components.createBowWithArrow(arrow);

		Map<String, Object> notPulling = Map.of(
			"pulling", false,
			"pull", 0.0f,
			"equippedArrow", arrow
		);

		var textures = resolve(bow, notPulling);

		assertHasTextures(textures);
		// Arrow parts should NOT use in_bow variants (pulling=false)
		assertDoesNotContainTexture(textures, "in_bow",
			"Arrow in bow should use default variants when not pulling");
	}

	// ==================== Helper Methods ====================

	private List<RenderableTexture> resolve(Component component, Map<String, Object> state) {
		return resolver.resolve(component, state)
				.orElse(Collections.emptyList())
				.stream()
				.filter(t -> t.texture() != null)
				.toList();
	}

	private ModelResolutionContext createContext(Map<String, Object> dynamicState) {
		Component dummy = new StaticComponent(
			OpenIdentifier.parse("forgero:test"),
			Set.of(),
			new HashMap<>()
		);
		return new ModelResolutionContext(dummy, dummy, Optional.empty(), dynamicState);
	}

	private ModelResolutionContext createContextWithRoot(Component root) {
		return new ModelResolutionContext(root, root, Optional.empty(), Collections.emptyMap());
	}

	private ModelResolutionContext createContextWithCurrent(Component current) {
		Component root = new StaticComponent(
			OpenIdentifier.parse("forgero:root"),
			Set.of(),
			new HashMap<>()
		);
		return new ModelResolutionContext(root, current, Optional.empty(), Collections.emptyMap());
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
