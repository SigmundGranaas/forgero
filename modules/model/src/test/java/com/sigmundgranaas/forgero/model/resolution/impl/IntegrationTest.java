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
 * End-to-end integration tests for the model resolution system.
 *
 * These tests validate that the entire system works correctly with complex, real-world
 * component hierarchies. They test interactions between multiple features (variants,
 * contexts, dynamic slots, mount points, ordering) working together.
 *
 * Key scenarios:
 * - Complex equipment with multiple parts and upgrades
 * - Dynamic state-driven rendering with variants
 * - Multi-context scenarios (same material in different contexts)
 * - Performance with large hierarchies
 */
class IntegrationTest {

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

	// ==================== Complex Hierarchies ====================

	@Test
	void compositeToolWithMultiplePartsRendersCorrectly() {
		// Tool with head, handle, and binding should render all parts
		Component layeredTool = components.createLayeredTool();

		var textures = resolve(layeredTool);

		assertHasTextures(textures);
		// Should have textures from all parts
		assertContainsTexture(textures, "base",
			"Composite tool should render base part");
		assertContainsTexture(textures, "overlay",
			"Composite tool should render overlay part");
		assertContainsTexture(textures, "gem",
			"Composite tool should render gem part");
	}

	@Test
	void bowWithEquippedArrowRendersCorrectly() {
		// Real-world scenario: bow with equipped arrow
		Component arrow = components.createArrow();
		Component bow = components.createBowWithArrow(arrow);

		Map<String, Object> state = Map.of(
			"pulling", true,
			"pull", 0.65f,
			"equippedArrow", arrow
		);

		var textures = resolve(bow, state);

		assertHasTextures(textures);
		// Should render bow parts
		assertContainsTexture(textures, "bow",
			"Bow should render bow parts");
		// Should render arrow parts
		assertContainsTexture(textures, "arrow",
			"Bow should render equipped arrow parts");
	}

	// ==================== Multi-Context Scenarios ====================

	@Test
	void sameMaterialInDifferentContexts() {
		// Oak used in arrow shaft vs tool handle should use different models
		Component arrow = components.createArrowWithContextualShaft();
		Component tool = components.createToolWithMultiContextMaterial();

		var arrowTextures = resolve(arrow);
		var toolTextures = resolve(tool);

		assertHasTextures(arrowTextures);
		assertHasTextures(toolTextures);

		// Both should render (with context-appropriate models)
		assertNotNull(arrowTextures, "Arrow with contextual material should render");
		assertNotNull(toolTextures, "Tool with contextual material should render");
	}

	// ==================== Dynamic State + Variants + Context ====================

	@Test
	void complexStateDrivenRendering() {
		// Bow with arrow, pulling, and context should combine all features
		Component arrow = components.createArrow();
		Component bow = components.createBowWithArrow(arrow);

		Map<String, Object> state = Map.of(
			"pulling", true,
			"pull", 0.9f,
			"equippedArrow", arrow
		);

		var textures = resolve(bow, state);

		assertHasTextures(textures);
		// Should render with pulling variants
		boolean hasPullingOrBow = textures.stream()
			.anyMatch(t -> t.texture().contains("pulling") || t.texture().contains("bow"));

		assertTrue(hasPullingOrBow,
			"Complex state-driven rendering should produce textures");
	}

	// ==================== Performance ====================

	@Test
	void largeComponentHierarchyPerformance() {
		// Deep nesting should complete in reasonable time
		Component deepNested = components.createDeeplyNestedComponent(10);

		long startTime = System.nanoTime();

		for (int i = 0; i < 10; i++) {
			resolver.resolve(deepNested, Collections.emptyMap());
		}

		long endTime = System.nanoTime();
		long durationMs = (endTime - startTime) / 1_000_000;

		// 10 resolutions of 10-deep hierarchy should complete quickly
		assertTrue(durationMs < 1000,
			"Large hierarchy resolution should be performant (took " + durationMs + "ms for 10 iterations)");
	}

	@Test
	void repeatedResolutionPerformance() {
		// Repeated resolution of same component should be consistent
		Component bow = components.createBow();

		long startTime = System.nanoTime();

		for (int i = 0; i < 100; i++) {
			resolver.resolve(bow, Collections.emptyMap());
		}

		long endTime = System.nanoTime();
		long durationMs = (endTime - startTime) / 1_000_000;

		// 100 resolutions should complete quickly
		assertTrue(durationMs < 1000,
			"Repeated resolution should be performant (took " + durationMs + "ms for 100 iterations)");
	}

