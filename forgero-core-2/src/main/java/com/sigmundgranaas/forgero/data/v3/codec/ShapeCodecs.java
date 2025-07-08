package com.sigmundgranaas.forgero.data.v3.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.data.v3.dto.ShapeData;

import java.util.Optional;

public class ShapeCodecs {

	public static final Codec<ShapeData> SHAPE_DATA_CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("type").forGetter(ShapeData::type),
					Codec.STRING.fieldOf("name").forGetter(ShapeData::name),
					Codec.list(CodecConstants.OPEN_IDENTIFIER_CODEC).optionalFieldOf("include").forGetter(data -> Optional.ofNullable(data.include())),
					Codec.list(CodecConstants.OPEN_IDENTIFIER_CODEC).optionalFieldOf("tags").forGetter(data -> Optional.ofNullable(data.tags())),
					Codec.list(AttributeCodecs.ATTRIBUTE_DATA_CODEC).optionalFieldOf("attributes").forGetter(data -> Optional.ofNullable(data.attributes())),
					Codec.list(FeatureCodecs.FEATURE_DATA_CODEC).optionalFieldOf("features").forGetter(data -> Optional.ofNullable(data.features()))
			).apply(instance, (type, name, include, tags, attributes, features) ->
					new ShapeData(type, name, include.orElse(null), tags.orElse(null), attributes.orElse(null), features.orElse(null))));
}
