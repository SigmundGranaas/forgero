package com.sigmundgranaas.forgero.data.loading.impl.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.data.loading.api.data.SchematicData;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.UpgradeSlotData;

import java.util.List;
import java.util.Optional;

public class SchematicCodecs {

	public static Codec<SchematicData> create(Codec<List<AttributeData>> attributeCodec) {
		return RecordCodecBuilder.create(instance ->
				instance.group(
						CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("type").forGetter(SchematicData::type),
						Codec.STRING.fieldOf("name").forGetter(SchematicData::name),
						Codec.list(CodecConstants.OPEN_IDENTIFIER_CODEC).optionalFieldOf("include").forGetter(data -> Optional.ofNullable(data.include())),
						Codec.list(CodecConstants.TAG_IDENTIFIER_CODEC).optionalFieldOf("tags").forGetter(data -> Optional.ofNullable(data.tags())),
						Codec.list(CodecConstants.TAG_IDENTIFIER_CODEC).optionalFieldOf("local_tags").forGetter(data -> Optional.ofNullable(data.localTags())),
						HostCodecs.HOST_DATA_CODEC.optionalFieldOf("host").forGetter(data -> Optional.ofNullable(data.host())),
						attributeCodec.optionalFieldOf("attributes").forGetter(data -> Optional.ofNullable(data.attributes())),
						attributeCodec.optionalFieldOf("local_attributes").forGetter(data -> Optional.ofNullable(data.localAttributes())),
						Codec.list(PartTemplateCodecs.UPGRADE_SLOT_DATA_CODEC).optionalFieldOf("upgrades").forGetter(data -> Optional.ofNullable(data.upgrades())),
						CodecConstants.OPEN_IDENTIFIER_CODEC.optionalFieldOf("target").forGetter(data -> Optional.ofNullable(data.target())),
						Codec.unboundedMap(Codec.STRING, CodecConstants.JSON_ELEMENT_CODEC).optionalFieldOf("properties").forGetter(data -> Optional.ofNullable(data.properties()))
				).apply(instance, (type, name, include, tags, localTags, host, attributes, localAttributes, upgrades, target, properties) ->
						new SchematicData(type, name, include.orElse(null), tags.orElse(null), localTags.orElse(null), host.orElse(null), attributes.orElse(null), localAttributes.orElse(null), upgrades.orElse(null), target.orElse(null), properties.orElse(null))));
	}
}
