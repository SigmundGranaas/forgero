package com.sigmundgranaas.forgero.model.loading.impl.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants;
import com.sigmundgranaas.forgero.model.loading.impl.dto.*;

import java.util.Arrays;
import java.util.Optional;

public class ModelCodecs {
	public static final Codec<LayerDTO> LAYER_DTO_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.fieldOf("order").forGetter(LayerDTO::order),
			TexturesDTO.CODEC.fieldOf("textures").forGetter(LayerDTO::textures),
			Codec.list(Codec.INT).xmap(
					list -> list.stream().mapToInt(Integer::intValue).toArray(),
					array -> Arrays.stream(array).boxed().toList()
			).optionalFieldOf("offset").forGetter(dto -> Optional.ofNullable(dto.offset()))
	).apply(instance, (order, textures, offset) -> new LayerDTO(order, textures, offset.orElse(null))));

	public static final Codec<TexturesDTO> TEXTURES_DTO_CODEC = TexturesDTO.CODEC;

	public static final Codec<SlotDTO> SLOT_DTO_CODEC = SlotDTO.CODEC;


	public static final Codec<ModelDTO> MODEL_DTO_CODEC_DISPATCHER = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.optionalFieldOf("id").forGetter(ModelDTO::getId),
			Codec.STRING.fieldOf("type").forGetter(ModelDTO::type),
			Codec.list(LAYER_DTO_CODEC).optionalFieldOf("layers").forGetter(ModelDTO::getLayers),
			Codec.list(SLOT_DTO_CODEC).optionalFieldOf("slots").forGetter(ModelDTO::getSlots),
			Codec.STRING.optionalFieldOf("texture").forGetter(ModelDTO::getTexture),
			TEXTURES_DTO_CODEC.optionalFieldOf("textures").forGetter(ModelDTO::getTextures),
			Codec.STRING.optionalFieldOf("target").forGetter(ModelDTO::getTarget),
			Codec.STRING.optionalFieldOf("context").forGetter(ModelDTO::getContext),
			Codec.STRING.optionalFieldOf("parent").forGetter(ModelDTO::getParent),
			JsonElementCodec.INSTANCE.optionalFieldOf("display").forGetter(ModelDTO::getDisplay)
	).apply(instance, (id, type, layers, slots, texture, textures, target, context, parent, display) -> new ModelDTO(id.orElse(null), type, layers.orElse(null), slots.orElse(null), texture.orElse(null), textures.orElse(null), target.orElse(null), context.orElse(null), parent.orElse(null), display.orElse(null))));
}
