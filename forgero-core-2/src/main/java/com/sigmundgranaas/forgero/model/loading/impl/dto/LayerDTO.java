package com.sigmundgranaas.forgero.model.loading.impl.dto;

import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record LayerDTO(
		int order,
		TexturesDTO textures,
		@Nullable int[] offset
) {
	public Optional<int[]> getOffsetAsOptional() {
		return Optional.ofNullable(offset);
	}
}
