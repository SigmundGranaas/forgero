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
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Behavioral tests for rendering order and z-layering.
 *
 * The rendering order system determines which textures appear in front/back. Lower order
 * values render first (further back), higher values render later (on top).
 *
 * Key behaviors:
 * - Slot order determines z-position
 * - Higher order renders on top of lower order
 * - Negative orders supported for background layers
 * - Order preserved through dynamic slots
 * - Deterministic tie-breaking when orders equal
 */
class RenderingOrderTest {

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

	// ==================== Basic Ordering ====================

	@Test
	void slotsRenderInOrderedSequence() {
		// Components with different order values should render in order
		Component layeredTool = components.createLayeredTool();

		var textures = resolve(layeredTool);

		assertHasTextures(textures);
		// Verify textures are sorted by order
		List<Integer> orders = textures.stream().map(RenderableTexture::order).toList();
		List<Integer> sortedOrders = new ArrayList<>(orders);
		Collections.sort(sortedOrders);
		assertEquals(sortedOrders, orders,
			"Textures should be sorted by order (lower first)");
	}

	@Test
	void higherOrderRendersOnTop() {
		// Higher order values appear later in the list (render on top)
		Component layeredTool = components.createLayeredTool();

		var textures = resolve(layeredTool);

		assertHasTextures(textures);
		// Base (order 1) should come before overlay (order 10) which comes before gem (order 100)
		int baseIndex = findTextureIndex(textures, "base");
		int overlayIndex = findTextureIndex(textures, "overlay");
		int gemIndex = findTextureIndex(textures, "gem");

		assertTrue(baseIndex >= 0, "Should have base texture");
		assertTrue(overlayIndex >= 0, "Should have overlay texture");
		assertTrue(gemIndex >= 0, "Should have gem texture");
		assertTrue(baseIndex < overlayIndex, "Base should render before overlay");
		assertTrue(overlayIndex < gemIndex, "Overlay should render before gem");
	}

	@Test
	void negativeOrdersRenderFirst() {
		// Negative orders render before zero/positive orders
		Component withBackground = components.createToolWithBackground();

		var textures = resolve(withBackground);

		assertHasTextures(textures);
		// Background (order -10) should come before normal layers
		int backgroundIndex = findTextureIndex(textures, "background");
		int normalIndex = findTextureIndex(textures, "normal");

		assertTrue(backgroundIndex >= 0, "Should have background texture");
		assertTrue(normalIndex >= 0, "Should have normal texture");
		assertTrue(backgroundIndex < normalIndex, "Background (negative order) should render before normal (positive order)");
	}

	// ==================== Order Preservation ====================

	@Test
	void orderPreservedThroughDynamicSlots() {
		// Dynamic slots should respect the slot's order value
		Component arrow = components.createArrow();
		Component bow = components.createBowWithArrow(arrow);

		Map<String, Object> state = Map.of(
			"pulling", true,
			"pull", 0.65f,
			"equippedArrow", arrow
		);

		var textures = resolve(bow, state);

		assertHasTextures(textures);
		// Arrow should render with the order specified in the bow's dynamic slot
		// Bow limb is typically order 1, arrow should be higher (on top)
		int limbIndex = findTextureIndex(textures, "limb");
		int arrowIndex = findTextureIndex(textures, "arrow");

		if (limbIndex >= 0 && arrowIndex >= 0) {
			// If both are present, arrow should be after limb (higher order)
			assertTrue(arrowIndex >= limbIndex, "Arrow should render at or after bow limb (higher order)");
		}
	}

	@Test
	void slotOrderIndependentOfDefinitionOrder() {
		// Order field determines rendering, not the order slots are defined in JSON
		Component layeredTool = components.createLayeredTool();

		var textures = resolve(layeredTool);

		assertHasTextures(textures);
		// Even if slots defined in different order in JSON, rendering respects order field
		List<Integer> orders = textures.stream().map(RenderableTexture::order).toList();
		// Orders should be in ascending sequence
		for (int i = 1; i < orders.size(); i++) {
			assertTrue(orders.get(i) >= orders.get(i - 1),
				"Orders should be in ascending sequence regardless of definition order");
		}
	}

	// ==================== Edge Cases ====================

	@Test
	void defaultOrderWhenUnspecified() {
		// When slot doesn't specify order, should use sensible default (0 or baseOrder)
		Component simpleComponent = components.createArrow();

		var textures = resolve(simpleComponent);

		assertHasTextures(textures);
		// All textures should have valid order values (not null, not undefined)
		textures.forEach(tex ->
			assertNotNull(tex.order(), "Texture should have defined order value"));
	}

	@Test
	void tieBreakingWhenOrdersEqual() {
		// When two textures have same order, should break tie deterministically
		Component twinSlots = components.createComponentWithEqualOrders();

		var textures = resolve(twinSlots);

		assertHasTextures(textures);
		// Find textures with same order
		Map<Integer, List<RenderableTexture>> byOrder = textures.stream()
			.collect(Collectors.groupingBy(RenderableTexture::order));

		// For any ties, verify deterministic ordering (by texture name)
		byOrder.forEach((order, texList) -> {
			if (texList.size() > 1) {
				// Tie-breaking should use texture name (alphabetical) as secondary sort
				List<String> textureNames = texList.stream()
					.map(RenderableTexture::texture)
					.toList();
				List<String> sorted = new ArrayList<>(textureNames);
				Collections.sort(sorted);
				assertEquals(sorted, textureNames,
					"Textures with same order should be sorted by name (deterministic tie-breaking)");
			}
		});
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

	private int findTextureIndex(List<RenderableTexture> textures, String substring) {
		for (int i = 0; i < textures.size(); i++) {
			if (textures.get(i).texture().contains(substring)) {
				return i;
			}
		}
		return -1;
	}

	// ==================== Assertion Helpers ====================

	private void assertHasTextures(List<RenderableTexture> textures) {
		assertFalse(textures.isEmpty(), "Should have textures");
	}
}
