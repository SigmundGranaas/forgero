package com.sigmundgranaas.forgero.model.loading.impl;

import com.sigmundgranaas.forgero.model.loading.impl.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ModelExtensionMerger.
 */
class ModelExtensionMergerTest {

	private ModelExtensionMerger merger;

	@BeforeEach
	void setUp() {
		merger = new ModelExtensionMerger();
	}

	private ModelDTO createModel(String id, List<LayerDTO> layers, List<SlotDTO> slots, List<MountPointDTO> mountPoints) {
		return new ModelDTO(
				id,
				"forgero:composite_model",
				layers,
				slots,
				mountPoints,
				null, null, null, null, null, null
		);
	}

	private ModelExtensionDTO createExtension(String target, int priority, List<LayerDTO> layers, List<SlotDTO> slots, List<MountPointDTO> mountPoints) {
		return new ModelExtensionDTO(
				"forgero:model_extension",
				target,
				priority,
				layers,
				slots,
				mountPoints
		);
	}

	private LayerDTO createLayer(int order, String texture) {
		return new LayerDTO(order, new TexturesDTO(texture, null), null);
	}

	private SlotDTO createSlot(String id, int order) {
		return new SlotDTO(id, order, new RendererDTO("forgero:component", null), null, null);
	}

	private MountPointDTO createMountPoint(String name, int x, int y) {
		return new MountPointDTO(name, List.of(x, y));
	}

	@Nested
	class WhenNoExtensions {

		@Test
		void shouldReturnOriginalModelsUnchanged() {
			List<ModelDTO> models = List.of(
					createModel("model1", List.of(createLayer(1, "tex1")), null, null),
					createModel("model2", List.of(createLayer(2, "tex2")), null, null)
			);

			List<ModelDTO> result = merger.merge(models, List.of());

			assertEquals(2, result.size());
			assertEquals("model1", result.get(0).id());
			assertEquals("model2", result.get(1).id());
		}

		@Test
		void shouldHandleNullExtensions() {
			List<ModelDTO> models = List.of(
					createModel("model1", List.of(createLayer(1, "tex1")), null, null)
			);

			List<ModelDTO> result = merger.merge(models, null);

			assertEquals(1, result.size());
		}
	}

	@Nested
	class WhenMergingLayers {

		@Test
		void shouldAppendExtensionLayersToTarget() {
			List<ModelDTO> models = List.of(
					createModel("target-model", List.of(createLayer(1, "base-tex")), null, null)
			);
			List<ModelExtensionDTO> extensions = List.of(
					createExtension("target-model", 0, List.of(createLayer(10, "overlay-tex")), null, null)
			);

			List<ModelDTO> result = merger.merge(models, extensions);

			assertEquals(1, result.size());
			ModelDTO merged = result.get(0);
			assertNotNull(merged.layers());
			assertEquals(2, merged.layers().size());
			assertEquals(1, merged.layers().get(0).order());
			assertEquals("base-tex", merged.layers().get(0).textures().defaultTexture());
			assertEquals(10, merged.layers().get(1).order());
			assertEquals("overlay-tex", merged.layers().get(1).textures().defaultTexture());
		}

		@Test
		void shouldAddLayersToModelWithNoLayers() {
			List<ModelDTO> models = List.of(
					createModel("target-model", null, null, null)
			);
			List<ModelExtensionDTO> extensions = List.of(
					createExtension("target-model", 0, List.of(createLayer(10, "new-tex")), null, null)
			);

			List<ModelDTO> result = merger.merge(models, extensions);

			assertEquals(1, result.size());
			assertNotNull(result.get(0).layers());
			assertEquals(1, result.get(0).layers().size());
		}
	}

	@Nested
	class WhenMergingSlots {

		@Test
		void shouldAppendExtensionSlots() {
			List<ModelDTO> models = List.of(
					createModel("target-model", null, List.of(createSlot("slot1", 1)), null)
			);
			List<ModelExtensionDTO> extensions = List.of(
					createExtension("target-model", 0, null, List.of(createSlot("slot2", 2)), null)
			);

			List<ModelDTO> result = merger.merge(models, extensions);

			assertEquals(1, result.size());
			assertNotNull(result.get(0).slots());
			assertEquals(2, result.get(0).slots().size());
		}

		@Test
		void shouldOverrideDuplicateSlotIds() {
			SlotDTO originalSlot = createSlot("dye_slot", 1);
			SlotDTO extensionSlot = createSlot("dye_slot", 5); // Same ID, different order

			List<ModelDTO> models = List.of(
					createModel("target-model", null, List.of(originalSlot), null)
			);
			List<ModelExtensionDTO> extensions = List.of(
					createExtension("target-model", 0, null, List.of(extensionSlot), null)
			);

			List<ModelDTO> result = merger.merge(models, extensions);

			assertEquals(1, result.size());
			assertNotNull(result.get(0).slots());
			assertEquals(1, result.get(0).slots().size()); // Should still be 1 (override, not duplicate)
			assertEquals(5, result.get(0).slots().get(0).order()); // Extension value should win
		}
	}