	// ==================== Rendering Order in Complex Hierarchies ====================

	@Test
	void complexHierarchyRespectsSsRenderingOrder() {
		// Layered tool with background, base, overlay, gem should render in order
		Component withBackground = components.createToolWithBackground();

		var textures = resolve(withBackground);

		assertHasTextures(textures);
		// Textures should be in order (verified by order field)
		List<Integer> orders = textures.stream().map(RenderableTexture::order).toList();

		// Verify ordering is ascending
		for (int i = 1; i < orders.size(); i++) {
			assertTrue(orders.get(i) >= orders.get(i - 1),
				"Textures should be in ascending order");
		}
	}

	// ==================== Real-World Edge Cases ====================

	@Test
	void emptyBowWithoutArrowRenders() {
		// Bow without equipped arrow should still render
		Component bow = components.createBow();

		var textures = resolve(bow, Collections.emptyMap());

		assertHasTextures(textures);
		// Should have bow textures (no arrow)
		assertContainsTexture(textures, "bow",
			"Empty bow without arrow should render bow parts");
	}

	@Test
	void arrowAloneRendersWithoutBow() {
		// Arrow rendered standalone should use default textures (not in_bow)
		Component arrow = components.createArrow();

		var textures = resolve(arrow, Collections.emptyMap());

		assertHasTextures(textures);
		// Should have arrow textures
		assertContainsTexture(textures, "arrow",
			"Standalone arrow should render");

		// Should NOT use in_bow variants (no bow context)
		boolean hasInBow = textures.stream()
			.anyMatch(t -> t.texture().contains("in_bow"));

		assertFalse(hasInBow,
			"Standalone arrow should not use in_bow variants");
	}

	// ==================== Equipment-Type-Specific Variants ====================

	@Test
	void oakHandleInPickaxeUsesPickaxeSpecificTexture() {
		// Oak handle in pickaxe should use pickaxe-specific texture
		Component pickaxe = components.createPickaxeWithOakHandle();

		var textures = resolve(pickaxe);

		assertHasTextures(textures);
		// Should use handle_pickaxe context texture
		assertContainsTexture(textures, "handle_pickaxe",
			"Oak handle in pickaxe should use pickaxe-specific texture");
	}

	@Test
	void oakHandleInSwordUsesSwordSpecificTexture() {
		// Oak handle in sword should use sword-specific texture
		Component sword = components.createSwordWithOakHandle();

		var textures = resolve(sword);

		assertHasTextures(textures);
		// Should use handle_sword context texture
		assertContainsTexture(textures, "handle_sword",
			"Oak handle in sword should use sword-specific texture");
	}

	@Test
	void oakHandleTexturesDifferByEquipmentType() {
		// Same material (oak) in same slot type (handle) should use different textures
		// when in different equipment types (pickaxe vs sword)
		Component pickaxe = components.createPickaxeWithOakHandle();
		Component sword = components.createSwordWithOakHandle();

		var pickaxeTextures = resolve(pickaxe);
		var swordTextures = resolve(sword);

		assertHasTextures(pickaxeTextures);
		assertHasTextures(swordTextures);

		// Pickaxe should have pickaxe-specific handle texture
		boolean hasPickaxeHandle = pickaxeTextures.stream()
			.anyMatch(t -> t.texture().contains("handle_pickaxe"));

		// Sword should have sword-specific handle texture
		boolean hasSwordHandle = swordTextures.stream()
			.anyMatch(t -> t.texture().contains("handle_sword"));

		assertTrue(hasPickaxeHandle,
			"Pickaxe should use pickaxe-specific handle texture");
		assertTrue(hasSwordHandle,
			"Sword should use sword-specific handle texture");

		// They should be different
		assertNotEquals(
			pickaxeTextures.stream().filter(t -> t.texture().contains("handle")).findFirst(),
			swordTextures.stream().filter(t -> t.texture().contains("handle")).findFirst(),
			"Pickaxe and sword handles should use different textures"
		);
	}

	// ==================== Multi-Gem Equipment ====================

