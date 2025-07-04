package com.sigmundgranaas.forgero.data.v3.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.data.v3.dto.ToolTemplateData;
import com.sigmundgranaas.forgero.data.v3.dto.ToolTemplateStructureData;
import com.sigmundgranaas.forgero.data.v3.dto.ToolTemplateSlotData;

import java.util.Optional;

public class ToolTemplateCodecs {

	public static final Codec<ToolTemplateSlotData> TOOL_TEMPLATE_SLOT_DATA_CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("type").forGetter(ToolTemplateSlotData::type), // Changed to OpenIdentifierCodec
					CodecConstants.OPEN_IDENTIFIER_CODEC.optionalFieldOf("default").forGetter(data -> Optional.ofNullable(data.defaultComponent())) // Changed to OpenIdentifierCodec
			).apply(instance, (type, defaultComponent) ->
					new ToolTemplateSlotData(type, defaultComponent.orElse(null))));

	public static final Codec<ToolTemplateStructureData> TOOL_TEMPLATE_STRUCTURE_DATA_CODEC = Codec.unboundedMap(Codec.STRING, TOOL_TEMPLATE_SLOT_DATA_CODEC)
			.xmap(ToolTemplateStructureData::new, ToolTemplateStructureData::slots);


	public static final Codec<ToolTemplateData> TOOL_TEMPLATE_DATA_CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("type").forGetter(ToolTemplateData::type), // Changed to OpenIdentifierCodec
					Codec.STRING.fieldOf("name").forGetter(ToolTemplateData::name),
					Codec.list(CodecConstants.OPEN_IDENTIFIER_CODEC).optionalFieldOf("include").forGetter(data -> Optional.ofNullable(data.include())), // Changed to list of OpenIdentifierCodec
					Codec.list(CodecConstants.OPEN_IDENTIFIER_CODEC).optionalFieldOf("tags").forGetter(data -> Optional.ofNullable(data.tags())), // Changed to list of OpenIdentifierCodec
					TOOL_TEMPLATE_STRUCTURE_DATA_CODEC.fieldOf("structure").forGetter(ToolTemplateData::structure),
					Codec.list(PartTemplateCodecs.UPGRADE_SLOT_DATA_CODEC).optionalFieldOf("upgrades").forGetter(data -> Optional.ofNullable(data.upgrades())), // Reuse UpgradeSlotData codec
					Codec.list(AttributeCodecs.ATTRIBUTE_DATA_CODEC).optionalFieldOf("attributes").forGetter(data -> Optional.ofNullable(data.attributes())),
					Codec.list(FeatureCodecs.FEATURE_DATA_CODEC).optionalFieldOf("features").forGetter(data -> Optional.ofNullable(data.features()))
			).apply(instance, (type, name, include, tags, structure, upgrades, attributes, features) ->
					new ToolTemplateData(type, name, include.orElse(null), tags.orElse(null), structure, upgrades.orElse(null), attributes.orElse(null), features.orElse(null))));
}
