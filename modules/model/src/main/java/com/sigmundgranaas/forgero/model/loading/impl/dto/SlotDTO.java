package com.sigmundgranaas.forgero.model.loading.impl.dto;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record SlotDTO(
		String id,
		int order,
		RendererDTO renderer,
		@Nullable String targetMount,
		@Nullable String childMount,
		@Nullable String dynamicKey
) {
	public Optional<String> getTargetMount() {
		return Optional.ofNullable(targetMount);
	}

	public Optional<String> getChildMount() {
		return Optional.ofNullable(childMount);
	}

	public Optional<String> getDynamicKey() {
		return Optional.ofNullable(dynamicKey);
	}

	public static final Codec<SlotDTO> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("id").forGetter(SlotDTO::id),
			Codec.INT.fieldOf("order").forGetter(SlotDTO::order),
			RendererDTO.CODEC.fieldOf("renderer").forGetter(SlotDTO::renderer),
			Codec.STRING.optionalFieldOf("mount").forGetter(SlotDTO::getTargetMount),
			Codec.STRING.optionalFieldOf("child_mount").forGetter(SlotDTO::getChildMount),
			Codec.STRING.optionalFieldOf("dynamic_key").forGetter(SlotDTO::getDynamicKey)
	).apply(instance, (id, order, renderer, target, child, dynamic) -> new SlotDTO(id, order, renderer, target.orElse(null), child.orElse(null), dynamic.orElse(null))));
}
