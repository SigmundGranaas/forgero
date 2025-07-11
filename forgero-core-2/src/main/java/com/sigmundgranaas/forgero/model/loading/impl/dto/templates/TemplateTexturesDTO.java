package com.sigmundgranaas.forgero.model.loading.impl.dto.templates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.model.loading.impl.codec.ModelTemplateCodecs;
import org.jetbrains.annotations.Nullable;

/**
 * DTO for the "textures" block within a template.
 * It contains a 'generation' block instead of a 'default' texture string.
 */
public record TemplateTexturesDTO(
		@Nullable GenerationDTO generation
) {
	public static final Codec<TemplateTexturesDTO> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					ModelTemplateCodecs.GENERATION_DTO_CODEC.optionalFieldOf("generation").forGetter(dto -> java.util.Optional.ofNullable(dto.generation))
			).apply(instance, gen -> new TemplateTexturesDTO(gen.orElse(null))));
}
