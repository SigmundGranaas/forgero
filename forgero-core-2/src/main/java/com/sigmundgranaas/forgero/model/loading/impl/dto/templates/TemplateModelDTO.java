package com.sigmundgranaas.forgero.model.loading.impl.dto.templates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.model.loading.impl.dto.SlotDTO;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * DTO for the "model" block inside a template file.
 * This is the template counterpart to the final ModelDTO.
 */
public record TemplateModelDTO(
		String type,
		@Nullable List<TemplateLayerDTO> layers,
		@Nullable List<SlotDTO> slots,      // Slots can be reused as they don't contain generation logic
		@Nullable TemplateTexturesDTO textures, // For simple texture models
		@Nullable String target,
		@Nullable String context
) {
	public static final Codec<TemplateLayerDTO> LAYER_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.fieldOf("order").forGetter(TemplateLayerDTO::order),
			TemplateTexturesDTO.CODEC.fieldOf("textures").forGetter(TemplateLayerDTO::textures)
	).apply(instance, TemplateLayerDTO::new));

	public static final Codec<TemplateModelDTO> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("type").forGetter(TemplateModelDTO::type),
			Codec.list(LAYER_CODEC).optionalFieldOf("layers").forGetter(dto -> Optional.ofNullable(dto.layers)),
			Codec.list(SlotDTO.CODEC).optionalFieldOf("slots").forGetter(dto -> Optional.ofNullable(dto.slots)),
			TemplateTexturesDTO.CODEC.optionalFieldOf("textures").forGetter(dto -> Optional.ofNullable(dto.textures)),
			Codec.STRING.optionalFieldOf("target").forGetter(dto -> Optional.ofNullable(dto.target)),
			Codec.STRING.optionalFieldOf("context").forGetter(dto -> Optional.ofNullable(dto.context))
	).apply(instance, (type, layers, slots, textures, target, context) -> new TemplateModelDTO(type, layers.orElse(null), slots.orElse(null), textures.orElse(null), target.orElse(null), context.orElse(null))));

}
