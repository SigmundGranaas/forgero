package com.sigmundgranaas.forgero.model.loading.impl.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants;
import com.sigmundgranaas.forgero.model.loading.impl.dto.ArmorModelDTO;
import com.sigmundgranaas.forgero.model.loading.impl.dto.LayerDTO;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ArmorModelCodecs {

	public static final Codec<ArmorModelDTO> ARMOR_MODEL_DTO_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			CodecConstants.OPEN_IDENTIFIER_CODEC.optionalFieldOf("id").forGetter(ArmorModelDTO::getId),
			Codec.STRING.fieldOf("type").forGetter(ArmorModelDTO::type),
			Codec.STRING.fieldOf("model").forGetter(ArmorModelDTO::model),
			Codec.list(ModelCodecs.LAYER_DTO_CODEC).optionalFieldOf("layers", Collections.emptyList()).forGetter(ArmorModelDTO::layers),
			Codec.list(ModelCodecs.SLOT_DTO_CODEC).optionalFieldOf("slots", Collections.emptyList()).forGetter(ArmorModelDTO::slots),
			Codec.STRING.optionalFieldOf("target").forGetter(ArmorModelDTO::getTarget),
			Codec.STRING.optionalFieldOf("context").forGetter(ArmorModelDTO::getContext)
	).apply(instance, (id, type, model, layers, slots, target, context) -> new ArmorModelDTO(id.orElse(null), type ,model, layers, slots, target.orElse(null), context.orElse(null))));
}
