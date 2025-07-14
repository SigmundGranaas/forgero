package com.sigmundgranaas.forgero.data.loading.impl.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.data.loading.api.data.StaticPartData;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.loading.api.data.feature.FeatureData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.UpgradeSlotData;

import java.util.List;
import java.util.Optional;

public class StaticPartCodecs {

	public static Codec<StaticPartData> create(Codec<List<AttributeData>> attributeCodec, Codec<List<FeatureData>> featureCodec, Codec<List<UpgradeSlotData>> upgradeSlotCodec) {
		return RecordCodecBuilder.create(instance ->
				instance.group(
						CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("type").forGetter(StaticPartData::type),
						Codec.STRING.fieldOf("name").forGetter(StaticPartData::name),
						Codec.list(CodecConstants.OPEN_IDENTIFIER_CODEC).optionalFieldOf("include").forGetter(data -> Optional.ofNullable(data.include())),
						Codec.list(CodecConstants.OPEN_IDENTIFIER_CODEC).optionalFieldOf("tags").forGetter(data -> Optional.ofNullable(data.tags())),
						HostCodecs.HOST_DATA_CODEC.optionalFieldOf("host").forGetter(data -> Optional.ofNullable(data.host())),
						attributeCodec.optionalFieldOf("attributes").forGetter(data -> Optional.ofNullable(data.attributes())),
						upgradeSlotCodec.optionalFieldOf("upgrades").forGetter(data -> Optional.ofNullable(data.upgrades())),
						featureCodec.optionalFieldOf("features").forGetter(data -> Optional.ofNullable(data.features())),
						Codec.unboundedMap(Codec.STRING, CodecConstants.JSON_ELEMENT_CODEC).optionalFieldOf("properties").forGetter(data -> Optional.ofNullable(data.properties()))
				).apply(instance, (type, name, include, tags, host, attributes, upgrades, features, properties) ->
						new StaticPartData(type, name, include.orElse(null), tags.orElse(null), host.orElse(null), attributes.orElse(null), upgrades.orElse(null), features.orElse(null), properties.orElse(null))));
	}
}
