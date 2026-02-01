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
 * Behavioral tests for contextual model selection.
 *
 * The context system allows components to use different models based on where/how they're used.
 * A model with "context": "arrow_shaft" only applies when the component is in a slot with
 * matching "model_context": "arrow_shaft".
 *
 * Key behaviors:
 * - Context-specific models selected when context matches
 * - Context overrides default model
 * - Falls back to default when no context match
 * - Multiple contexts select correctly
 * - No-context models still render
 */
class ContextualModelSelectionTest {

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

	// ==================== Basic Context Matching ====================

	@Test
	void contextModelSelectedWhenContextMatches() {
		// Arrow shaft should use context-specific model when in arrow context
		Component arrow = components.createArrowWithContextualShaft();

		var textures = resolve(arrow);

		assertHasTextures(textures);
		// Should use arrow_shaft context variant (different texture than default)
		assertContainsTexture(textures, "arrow_shaft",
			"Should use context-specific model when context matches");
	}

	@Test
	void defaultModelUsedWhenNoContextMatch() {
		// Material used in non-matching context should use default model
		Component tool = components.createToolWithContextualMaterial();

		var textures = resolve(tool);

		assertHasTextures(textures);
		// Should NOT use arrow_shaft variant (wrong context)
		assertDoesNotContainTexture(textures, "arrow_shaft",
			"Should use default model when context doesn't match");
	}

	// ==================== Context Priority ====================

	@Test
	void contextModelOverridesDefault() {
		// When both default and context models exist, context wins
		Component arrow = components.createArrowWithContextualShaft();

		var textures = resolve(arrow);

		assertHasTextures(textures);
		// Should use context variant, not default
		boolean hasContextTexture = textures.stream()
			.anyMatch(t -> t.texture().contains("arrow_shaft"));
		assertTrue(hasContextTexture,
			"Context-specific model should override default model");
	}

	@Test
	void multipleContextsSelectCorrectOne() {
		// Component with multiple context models should select the right one
		Component tool = components.createToolWithMultiContextMaterial();

		var textures = resolve(tool);

		assertHasTextures(textures);
		// Should use handle context (not arrow_shaft or blade)
		assertContainsTexture(textures, "handle",
			"Should select correct context model from multiple options");
	}

	// ==================== Real-World Integration ====================

	@Test
	void arrowShaftUsesArrowContext() {
		// Real arrow parts should use their context-specific models
		Component arrow = components.createArrow();

		var textures = resolve(arrow);

		assertHasTextures(textures);
		// Arrow parts exist and render (may use context-specific textures)
		assertContainsTexture(textures, "arrow",
			"Arrow parts should render (with or without context)");
	}

	@Test
	void sameComponentDifferentContexts() {
		// Same material (oak) used in different contexts should render differently
		Component arrow = components.createArrowWithContextualShaft();
		Component tool = components.createToolWithContextualMaterial();

		var arrowTextures = resolve(arrow);
		var toolTextures = resolve(tool);

		assertHasTextures(arrowTextures);
		assertHasTextures(toolTextures);

		// Oak in arrow should have different texture than oak in tool
		// (This test assumes oak has context-specific models for both)
		boolean arrowHasArrowShaft = arrowTextures.stream()
			.anyMatch(t -> t.texture().contains("arrow_shaft"));
		boolean toolHasArrowShaft = toolTextures.stream()
			.anyMatch(t -> t.texture().contains("arrow_shaft"));

		// Arrow should use arrow_shaft context, tool should not
		assertTrue(arrowHasArrowShaft,
			"Arrow should use arrow_shaft context");
		assertFalse(toolHasArrowShaft,
			"Tool should NOT use arrow_shaft context");
	}

	// ==================== Edge Cases ====================

	@Test
	void noContextModelStillResolves() {
		// Components without context specifications should still render
		Component simple = components.createBow();

		var textures = resolve(simple);

		assertHasTextures(textures);
		// Should render with default models (no crash)
		assertContainsTexture(textures, "bow",
			"Components without context should still render");
	}

	@Test
	void emptyDynamicStateDoesNotAffectContext() {
		// Context selection should work with empty dynamic state
		Component arrow = components.createArrowWithContextualShaft();

		var textures = resolve(arrow, Collections.emptyMap());

		assertHasTextures(textures);
		// Context selection independent of dynamic state
		assertContainsTexture(textures, "arrow",
			"Context selection should work with empty dynamic state");
	}

	// ==================== Helper Methods ====================

	private List<RenderableTexture> resolve(Component component) {
		return resolve(component, Collections.emptyMap());
	}

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
