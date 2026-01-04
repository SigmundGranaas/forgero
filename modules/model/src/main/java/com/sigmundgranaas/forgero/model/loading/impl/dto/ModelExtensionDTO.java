package com.sigmundgranaas.forgero.model.loading.impl.dto;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * DTO for {@code forgero:model_extension} type files.
 * Extensions contribute visual elements to existing model definitions.
 *
 * <p>Model extensions are merged into their target model during the loading pipeline,
 * after manual models are loaded but before they are translated to domain objects.
 * Multiple extensions can target the same model and are applied in priority order (lowest first).</p>
 *
 * <h3>Merge Semantics:</h3>
 * <ul>
 *   <li><strong>Layers:</strong> Concatenate (extension layers appended after target layers)</li>
 *   <li><strong>Slots:</strong> By ID - extension wins with warning for duplicates</li>
 *   <li><strong>MountPoints:</strong> By name - extension wins with warning for duplicates</li>
 * </ul>
 *
 * <h3>Example Use Case:</h3>
 * <p>A dyes module can add dye overlay layers to all tool models:</p>
 * <pre>{@code
 * // dye-layer-extension.json
 * {
 *   "type": "forgero:model_extension",
 *   "target": "forgero:parts/iron-pickaxe_head",
 *   "priority": 100,
 *   "layers": [
 *     {
 *       "order": 10,
 *       "textures": {
 *         "default": "forgero:item/overlays/dye_overlay"
 *       }
 *     }
 *   ]
 * }
 * }</pre>
 *
 * @param type        The type identifier, always "forgero:model_extension".
 * @param target      The model ID to extend (e.g., "forgero:parts/iron-pickaxe_head").
 * @param priority    The merge priority. Lower values are applied first (default: 0).
 * @param layers      Optional list of layers to add to the target model.
 * @param slots       Optional list of slots to add to the target model.
 * @param mountPoints Optional list of mount points to add to the target model.
 */
public record ModelExtensionDTO(
		String type,
		String target,
		int priority,
		@Nullable List<LayerDTO> layers,
		@Nullable List<SlotDTO> slots,
		@Nullable List<MountPointDTO> mountPoints
) {
	/**
	 * The type identifier for model extension resources.
	 */
	public static final String TYPE_ID = "forgero:model_extension";

	/**
	 * Default priority for model extensions when not specified.
	 */
	public static final int DEFAULT_PRIORITY = 0;

	/**
	 * Creates a ModelExtensionDTO with default priority.
	 */
	public ModelExtensionDTO(
			String type,
			String target,
			@Nullable List<LayerDTO> layers,
			@Nullable List<SlotDTO> slots,
			@Nullable List<MountPointDTO> mountPoints
	) {
		this(type, target, DEFAULT_PRIORITY, layers, slots, mountPoints);
	}

	public Optional<List<LayerDTO>> getLayers() {
		return Optional.ofNullable(layers);
	}

	public Optional<List<SlotDTO>> getSlots() {
		return Optional.ofNullable(slots);
	}

	public Optional<List<MountPointDTO>> getMountPoints() {
		return Optional.ofNullable(mountPoints);
	}
}
