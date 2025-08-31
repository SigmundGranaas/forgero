package com.sigmundgranaas.forgero.model.loading.impl.dto;

import org.jetbrains.annotations.Nullable;

public record LayerDTO(
		int order,
		TexturesDTO textures,
		@Nullable int[] offset
) {
}
