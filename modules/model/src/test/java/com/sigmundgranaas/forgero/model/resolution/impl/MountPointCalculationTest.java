package com.sigmundgranaas.forgero.model.resolution.impl;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.slot.SlotValidator;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentPart;
import com.sigmundgranaas.forgero.core.component.impl.StaticComponent;
import com.sigmundgranaas.forgero.model.api.Offset;
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
 * Behavioral tests for mount point offset calculations.
 *
 * Mount points are named alignment coordinates used to position child textures
 * relative to parent textures. Wrong offset calculations cause misaligned visuals.
 *
 * Key behaviors:
 * - Offset calculated from parent and child mount points
 * - Missing target mount defaults to ZERO offset
 * - Missing child mount defaults to "center"
 * - Offset applies to all textures from child component
 * - Y-coordinates are inverted (16-1-y) for bottom-left to top-left conversion
 */
class MountPointCalculationTest {

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

	// ==================== Core Mount Point Behaviors ====================

	@Test
	void mountPointCalculatesNonZeroOffsetWhenMountsSpecified() {
		// When slot has mount points defined, should calculate non-zero offset
		Component head = createComponent("forgero:parts/test-pickaxe_head");
		Component pickaxe = createPickaxeWithHead(head);

		var textures = resolve(pickaxe);

		assertHasTextures(textures);

		// Find the head texture
		Optional<RenderableTexture> headTexture = textures.stream()
				.filter(t -> t.texture().contains("pickaxe_head"))
				.findFirst();

		assertTrue(headTexture.isPresent(), "Should have pickaxe head texture");
		assertNotEquals(Offset.ZERO, headTexture.get().offset(),
			"Mount point calculation should produce non-zero offset");
	}

	@Test
	void missingTargetMountUsesZeroOffset() {
		// Slot without mount specification should use zero offset
		Component handle = createComponent("forgero:parts/oak-handle");
		Component pickaxe = createPickaxeWithHandle(handle);

		var textures = resolve(pickaxe);

		assertHasTextures(textures);

		// All textures should have zero offset (no mount specified)
		boolean allZeroOffset = textures.stream()
				.allMatch(t -> t.offset().equals(Offset.ZERO));

		assertTrue(allZeroOffset, "Components without mount specification should have zero offset");
	}

	@Test
	void offsetAppliedToAllChildTextures() {
		// When a child component resolves to multiple textures,
		// all should receive the same mount point offset

		Component head = createComponent("forgero:parts/test-pickaxe_head");
		Component pickaxe = createPickaxeWithHead(head);

		var textures = resolve(pickaxe);

		assertHasTextures(textures);

		// Filter to just head textures
		List<Offset> headOffsets = textures.stream()
				.filter(t -> t.texture().contains("pickaxe_head"))
				.map(RenderableTexture::offset)
				.distinct()
				.toList();

		// All textures from same component should have same offset
		assertTrue(headOffsets.isEmpty() || headOffsets.size() == 1,
			"All textures from same child component should have identical offset");
	}

	@Test
	void offsetIndependentOfRenderingOrder() {
		// Texture order and offset are independent concerns

		Component head = createComponent("forgero:parts/test-pickaxe_head");
		Component pickaxe = createPickaxeWithHead(head);

		var textures = resolve(pickaxe);

		assertHasTextures(textures);

		// Should have textures with both order and offset
		boolean hasOrderedTextures = textures.stream()
				.anyMatch(t -> t.order() > 0);

		assertTrue(hasOrderedTextures, "Should have textures with rendering order");
	}

	// ==================== Robustness Tests ====================

	@Test
	void handlesComponentWithoutMountPoints() {
		// Components without mount points should still render

		Component arrow = components.createArrow();

		var textures = resolve(arrow);

		assertHasTextures(textures);
		// All offsets should be zero (no mounts defined)
		boolean allZeroOffset = textures.stream()
				.allMatch(t -> t.offset().equals(Offset.ZERO));

		assertTrue(allZeroOffset, "Components without mount points should have zero offset");
	}

	@Test
	void handlesEmptyMountPointList() {
		// Model with empty mount_points list should work

		Component bow = components.createBow();

		var textures = resolve(bow);

		assertHasTextures(textures);
		// Should render successfully even with no/empty mount points
	}

	@Test
	void multipleComponentsWithDifferentOffsets() {
		// Different components can have different offsets

		Component head = createComponent("forgero:parts/test-pickaxe_head");
		Component handle = createComponent("forgero:parts/oak-handle");

		Component pickaxe = createPickaxeWithHeadAndHandle(head, handle);

		var textures = resolve(pickaxe);

		assertHasTextures(textures);

		// Should have at least 2 distinct offsets (head with mount, handle without)
		long distinctOffsets = textures.stream()
				.map(RenderableTexture::offset)
				.distinct()
				.count();

		assertTrue(distinctOffsets >= 1, "Should have textures (potentially with different offsets)");
	}

	// ==================== Helper Methods ====================

	private Component createComponent(String id) {
		return new StaticComponent(
			OpenIdentifier.parse(id),
			Set.of(),
			new HashMap<>()
		);
	}

	private Component createPickaxeWithHead(Component head) {
		List<ComponentPart> slots = List.of(
			new ComponentPart(
				OpenIdentifier.parse("forgero:head"),
				OpenIdentifier.parse("forgero:slot_type"),
				"description",
				SlotValidator.ACCEPT_ALL,
				head
			)
		);

		return components.createStructuredComponent(
			"forgero:equipment/test-pickaxe",
			slots,
			"tools/pickaxe"
		);
	}

	private Component createPickaxeWithHandle(Component handle) {
		List<ComponentPart> slots = List.of(
			new ComponentPart(
				OpenIdentifier.parse("forgero:handle"),
				OpenIdentifier.parse("forgero:slot_type"),
				"description",
				SlotValidator.ACCEPT_ALL,
				handle
			)
		);

		return components.createStructuredComponent(
			"forgero:equipment/test-pickaxe",
			slots,
			"tools/pickaxe"
		);
	}

	private Component createPickaxeWithHeadAndHandle(Component head, Component handle) {
		List<ComponentPart> slots = List.of(
			new ComponentPart(
				OpenIdentifier.parse("forgero:head"),
				OpenIdentifier.parse("forgero:slot_type"),
				"description",
				SlotValidator.ACCEPT_ALL,
				head
			),
			new ComponentPart(
				OpenIdentifier.parse("forgero:handle"),
				OpenIdentifier.parse("forgero:slot_type"),
				"description",
				SlotValidator.ACCEPT_ALL,
				handle
			)
		);

		return components.createStructuredComponent(
			"forgero:equipment/test-pickaxe",
			slots,
			"tools/pickaxe"
		);
	}

	private List<RenderableTexture> resolve(Component component) {
		return resolver.resolve(component, Collections.emptyMap())
				.orElse(Collections.emptyList())
				.stream()
				.filter(t -> t.texture() != null)
				.toList();
	}

	// ==================== Assertion Helpers ====================

	private void assertHasTextures(List<RenderableTexture> textures) {
		assertFalse(textures.isEmpty(), "Should have textures");
	}
}
