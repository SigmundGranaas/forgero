package com.sigmundgranaas.forgero.model.loading.impl.dto;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public record ModelDTO(
		String type,
		@Nullable List<LayerDTO> layers,
		@Nullable List<SlotDTO> slots,
		@Nullable String texture, // For simple texture models
		@Nullable TexturesDTO textures, // For texture blocks with variants
		@Nullable String target, // The base component ID this model is for
		@Nullable String context // The context this model belongs to
) {
	public Optional<List<LayerDTO>> getLayers() {
		return Optional.ofNullable(layers);
	}

	public Optional<List<SlotDTO>> getSlots() {
		return Optional.ofNullable(slots);
	}

	public Optional<String> getTexture() {
		return Optional.ofNullable(texture);
	}

	public Optional<TexturesDTO> getTextures() {
		return Optional.ofNullable(textures);
	}

	public Optional<String> getTarget() {
		return Optional.ofNullable(target);
	}

	public Optional<String> getContext() {
		return Optional.ofNullable(context);
	}
}
