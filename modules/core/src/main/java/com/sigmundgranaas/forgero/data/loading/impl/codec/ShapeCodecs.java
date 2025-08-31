package com.sigmundgranaas.forgero.data.loading.impl.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.data.loading.api.data.ShapeData;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeData;

import java.util.List;
import java.util.Optional;

public class ShapeCodecs {

	public static Codec<ShapeData> create(Codec<List<AttributeData>> attributeCodec) {
		return RecordCodecBuilder.create(instance ->
				instance.group(
						CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("type").forGetter(ShapeData::type),
						Codec.STRING.fieldOf("name").forGetter(ShapeData::name),
						Codec.list(CodecConstants.OPEN_IDENTIFIER_CODEC).optionalFieldOf("include").forGetter(data -> Optional.ofNullable(data.include())),
						Codec.list(CodecConstants.OPEN_IDENTIFIER_CODEC).optionalFieldOf("tags").forGetter(data -> Optional.ofNullable(data.tags())),
						HostCodecs.HOST_DATA_CODEC.optionalFieldOf("host").forGetter(data -> Optional.ofNullable(data.host())),
						attributeCodec.optionalFieldOf("attributes").forGetter(data -> Optional.ofNullable(data.attributes())),
						Codec.unboundedMap(Codec.STRING, CodecConstants.JSON_ELEMENT_CODEC).optionalFieldOf("properties").forGetter(data -> Optional.ofNullable(data.properties()))
				).apply(instance, (type, name, include, tags, host, attributes, properties) ->
						new ShapeData(type, name, include.orElse(null), tags.orElse(null), host.orElse(null), attributes.orElse(null), properties.orElse(null))));
	}
}
