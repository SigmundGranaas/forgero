package com.sigmundgranaas.forgero.data.v3.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.data.v3.dto.SchematicData;

import java.util.Optional;

public class SchematicCodecs {

	public static final Codec<SchematicData> SCHEMATIC_DATA_CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("type").forGetter(SchematicData::type),
					Codec.STRING.fieldOf("name").forGetter(SchematicData::name),
					Codec.list(CodecConstants.OPEN_IDENTIFIER_CODEC).optionalFieldOf("include").forGetter(data -> Optional.ofNullable(data.include())),
					Codec.list(CodecConstants.OPEN_IDENTIFIER_CODEC).optionalFieldOf("tags").forGetter(data -> Optional.ofNullable(data.tags())),
					CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("target").forGetter(SchematicData::target),
					Codec.STRING.fieldOf("crafting_material").forGetter(SchematicData::craftingMaterial)
			).apply(instance, (type, name, include, tags, target, craftingMaterial) ->
					new SchematicData(type, name, include.orElse(null), tags.orElse(null), target, craftingMaterial)));
}
