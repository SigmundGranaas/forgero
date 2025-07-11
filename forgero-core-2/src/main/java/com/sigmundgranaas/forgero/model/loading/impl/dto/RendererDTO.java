package com.sigmundgranaas.forgero.model.loading.impl.dto;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.Nullable;
import java.util.Optional;

public record RendererDTO(
		String type,
		@Nullable String context
) {
	public Optional<String> getContext() {
		return Optional.ofNullable(context);
	}


	public static final Codec<RendererDTO> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("type").forGetter(RendererDTO::type),
			Codec.STRING.optionalFieldOf("context").forGetter(RendererDTO::getContext)
	).apply(instance, (type, context) -> new RendererDTO(type, context.orElse(null))));
}
