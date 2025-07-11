package com.sigmundgranaas.forgero.model.loading.impl.dto;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public record VariantDTO(
		List<PredicateDTO> predicate,
		@Nullable String texture,
		@Nullable String model,
		@Nullable int[] offset
) {
	public Optional<String> getTexture() {
		return Optional.ofNullable(texture);
	}

	public Optional<String> getModel() {
		return Optional.ofNullable(model);
	}

	public Optional<int[]> getOffsetAsOptional() {
		return Optional.ofNullable(offset);
	}

	public static final Codec<VariantDTO> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.list(PredicateDTO.CODEC).fieldOf("predicate").forGetter(VariantDTO::predicate),
			Codec.STRING.optionalFieldOf("texture").forGetter(VariantDTO::getTexture),
			Codec.STRING.optionalFieldOf("model").forGetter(VariantDTO::getModel),
			Codec.list(Codec.INT).xmap(
					list -> list.stream().mapToInt(Integer::intValue).toArray(),
					array -> Arrays.stream(array).boxed().toList()
			).optionalFieldOf("offset").forGetter(dto -> Optional.ofNullable(dto.offset()))
	).apply(instance, (predicate, texture, model, offset) -> new VariantDTO(predicate, texture.orElse(null), model.orElse(null), offset.orElse(null))));
}
