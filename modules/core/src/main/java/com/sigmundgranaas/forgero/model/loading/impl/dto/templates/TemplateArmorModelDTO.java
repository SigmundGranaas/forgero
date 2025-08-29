package com.sigmundgranaas.forgero.model.loading.impl.dto.templates;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.model.loading.impl.codec.JsonElementCodec;
import com.sigmundgranaas.forgero.model.loading.impl.dto.SlotDTO;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * DTO for the "model" block inside an armor template file.
 * This is the template counterpart to the final ArmorModelDTO.
 */
public record TemplateArmorModelDTO(
		@Nullable String id,
		String model,
		@Nullable List<TemplateModelDTO.TemplateLayerDTO> layers,
		@Nullable List<SlotDTO> slots,
		@Nullable String target,
		@Nullable String context,
		@Nullable JsonElement display
) {
	public static final Codec<TemplateArmorModelDTO> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.optionalFieldOf("id").forGetter(dto -> Optional.ofNullable(dto.id)),
			Codec.STRING.fieldOf("model").forGetter(TemplateArmorModelDTO::model),
			Codec.list(TemplateModelDTO.TemplateLayerDTO.CODEC).optionalFieldOf("layers").forGetter(dto -> Optional.ofNullable(dto.layers)),
			Codec.list(SlotDTO.CODEC).optionalFieldOf("slots").forGetter(dto -> Optional.ofNullable(dto.slots)),
			Codec.STRING.optionalFieldOf("target").forGetter(dto -> Optional.ofNullable(dto.target)),
			Codec.STRING.optionalFieldOf("context").forGetter(dto -> Optional.ofNullable(dto.context)),
			JsonElementCodec.INSTANCE.optionalFieldOf("display").forGetter(dto -> Optional.ofNullable(dto.display))
	).apply(instance, (id, model, layers, slots, target, context, display) -> new TemplateArmorModelDTO(id.orElse(null), model, layers.orElse(null), slots.orElse(null), target.orElse(null), context.orElse(null), display.orElse(null))));
}
