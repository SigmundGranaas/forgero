package com.sigmundgranaas.forgero.data.loading.impl.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.PartTemplateData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.PartTemplateStructureData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.PartTemplateStructureSlotData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.UpgradeSlotData;

import java.util.List;
import java.util.Optional;

public class PartTemplateCodecs {

	public static final Codec<UpgradeSlotData> UPGRADE_SLOT_DATA_CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					CodecConstants.TAG_IDENTIFIER_CODEC.fieldOf("id").forGetter(UpgradeSlotData::id),
					CodecConstants.TAG_IDENTIFIER_CODEC.fieldOf("type").forGetter(UpgradeSlotData::type),
					Codec.list(CodecConstants.TAG_IDENTIFIER_CODEC).optionalFieldOf("tags").forGetter(data -> Optional.ofNullable(data.tags())),
					Codec.INT.optionalFieldOf("tier").forGetter(data -> Optional.ofNullable(data.tier())),
					Codec.STRING.optionalFieldOf("description").forGetter(data -> Optional.ofNullable(data.description())),
					// Optional slot kind (Slot.type()); absent => the standard component-upgrade slot.
					CodecConstants.OPEN_IDENTIFIER_CODEC.optionalFieldOf("kind").forGetter(data -> Optional.ofNullable(data.kind()))
			).apply(instance, (id, type, tags, tier, description, kind) ->
					new UpgradeSlotData(id, type, tags.orElse(null), tier.orElse(null), description.orElse(null), kind.orElse(null))));

	public static final Codec<PartTemplateStructureSlotData> PART_TEMPLATE_STRUCTURE_MATERIAL_DATA_CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					CodecConstants.TAG_IDENTIFIER_CODEC.fieldOf("type").forGetter(PartTemplateStructureSlotData::type),
					CodecConstants.TAG_IDENTIFIER_CODEC.optionalFieldOf("default_tag").forGetter(data -> Optional.ofNullable(data.defaultTag())),
					Codec.INT.optionalFieldOf("count").forGetter(data -> Optional.ofNullable(data.count())),
					Codec.STRING.optionalFieldOf("description").forGetter(data -> Optional.ofNullable(data.description()))
			).apply(instance, (type, defaultTag, count, description) ->
					new PartTemplateStructureSlotData(type, defaultTag.orElse(null), count.orElse(null), description.orElse(null))));

	public static final Codec<PartTemplateStructureData> PART_TEMPLATE_STRUCTURE_DATA_CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					Codec.STRING.optionalFieldOf("id").forGetter(data -> Optional.ofNullable(data.id())),
					Codec.unboundedMap(Codec.STRING, PART_TEMPLATE_STRUCTURE_MATERIAL_DATA_CODEC).fieldOf("slots").forGetter(PartTemplateStructureData::slots)
			).apply(instance, (id, slots) ->
					new PartTemplateStructureData(id.orElse(null), slots)));


	public static Codec<PartTemplateData> create(Codec<List<AttributeData>> attributeCodec, Codec<List<UpgradeSlotData>> upgradeSlotCodec) {
		return RecordCodecBuilder.create(instance ->
				instance.group(
						CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("type").forGetter(PartTemplateData::type),
						Codec.STRING.fieldOf("name").forGetter(PartTemplateData::name),
						Codec.list(CodecConstants.OPEN_IDENTIFIER_CODEC).optionalFieldOf("include").forGetter(data -> Optional.ofNullable(data.include())),
						Codec.list(CodecConstants.TAG_IDENTIFIER_CODEC).optionalFieldOf("tags").forGetter(data -> Optional.ofNullable(data.tags())),
						HostCodecs.HOST_TEMPLATE_DATA_CODEC.optionalFieldOf("host_template").forGetter(data -> Optional.ofNullable(data.host_template())),
						PART_TEMPLATE_STRUCTURE_DATA_CODEC.fieldOf("structure").forGetter(PartTemplateData::structure),
						upgradeSlotCodec.optionalFieldOf("upgrades").forGetter(data -> Optional.ofNullable(data.upgrades())),
						attributeCodec.optionalFieldOf("attributes").forGetter(data -> Optional.ofNullable(data.attributes())),
						GenerationConfigCodecs.GENERATION_CONFIG_DATA_CODEC.optionalFieldOf("generation").forGetter(data -> Optional.ofNullable(data.generation())),
						Codec.unboundedMap(Codec.STRING, CodecConstants.JSON_ELEMENT_CODEC).optionalFieldOf("properties").forGetter(data -> Optional.ofNullable(data.properties()))
				).apply(instance, (type, name, include, tags, host, structure, upgrades, attributes, generation, properties) ->
						new PartTemplateData(type, name, include.orElse(null), tags.orElse(null), host.orElse(null), structure, upgrades.orElse(null), attributes.orElse(null), generation.orElse(null), properties.orElse(null))));
	}
}
