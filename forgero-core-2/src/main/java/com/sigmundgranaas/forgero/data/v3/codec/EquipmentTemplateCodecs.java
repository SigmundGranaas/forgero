package com.sigmundgranaas.forgero.data.v3.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.data.v3.dto.template.EquipmentTemplateData;
import com.sigmundgranaas.forgero.data.v3.dto.template.EquipmentTemplateStructureData;
import com.sigmundgranaas.forgero.data.v3.dto.template.EquipmentTemplateSlotData;

import java.util.Optional;

public class EquipmentTemplateCodecs {

	public static final Codec<EquipmentTemplateSlotData> EQUIPMENT_TEMPLATE_SLOT_DATA_CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("type").forGetter(EquipmentTemplateSlotData::type),
					CodecConstants.OPEN_IDENTIFIER_CODEC.optionalFieldOf("default").forGetter(data -> Optional.ofNullable(data.defaultComponent()))
			).apply(instance, (type, defaultComponent) ->
					new EquipmentTemplateSlotData(type, defaultComponent.orElse(null))));

	public static final Codec<EquipmentTemplateStructureData> EQUIPMENT_TEMPLATE_STRUCTURE_DATA_CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					Codec.STRING.optionalFieldOf("id").forGetter(data -> Optional.ofNullable(data.id())), // ADDED
					Codec.unboundedMap(Codec.STRING, EQUIPMENT_TEMPLATE_SLOT_DATA_CODEC).fieldOf("slots").forGetter(EquipmentTemplateStructureData::slots) // Wrapped slots in a field
			).apply(instance, (id, slots) -> new EquipmentTemplateStructureData(id.orElse(null), slots))); // Modified apply


	public static final Codec<EquipmentTemplateData> TOOL_TEMPLATE_DATA_CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("type").forGetter(EquipmentTemplateData::type),
					Codec.STRING.fieldOf("name").forGetter(EquipmentTemplateData::name),
					Codec.list(CodecConstants.OPEN_IDENTIFIER_CODEC).optionalFieldOf("include").forGetter(data -> Optional.ofNullable(data.include())),
					Codec.list(CodecConstants.OPEN_IDENTIFIER_CODEC).optionalFieldOf("tags").forGetter(data -> Optional.ofNullable(data.tags())),
					EQUIPMENT_TEMPLATE_STRUCTURE_DATA_CODEC.fieldOf("structure").forGetter(EquipmentTemplateData::structure), // Type is now EquipmentTemplateStructureData
					Codec.list(PartTemplateCodecs.UPGRADE_SLOT_DATA_CODEC).optionalFieldOf("upgrades").forGetter(data -> Optional.ofNullable(data.upgrades())),
					Codec.list(AttributeCodecs.ATTRIBUTE_DATA_CODEC).optionalFieldOf("attributes").forGetter(data -> Optional.ofNullable(data.attributes())),
					Codec.list(FeatureCodecs.FEATURE_DATA_CODEC).optionalFieldOf("features").forGetter(data -> Optional.ofNullable(data.features()))
			).apply(instance, (type, name, include, tags, structure, upgrades, attributes, features) ->
					new EquipmentTemplateData(type, name, include.orElse(null), tags.orElse(null), structure, upgrades.orElse(null), attributes.orElse(null), features.orElse(null))));
}
