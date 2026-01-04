package com.sigmundgranaas.forgero.model.loading.impl;

import com.sigmundgranaas.forgero.model.loading.impl.dto.LayerDTO;
import com.sigmundgranaas.forgero.model.loading.impl.dto.ModelDTO;
import com.sigmundgranaas.forgero.model.loading.impl.dto.ModelExtensionDTO;
import com.sigmundgranaas.forgero.model.loading.impl.dto.MountPointDTO;
import com.sigmundgranaas.forgero.model.loading.impl.dto.SlotDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Merges model extensions into their target models.
 *
 * <p>This merger is called after manual models are loaded but before they are
 * translated to domain objects. Extensions are sorted by priority (lowest first)
 * and applied in order to their target models.</p>
 *
 * <h3>Merge Semantics:</h3>
 * <ul>
 *   <li><strong>Layers:</strong> Concatenated (extension layers appended after target layers)</li>
 *   <li><strong>Slots:</strong> Merged by ID; extension wins with warning for duplicates</li>
 *   <li><strong>MountPoints:</strong> Merged by name; extension wins with warning for duplicates</li>
 * </ul>
 */
public class ModelExtensionMerger {
	private static final Logger LOGGER = LoggerFactory.getLogger(ModelExtensionMerger.class);

	/**
	 * Merges extensions into the provided model DTOs.
	 *
	 * @param models     The list of model DTOs to merge into
	 * @param extensions The list of extensions to apply
	 * @return A new list of merged model DTOs
	 */
	public List<ModelDTO> merge(List<ModelDTO> models, List<ModelExtensionDTO> extensions) {
		if (extensions == null || extensions.isEmpty()) {
			return models;
		}

		// Sort extensions by priority (lowest first)
		List<ModelExtensionDTO> sorted = extensions.stream()
				.sorted(Comparator.comparingInt(ModelExtensionDTO::priority))
				.toList();

		// Build map of models by ID
		Map<String, ModelDTO> modelMap = new LinkedHashMap<>();
		for (ModelDTO model : models) {
			if (model.id() != null) {
				modelMap.put(model.id(), model);
			}
		}

		// Apply extensions
		for (ModelExtensionDTO extension : sorted) {
			String targetId = extension.target();
			ModelDTO target = modelMap.get(targetId);
			if (target == null) {
				LOGGER.warn("Model extension targets unknown model: {}", targetId);
				continue;
			}

			LOGGER.debug("Applying model extension to {}", targetId);
			modelMap.put(targetId, mergeInto(target, extension));
		}

		return new ArrayList<>(modelMap.values());
	}

	/**
	 * Merges a single extension into a target model.
	 */
	private ModelDTO mergeInto(ModelDTO target, ModelExtensionDTO extension) {
		return new ModelDTO(
				target.id(),
				target.type(),
				mergeLayers(target.layers(), extension.layers()),
				mergeSlots(target.slots(), extension.slots()),
				mergeMountPoints(target.mountPoints(), extension.mountPoints()),
				target.texture(),
				target.textures(),
				target.target(),
				target.context(),
				target.parent(),
				target.display()
		);
	}

	/**
	 * Merges layers by concatenation.
	 * Extension layers are appended after target layers.
	 */
	private List<LayerDTO> mergeLayers(List<LayerDTO> target, List<LayerDTO> extension) {
		if (extension == null || extension.isEmpty()) {
			return target;
		}
		if (target == null || target.isEmpty()) {
			return extension;
		}

		List<LayerDTO> merged = new ArrayList<>(target);
		merged.addAll(extension);
		return merged;
	}

	/**
	 * Merges slots by ID.
	 * Extension slots override target slots with the same ID (with warning).
	 */
	private List<SlotDTO> mergeSlots(List<SlotDTO> target, List<SlotDTO> extension) {
		if (extension == null || extension.isEmpty()) {
			return target;
		}
		if (target == null || target.isEmpty()) {
			return extension;
		}

		Map<String, SlotDTO> slotMap = new LinkedHashMap<>();
		for (SlotDTO slot : target) {
			slotMap.put(slot.id(), slot);
		}
		for (SlotDTO slot : extension) {
			if (slotMap.containsKey(slot.id())) {
				LOGGER.warn("Model extension overriding slot ID '{}'", slot.id());
			}
			slotMap.put(slot.id(), slot);
		}
		return new ArrayList<>(slotMap.values());
	}

	/**
	 * Merges mount points by name.
	 * Extension mount points override target mount points with the same name (with warning).
	 */
	private List<MountPointDTO> mergeMountPoints(List<MountPointDTO> target, List<MountPointDTO> extension) {
		if (extension == null || extension.isEmpty()) {
			return target;
		}
		if (target == null || target.isEmpty()) {
			return extension;
		}

		Map<String, MountPointDTO> mountMap = new LinkedHashMap<>();
		for (MountPointDTO mount : target) {
			mountMap.put(mount.name(), mount);
		}
		for (MountPointDTO mount : extension) {
			if (mountMap.containsKey(mount.name())) {
				LOGGER.warn("Model extension overriding mount point '{}'", mount.name());
			}
			mountMap.put(mount.name(), mount);
		}
		return new ArrayList<>(mountMap.values());
	}
}
