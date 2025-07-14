package com.sigmundgranaas.forgero.data.loading.impl.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.loading.api.data.feature.FeatureData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.EquipmentTemplateData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.EquipmentTemplateSlotData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.EquipmentTemplateStructureData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.UpgradeSlotData;

import java.util.List;
import java.util.Optional;

public class EquipmentTemplateCodecs {

	public static final Codec<EquipmentTemplateSlotData> EQUIPMENT_TEMPLATE_SLOT_DATA_CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("type").forGetter(EquipmentTemplateSlotData::type),
					CodecConstants.OPEN_IDENTIFIER_CODEC.optionalFieldOf("default_tag").forGetter(data -> Optional.ofNullable(data.defaultTag())),
					CodecConstants.OPEN_IDENTIFIER_CODEC.optionalFieldOf("default").forGetter(data -> Optional.ofNullable(data.defaultComponent()))
			).apply(instance, (type, defaultTag, defaultId) ->
					new EquipmentTemplateSlotData(type, defaultTag.orElse(null), defaultId.orElse(null))));

	public static final Codec<EquipmentTemplateStructureData> EQUIPMENT_TEMPLATE_STRUCTURE_DATA_CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					Codec.STRING.optionalFieldOf("id").forGetter(data -> Optional.ofNullable(data.id())),
					Codec.unboundedMap(Codec.STRING, EQUIPMENT_TEMPLATE_SLOT_DATA_CODEC).fieldOf("slots").forGetter(EquipmentTemplateStructureData::slots)
			).apply(instance, (id, slots) -> new EquipmentTemplateStructureData(id.orElse(null), slots)));


	public static Codec<EquipmentTemplateData> create(Codec<List<AttributeData>> attributeCodec, Codec<List<FeatureData>> featureCodec, Codec<List<UpgradeSlotData>> upgradeSlotCodec) {
		return RecordCodecBuilder.create(instance ->
				instance.group(
						CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("type").forGetter(EquipmentTemplateData::type),
						Codec.STRING.fieldOf("name").forGetter(EquipmentTemplateData::name),
						Codec.list(CodecConstants.OPEN_IDENTIFIER_CODEC).optionalFieldOf("include").forGetter(data -> Optional.ofNullable(data.include())),
						Codec.list(CodecConstants.OPEN_IDENTIFIER_CODEC).optionalFieldOf("tags").forGetter(data -> Optional.ofNullable(data.tags())),
						HostCodecs.HOST_TEMPLATE_DATA_CODEC.optionalFieldOf("host_template").forGetter(data -> Optional.ofNullable(data.host_template())),
						EQUIPMENT_TEMPLATE_STRUCTURE_DATA_CODEC.fieldOf("structure").forGetter(EquipmentTemplateData::structure),
						upgradeSlotCodec.optionalFieldOf("upgrades").forGetter(data -> Optional.ofNullable(data.upgrades())),
						attributeCodec.optionalFieldOf("attributes").forGetter(data -> Optional.ofNullable(data.attributes())),
						featureCodec.optionalFieldOf("features").forGetter(data -> Optional.ofNullable(data.features())),
						Codec.unboundedMap(Codec.STRING, CodecConstants.JSON_ELEMENT_CODEC).optionalFieldOf("properties").forGetter(data -> Optional.ofNullable(data.properties()))
				).apply(instance, (type, name, include, tags, host, structure, upgrades, attributes, features, properties) ->
						new EquipmentTemplateData(type, name, include.orElse(null), tags.orElse(null), host.orElse(null), structure, upgrades.orElse(null), attributes.orElse(null), features.orElse(null), properties.orElse(null))));
	}
}