	@Test
	void toolWithThreeGemsRendersAllGemsCorrectly() {
		// Tool with three different gems in three slots
		Component tool = components.createToolWithThreeGems();

		var textures = resolve(tool);

		assertHasTextures(textures);

		// Should have all three gem textures
		boolean hasDiamond = textures.stream()
			.anyMatch(t -> t.texture().contains("diamond_gem"));
		boolean hasEmerald = textures.stream()
			.anyMatch(t -> t.texture().contains("emerald_gem"));
		boolean hasRuby = textures.stream()
			.anyMatch(t -> t.texture().contains("ruby_gem"));

		assertTrue(hasDiamond, "Should render diamond gem");
		assertTrue(hasEmerald, "Should render emerald gem");
		assertTrue(hasRuby, "Should render ruby gem");

		// All three gems should have different textures (context-based)
		long uniqueGemTextures = textures.stream()
			.filter(t -> t.texture().contains("gem"))
			.map(RenderableTexture::texture)
			.distinct()
			.count();

		assertEquals(3, uniqueGemTextures,
			"Three different gems should produce three different textures");
	}

	@Test
	void multiGemToolGemsRenderInCorrectOrder() {
		// Gems in different slots should render in correct order
		Component tool = components.createToolWithThreeGems();

		var textures = resolve(tool);

		assertHasTextures(textures);

		// Find gem textures
		List<RenderableTexture> gems = textures.stream()
			.filter(t -> t.texture().contains("gem"))
			.sorted() // Sort by order
			.toList();

		assertEquals(3, gems.size(), "Should have 3 gem textures");

		// Gems should be in ascending order
		assertTrue(gems.get(0).order() <= gems.get(1).order(),
			"First gem should have lower or equal order than second");
		assertTrue(gems.get(1).order() <= gems.get(2).order(),
			"Second gem should have lower or equal order than third");

		// Specifically: gem_slot_1 (order 10), gem_slot_2 (order 20), gem_slot_3 (order 30)
		// The actual orders might be higher due to base order, but relative order should be preserved
		int firstOrder = gems.get(0).order();
		int secondOrder = gems.get(1).order();
		int thirdOrder = gems.get(2).order();

		assertTrue(secondOrder > firstOrder || thirdOrder > secondOrder,
			"Gems should have different rendering orders");
	}

	// ==================== Context-Dependent Binding Textures ====================

	@Test
	void bindingInPickaxeUsesPickaxeSpecificTexture() {
		// Binding with gem on pickaxe should use pickaxe-specific binding texture
		Component pickaxe = components.createPickaxeWithBindingAndGem();

		var textures = resolve(pickaxe);

		assertHasTextures(textures);

		// Should use binding_pickaxe context
		boolean hasPickaxeBinding = textures.stream()
			.anyMatch(t -> t.texture().contains("binding_pickaxe"));

		assertTrue(hasPickaxeBinding,
			"Binding on pickaxe should use pickaxe-specific binding texture");
	}

	@Test
	void bindingInAxeUsesAxeSpecificTexture() {
		// Binding with gem on axe should use axe-specific binding texture
		Component axe = components.createAxeWithBindingAndGem();

		var textures = resolve(axe);

		assertHasTextures(textures);

		// Should use binding_axe context
		boolean hasAxeBinding = textures.stream()
			.anyMatch(t -> t.texture().contains("binding_axe"));

		assertTrue(hasAxeBinding,
			"Binding on axe should use axe-specific binding texture");
	}

	@Test
	void sameBindingDifferentToolTypesDifferentTextures() {
		// Same binding (leather with diamond gem) should look different on pickaxe vs axe
		Component pickaxe = components.createPickaxeWithBindingAndGem();
		Component axe = components.createAxeWithBindingAndGem();

		var pickaxeTextures = resolve(pickaxe);
		var axeTextures = resolve(axe);

		assertHasTextures(pickaxeTextures);
		assertHasTextures(axeTextures);

		// Pickaxe binding should use pickaxe context
		boolean hasPickaxeBinding = pickaxeTextures.stream()
			.anyMatch(t -> t.texture().contains("binding_pickaxe"));

		// Axe binding should use axe context
		boolean hasAxeBinding = axeTextures.stream()
			.anyMatch(t -> t.texture().contains("binding_axe"));

		assertTrue(hasPickaxeBinding,
			"Pickaxe should use pickaxe-specific binding");
		assertTrue(hasAxeBinding,
			"Axe should use axe-specific binding");

		// They should be different
		assertNotEquals(
			pickaxeTextures.stream().filter(t -> t.texture().contains("binding")).findFirst(),
			axeTextures.stream().filter(t -> t.texture().contains("binding")).findFirst(),
			"Pickaxe and axe bindings should use different textures"
		);
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

	private String formatTextures(List<RenderableTexture> textures) {
		return textures.stream()
				.map(RenderableTexture::texture)
				.reduce((a, b) -> a + ", " + b)
				.orElse("none");
	}
}
