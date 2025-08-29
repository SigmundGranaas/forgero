package com.sigmundgranaas.forgero.model.loading.impl.dto;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.Nullable;
import java.util.List;
import java.util.Optional;

public record TexturesDTO(
		String defaultTexture,
		@Nullable List<VariantDTO> variants
) {
	public Optional<List<VariantDTO>> getVariants() {
		return Optional.ofNullable(variants);
	}

	public static final Codec<TexturesDTO> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("default").forGetter(TexturesDTO::defaultTexture),
			Codec.list(VariantDTO.CODEC).optionalFieldOf("variants").forGetter(TexturesDTO::getVariants)
	).apply(instance, (def, variants) -> new TexturesDTO(def, variants.orElse(List.of()))));
}
