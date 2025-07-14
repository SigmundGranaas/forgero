package com.sigmundgranaas.forgero.data.loading.impl.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.data.loading.api.data.MaterialData;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.loading.api.data.feature.FeatureData;

import java.util.List;
import java.util.Optional;

import static com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants.JSON_ELEMENT_CODEC;

public class MaterialCodecs {

	public static Codec<MaterialData> create(Codec<List<AttributeData>> attributeCodec, Codec<List<FeatureData>> featureCodec) {
		return RecordCodecBuilder.create(instance ->
				instance.group(
						CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("type").forGetter(MaterialData::type),
						Codec.STRING.fieldOf("name").forGetter(MaterialData::name),
						Codec.list(CodecConstants.OPEN_IDENTIFIER_CODEC).optionalFieldOf("include").forGetter(data -> Optional.ofNullable(data.include())),
						Codec.list(CodecConstants.OPEN_IDENTIFIER_CODEC).optionalFieldOf("tags").forGetter(data -> Optional.ofNullable(data.tags())),
						HostCodecs.HOST_DATA_CODEC.optionalFieldOf("host").forGetter(data -> Optional.ofNullable(data.host())),
						attributeCodec.optionalFieldOf("attributes").forGetter(data -> Optional.ofNullable(data.attributes())),
						featureCodec.optionalFieldOf("features").forGetter(data -> Optional.ofNullable(data.features())),
						Codec.unboundedMap(Codec.STRING, JSON_ELEMENT_CODEC).optionalFieldOf("properties").forGetter(data -> Optional.ofNullable(data.properties()))
				).apply(instance, (type, name, include, tags, host, attributes, features, properties) ->
						new MaterialData(type, name, include.orElse(null), tags.orElse(null), host.orElse(null), attributes.orElse(null), features.orElse(null), properties.orElse(null))));
	}
}
