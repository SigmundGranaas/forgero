package com.sigmundgranaas.forgero.data.loading.impl.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.data.loading.api.data.SchematicData;

import java.util.Optional;

public class SchematicCodecs {

	public static Codec<SchematicData> create() {
		return RecordCodecBuilder.create(instance ->
				instance.group(
						CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("type").forGetter(SchematicData::type),
						Codec.STRING.fieldOf("name").forGetter(SchematicData::name),
						Codec.list(CodecConstants.OPEN_IDENTIFIER_CODEC).optionalFieldOf("include").forGetter(data -> Optional.ofNullable(data.include())),
						Codec.list(CodecConstants.OPEN_IDENTIFIER_CODEC).optionalFieldOf("tags").forGetter(data -> Optional.ofNullable(data.tags())),
						HostCodecs.HOST_DATA_CODEC.optionalFieldOf("host").forGetter(data -> Optional.ofNullable(data.host())),
						CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("target").forGetter(SchematicData::target),
						Codec.STRING.fieldOf("crafting_material").forGetter(SchematicData::craftingMaterial),
						Codec.unboundedMap(Codec.STRING, CodecConstants.JSON_ELEMENT_CODEC).optionalFieldOf("properties").forGetter(data -> Optional.ofNullable(data.properties()))
				).apply(instance, (type, name, include, tags, host, target, craftingMaterial, properties) ->
						new SchematicData(type, name, include.orElse(null), tags.orElse(null), host.orElse(null), target, craftingMaterial, properties.orElse(null))));
	}
}
