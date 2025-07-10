package com.sigmundgranaas.forgero.model.loading.impl.dto;

import com.google.gson.JsonElement;
import org.jetbrains.annotations.Nullable;

public record StaticModelDTO(
		String model_type,
		String texture,
		@Nullable JsonElement display
) implements ModelDTO {}
