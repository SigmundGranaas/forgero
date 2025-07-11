package com.sigmundgranaas.forgero.model.loading.impl.dto;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public record ModelDTO(
		@Nullable OpenIdentifier id,
		String type,
		@Nullable List<LayerDTO> layers,
		@Nullable List<SlotDTO> slots,
		@Nullable String texture,
		@Nullable TexturesDTO textures,
		@Nullable String target,
		@Nullable String context
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

	public Optional<OpenIdentifier> getOpenIdentifierId() {
		return Optional.ofNullable(id);
	}
}
