package com.sigmundgranaas.forgero.data.loading.impl.codec;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.data.loading.api.data.template.PartTemplateData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.PartTemplateStructureData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.PartTemplateStructureSlotData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.UpgradeSlotData;

import java.util.Optional;

public class PartTemplateCodecs {

	public static final Codec<UpgradeSlotData> UPGRADE_SLOT_DATA_CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("id").forGetter(UpgradeSlotData::id),
					CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("type").forGetter(UpgradeSlotData::type),
					Codec.list(CodecConstants.OPEN_IDENTIFIER_CODEC).optionalFieldOf("tags").forGetter(data -> Optional.ofNullable(data.tags())),
					Codec.INT.optionalFieldOf("tier").forGetter(data -> Optional.ofNullable(data.tier())),
					Codec.STRING.optionalFieldOf("description").forGetter(data -> Optional.ofNullable(data.description()))
			).apply(instance, (id, type, tags, tier, description) ->
					new UpgradeSlotData(id, type, tags.orElse(null), tier.orElse(null), description.orElse(null))));

	public static final Codec<PartTemplateStructureSlotData> PART_TEMPLATE_STRUCTURE_MATERIAL_DATA_CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("type").forGetter(PartTemplateStructureSlotData::type),
					Codec.INT.optionalFieldOf("count").forGetter(data -> Optional.ofNullable(data.count())),
					Codec.STRING.optionalFieldOf("description").forGetter(data -> Optional.ofNullable(data.description()))
			).apply(instance, (type, count, description) ->
					new PartTemplateStructureSlotData(type, count.orElse(null), description.orElse(null))));

	public static final Codec<PartTemplateStructureData> PART_TEMPLATE_STRUCTURE_DATA_CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					Codec.STRING.optionalFieldOf("id").forGetter(data -> Optional.ofNullable(data.id())),
					Codec.unboundedMap(Codec.STRING, PART_TEMPLATE_STRUCTURE_MATERIAL_DATA_CODEC).fieldOf("slots").forGetter(PartTemplateStructureData::slots)
			).apply(instance, (id, slots) ->
					new PartTemplateStructureData(id.orElse(null), slots)));


	public static final Codec<PartTemplateData> PART_TEMPLATE_DATA_CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("type").forGetter(PartTemplateData::type),
					Codec.STRING.fieldOf("name").forGetter(PartTemplateData::name),
					Codec.list(CodecConstants.OPEN_IDENTIFIER_CODEC).optionalFieldOf("include").forGetter(data -> Optional.ofNullable(data.include())),
					Codec.list(CodecConstants.OPEN_IDENTIFIER_CODEC).optionalFieldOf("tags").forGetter(data -> Optional.ofNullable(data.tags())),
					PART_TEMPLATE_STRUCTURE_DATA_CODEC.fieldOf("structure").forGetter(PartTemplateData::structure),
					Codec.list(UPGRADE_SLOT_DATA_CODEC).optionalFieldOf("upgrades").forGetter(data -> Optional.ofNullable(data.upgrades())),
					Codec.list(AttributeCodecs.ATTRIBUTE_DATA_CODEC).optionalFieldOf("attributes").forGetter(data -> Optional.ofNullable(data.attributes())),
					Codec.list(FeatureCodecs.FEATURE_DATA_CODEC).optionalFieldOf("features").forGetter(data -> Optional.ofNullable(data.features())),
					Codec.unboundedMap(Codec.STRING, CodecConstants.JSON_ELEMENT_CODEC).optionalFieldOf("properties").forGetter(data -> Optional.ofNullable(data.properties()))
			).apply(instance, (type, name, include, tags, structure, upgrades, attributes, features, properties) ->
					new PartTemplateData(type, name, include.orElse(null), tags.orElse(null), structure, upgrades.orElse(null), attributes.orElse(null), features.orElse(null), properties.orElse(null))));
}
