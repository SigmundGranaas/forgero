package com.sigmundgranaas.forgero.model.loading.impl.dto;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.model.api.armor.ArmorModel;
import com.sigmundgranaas.forgero.model.api.ModelLayer;
import com.sigmundgranaas.forgero.model.api.ModelSlot;
import com.sigmundgranaas.forgero.model.loading.impl.ModelTranslator;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ArmorModelTranslator {
	private final ModelTranslator itemModelTranslator;

	public ArmorModelTranslator() {
		this.itemModelTranslator = new ModelTranslator();
	}

	public ArmorModel toDomain(OpenIdentifier fileDerivedId, ArmorModelDTO dto) {
		OpenIdentifier finalId = dto.getId().orElse(fileDerivedId);

		List<ModelLayer> layers = Optional.ofNullable(dto.layers())
				.orElse(Collections.emptyList())
				.stream()
				.map(this::toModelLayer)
				.toList();

		List<ModelSlot> slots = Optional.ofNullable(dto.slots())
				.orElse(Collections.emptyList())
				.stream()
				.map(this::toModelSlot)
				.toList();

		OpenIdentifier modelId = OpenIdentifier.parse(dto.model());
		Optional<OpenIdentifier> target = dto.getTarget().map(OpenIdentifier::parse);
		Optional<String> context = dto.getContext();

		return new ArmorModel(finalId, modelId, layers, slots, target, context);
	}

	// HACK: Accessing private methods from ModelTranslator.
	// In a real scenario, these should be made public or moved to a shared utility.
	private ModelLayer toModelLayer(LayerDTO dto) {
		try {
			Method method = ModelTranslator.class.getDeclaredMethod("toModelLayer", LayerDTO.class);
			method.setAccessible(true);
			return (ModelLayer) method.invoke(itemModelTranslator, dto);
		} catch (Exception e) {
			throw new RuntimeException("Failed to reflectively call toModelLayer", e);
		}
	}

	private ModelSlot toModelSlot(SlotDTO dto) {
		try {
			Method method = ModelTranslator.class.getDeclaredMethod("toModelSlot", SlotDTO.class);
			method.setAccessible(true);
			return (ModelSlot) method.invoke(itemModelTranslator, dto);
		} catch (Exception e) {
			throw new RuntimeException("Failed to reflectively call toModelSlot", e);
		}
	}
}
