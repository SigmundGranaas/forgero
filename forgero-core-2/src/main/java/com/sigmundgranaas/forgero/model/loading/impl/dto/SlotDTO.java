package com.sigmundgranaas.forgero.model.loading.impl.dto;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record SlotDTO(
		String id,
		int order,
		RendererDTO renderer
) {
	public static final Codec<SlotDTO> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("id").forGetter(SlotDTO::id),
			Codec.INT.fieldOf("order").forGetter(SlotDTO::order),
			RendererDTO.CODEC.fieldOf("renderer").forGetter(SlotDTO::renderer)
	).apply(instance, SlotDTO::new));
}