	@Nested
	class WhenMergingMountPoints {

		@Test
		void shouldAppendExtensionMountPoints() {
			List<ModelDTO> models = List.of(
					createModel("target-model", null, null, List.of(createMountPoint("mount1", 0, 0)))
			);
			List<ModelExtensionDTO> extensions = List.of(
					createExtension("target-model", 0, null, null, List.of(createMountPoint("mount2", 5, 5)))
			);

			List<ModelDTO> result = merger.merge(models, extensions);

			assertEquals(1, result.size());
			assertNotNull(result.get(0).mountPoints());
			assertEquals(2, result.get(0).mountPoints().size());
		}

		@Test
		void shouldOverrideDuplicateMountPointNames() {
			MountPointDTO originalMount = createMountPoint("head_mount", 0, 0);
			MountPointDTO extensionMount = createMountPoint("head_mount", 5, 10); // Same name, different position

			List<ModelDTO> models = List.of(
					createModel("target-model", null, null, List.of(originalMount))
			);
			List<ModelExtensionDTO> extensions = List.of(
					createExtension("target-model", 0, null, null, List.of(extensionMount))
			);

			List<ModelDTO> result = merger.merge(models, extensions);

			assertEquals(1, result.size());
			assertNotNull(result.get(0).mountPoints());
			assertEquals(1, result.get(0).mountPoints().size()); // Should still be 1 (override)
			assertEquals(List.of(5, 10), result.get(0).mountPoints().get(0).position()); // Extension value should win
		}
	}

	@Nested
	class WhenExtensionTargetsUnknownModel {

		@Test
		void shouldSkipExtensionAndLogWarning() {
			List<ModelDTO> models = List.of(
					createModel("existing-model", List.of(createLayer(1, "tex")), null, null)
			);
			List<ModelExtensionDTO> extensions = List.of(
					createExtension("non-existent-model", 0, List.of(createLayer(10, "overlay")), null, null)
			);

			List<ModelDTO> result = merger.merge(models, extensions);

			// Original models should be unchanged
			assertEquals(1, result.size());
			assertEquals("existing-model", result.get(0).id());
			assertEquals(1, result.get(0).layers().size()); // No layers added from extension
		}
	}

	@Nested
	class WhenMultipleExtensionsTargetSameModel {

		@Test
		void shouldApplyExtensionsInPriorityOrder() {
			List<ModelDTO> models = List.of(
					createModel("target-model", List.of(createLayer(1, "base")), null, null)
			);
			List<ModelExtensionDTO> extensions = List.of(
					createExtension("target-model", 100, List.of(createLayer(30, "high-priority")), null, null),
					createExtension("target-model", 0, List.of(createLayer(20, "low-priority")), null, null),
					createExtension("target-model", 50, List.of(createLayer(25, "medium-priority")), null, null)
			);

			List<ModelDTO> result = merger.merge(models, extensions);

			assertEquals(1, result.size());
			assertNotNull(result.get(0).layers());
			assertEquals(4, result.get(0).layers().size());

			// Layers should be: base, low-priority (0), medium-priority (50), high-priority (100)
			assertEquals("base", result.get(0).layers().get(0).textures().defaultTexture());
			assertEquals("low-priority", result.get(0).layers().get(1).textures().defaultTexture());
			assertEquals("medium-priority", result.get(0).layers().get(2).textures().defaultTexture());
			assertEquals("high-priority", result.get(0).layers().get(3).textures().defaultTexture());
		}
	}

	@Nested
	class WhenModelHasNoId {

		@Test
		void shouldSkipModelsWithNullId() {
			List<ModelDTO> models = new ArrayList<>();
			models.add(createModel(null, List.of(createLayer(1, "tex")), null, null));
			models.add(createModel("valid-model", List.of(createLayer(2, "tex2")), null, null));

			List<ModelExtensionDTO> extensions = List.of(
					createExtension("valid-model", 0, List.of(createLayer(10, "overlay")), null, null)
			);

			List<ModelDTO> result = merger.merge(models, extensions);

			// Only the model with valid ID should be in the result map
			// The null-id model is not added to the map
			assertEquals(1, result.size());
			assertEquals("valid-model", result.get(0).id());
			assertEquals(2, result.get(0).layers().size()); // Original + extension layer
		}
	}
}
