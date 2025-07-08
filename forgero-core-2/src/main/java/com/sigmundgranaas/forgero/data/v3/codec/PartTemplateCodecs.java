package com.sigmundgranaas.forgero.data.v3.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.data.v3.dto.template.PartTemplateData;
import com.sigmundgranaas.forgero.data.v3.dto.template.PartTemplateNamingData;
import com.sigmundgranaas.forgero.data.v3.dto.template.PartTemplateStructureData;
import com.sigmundgranaas.forgero.data.v3.dto.template.PartTemplateStructureSlotData;
import com.sigmundgranaas.forgero.data.v3.dto.template.UpgradeSlotData;
// Removed unused import: import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier;

import java.util.Optional;

public class PartTemplateCodecs {

	public static final Codec<UpgradeSlotData> UPGRADE_SLOT_DATA_CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("id").forGetter(UpgradeSlotData::id), // Changed to OpenIdentifierCodec
					CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("type").forGetter(UpgradeSlotData::type), // Changed to OpenIdentifierCodec
					Codec.list(CodecConstants.OPEN_IDENTIFIER_CODEC).optionalFieldOf("tags").forGetter(data -> Optional.ofNullable(data.tags())), // Changed to list of OpenIdentifierCodec
					Codec.INT.optionalFieldOf("tier").forGetter(data -> Optional.ofNullable(data.tier())),
					Codec.STRING.optionalFieldOf("description").forGetter(data -> Optional.ofNullable(data.description()))
			).apply(instance, (id, type, tags, tier, description) ->
					new UpgradeSlotData(id, type, tags.orElse(null), tier.orElse(null), description.orElse(null))));

	public static final Codec<PartTemplateStructureSlotData> PART_TEMPLATE_STRUCTURE_MATERIAL_DATA_CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("type").forGetter(PartTemplateStructureSlotData::type), // Changed to OpenIdentifierCodec
					Codec.INT.optionalFieldOf("count").forGetter(data -> Optional.of(data.count())),
					Codec.STRING.optionalFieldOf("description").forGetter(data -> Optional.ofNullable(data.description()))
			).apply(instance, (type, count, description) ->
					new PartTemplateStructureSlotData(type, count.orElse(0), description.orElse(null))));

	public static final Codec<PartTemplateStructureData> PART_TEMPLATE_STRUCTURE_DATA_CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					PART_TEMPLATE_STRUCTURE_MATERIAL_DATA_CODEC.fieldOf("material").forGetter(PartTemplateStructureData::material),
					PART_TEMPLATE_STRUCTURE_MATERIAL_DATA_CODEC.fieldOf("shape").forGetter(PartTemplateStructureData::shape)
			).apply(instance, PartTemplateStructureData::new));

	public static final Codec<PartTemplateNamingData> PART_TEMPLATE_NAMING_DATA_CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					Codec.STRING.fieldOf("pattern").forGetter(PartTemplateNamingData::pattern)
			).apply(instance, PartTemplateNamingData::new));

	public static final Codec<PartTemplateData> PART_TEMPLATE_DATA_CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("type").forGetter(PartTemplateData::type), // Changed to OpenIdentifierCodec
					Codec.STRING.fieldOf("name").forGetter(PartTemplateData::name),
					Codec.list(CodecConstants.OPEN_IDENTIFIER_CODEC).optionalFieldOf("include").forGetter(data -> Optional.ofNullable(data.include())), // Changed to list of OpenIdentifierCodec
					Codec.list(CodecConstants.OPEN_IDENTIFIER_CODEC).optionalFieldOf("tags").forGetter(data -> Optional.ofNullable(data.tags())), // Changed to list of OpenIdentifierCodec
					PART_TEMPLATE_STRUCTURE_DATA_CODEC.fieldOf("structure").forGetter(PartTemplateData::structure),
					Codec.list(UPGRADE_SLOT_DATA_CODEC).optionalFieldOf("upgrades").forGetter(data -> Optional.ofNullable(data.upgrades())),
					PART_TEMPLATE_NAMING_DATA_CODEC.optionalFieldOf("naming").forGetter(data -> Optional.ofNullable(data.naming())),
					Codec.list(AttributeCodecs.ATTRIBUTE_DATA_CODEC).optionalFieldOf("attributes").forGetter(data -> Optional.ofNullable(data.attributes())),
					Codec.list(FeatureCodecs.FEATURE_DATA_CODEC).optionalFieldOf("features").forGetter(data -> Optional.ofNullable(data.features()))
			).apply(instance, (type, name, include, tags, structure, upgrades, naming, attributes, features) ->
					new PartTemplateData(type, name, include.orElse(null), tags.orElse(null), structure, upgrades.orElse(null), naming.orElse(null), attributes.orElse(null), features.orElse(null))));
}
