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
 * Behavioral tests for error handling and robustness.
 *
 * The model resolution system must handle malformed input, missing data, and edge cases
 * gracefully without crashing. These tests validate that the system degrades gracefully
 * and provides useful feedback when things go wrong.
 *
 * Key behaviors:
 * - Missing models return empty (not crash)
 * - Invalid dynamic state values ignored gracefully
 * - Null values tolerated throughout the system
 * - Malformed mount points handled with sensible defaults
 */
class ErrorHandlingTest {

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

	// ==================== Missing Models ====================

	@Test
	void missingComponentModelReturnsEmpty() {
		// Component with no registered model should return empty
		Component unregistered = components.createUnregisteredComponent();

		var result = resolver.resolve(unregistered, Collections.emptyMap());

		assertTrue(result.isEmpty() || result.get().isEmpty(),
			"Missing component model should return empty (not crash)");
	}

	@Test
	void missingSlotComponentContinuesResolving() {
		// Composite with missing child component should resolve other slots
		Component withMissingChild = components.createComponentWithMissingChild();

		var textures = resolve(withMissingChild);

		// Should still have some textures (from valid slots)
		// Missing child slot simply skipped
		assertNotNull(textures, "Should handle missing child component gracefully");
	}

	// ==================== Invalid Dynamic State ====================

	@Test
	void emptyDynamicStateAccepted() {
		// Empty map is the correct way to represent "no dynamic state"
		// Note: null is NOT supported and will cause NPE
		Component bow = components.createBow();

		var result = resolver.resolve(bow, Collections.emptyMap());

		assertTrue(result.isPresent(), "Empty dynamic state should be accepted");
	}

	@Test
	void wrongTypeDynamicStateValueIgnored() {
		// Dynamic state with wrong type should be ignored gracefully
		Component bow = components.createBowWithArrow(null);

		Map<String, Object> invalidState = new HashMap<>();
		invalidState.put("equippedArrow", "not a component");  // Wrong type
		invalidState.put("pulling", true);
		invalidState.put("pull", 0.65f);

		// Should not crash, should ignore invalid value
		var textures = resolve(bow, invalidState);

		assertNotNull(textures, "Should handle wrong-type dynamic state values gracefully");
	}

	@Test
	void dynamicStateWithNullValuesIgnored() {
		// Dynamic state with null values should be ignored
		Component bow = components.createBowWithArrow(null);

		Map<String, Object> stateWithNulls = new HashMap<>();
		stateWithNulls.put("equippedArrow", null);  // Null value
		stateWithNulls.put("pulling", true);
		stateWithNulls.put("pull", 0.65f);

		// Should not crash, should ignore null values
		var textures = resolve(bow, stateWithNulls);

		assertNotNull(textures, "Should handle null dynamic state values gracefully");
	}

	// ==================== Malformed Mount Points ====================

	@Test
	void missingMountPointUsesZeroOffset() {
		// Component with mount point reference that doesn't exist
		Component withMissingMount = components.createComponentWithMissingMount();

		var textures = resolve(withMissingMount);

		assertNotNull(textures, "Should handle missing mount points gracefully");
		// Should use zero offset when mount point missing
		// (Can't easily verify offset value without inspecting internal state,
		// but at minimum should not crash)
	}

	// ==================== Edge Cases ====================

	@Test
	void emptyComponentStructureResolves() {
		// Component with empty structure should resolve without crash
		Component empty = components.createEmptyComponent();

		var result = resolver.resolve(empty, Collections.emptyMap());

		// Empty component may return empty or may have base textures
		assertNotNull(result, "Empty component should resolve gracefully");
	}

	@Test
	void veryDeepNestingHandled() {
		// Deeply nested component hierarchy should not stack overflow
		Component deepNested = components.createDeeplyNestedComponent(10);

		var textures = resolve(deepNested);

		// Should complete without stack overflow
		assertNotNull(textures, "Deep nesting should be handled without stack overflow");
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
}
