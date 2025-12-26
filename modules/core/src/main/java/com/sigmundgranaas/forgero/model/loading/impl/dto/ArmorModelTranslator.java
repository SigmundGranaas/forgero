package com.sigmundgranaas.forgero.model.loading.impl.dto;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.model.api.armor.ArmorModel;
import com.sigmundgranaas.forgero.model.api.ModelLayer;
import com.sigmundgranaas.forgero.model.api.ModelSlot;
import com.sigmundgranaas.forgero.model.loading.impl.ModelTranslator;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Translates ArmorModelDTO to ArmorModel domain objects.
 * Extends ModelTranslator to reuse layer and slot translation logic.
 */
public class ArmorModelTranslator extends ModelTranslator {

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
}
