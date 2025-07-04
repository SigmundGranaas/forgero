package com.sigmundgranaas.forgero.data.v3.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.data.v3.dto.MaterialData;

import java.util.Optional;

public class MaterialCodecs {

	public static final Codec<MaterialData> MATERIAL_DATA_CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("type").forGetter(MaterialData::type), // Changed to OpenIdentifierCodec
					Codec.STRING.fieldOf("name").forGetter(MaterialData::name),
					Codec.list(CodecConstants.OPEN_IDENTIFIER_CODEC).optionalFieldOf("include").forGetter(data -> Optional.ofNullable(data.include())), // Changed to list of OpenIdentifierCodec
					Codec.list(CodecConstants.OPEN_IDENTIFIER_CODEC).optionalFieldOf("tags").forGetter(data -> Optional.ofNullable(data.tags())), // Changed to list of OpenIdentifierCodec
					Codec.list(AttributeCodecs.ATTRIBUTE_DATA_CODEC).optionalFieldOf("attributes").forGetter(data -> Optional.ofNullable(data.attributes())),
					Codec.list(FeatureCodecs.FEATURE_DATA_CODEC).optionalFieldOf("features").forGetter(data -> Optional.ofNullable(data.features()))
			).apply(instance, (type, name, include, tags, attributes, features) ->
					new MaterialData(type, name, include.orElse(null), tags.orElse(null), attributes.orElse(null), features.orElse(null))));
}
