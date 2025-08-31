package com.sigmundgranaas.forgero.model.loading.impl.dto;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public record ArmorModelDTO(
		@Nullable OpenIdentifier id,
		String type,
		String model,
		List<LayerDTO> textures,
		List<SlotDTO> slots,
		@Nullable String target,
		@Nullable String context
) {
	public Optional<OpenIdentifier> getId() {
		return Optional.ofNullable(id);
	}

	public Optional<String> getTarget() {
		return Optional.ofNullable(target);
	}

	public Optional<String> getContext() {
		return Optional.ofNullable(context);
	}
}
