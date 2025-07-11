package com.sigmundgranaas.forgero.model.loading.impl.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants;
import com.sigmundgranaas.forgero.model.loading.impl.dto.templates.*;

public class ModelTemplateCodecs {

	public static final Codec<TargetDTO> TARGET_DTO_CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("tag").forGetter(TargetDTO::tag)
			).apply(instance, TargetDTO::new));

	public static final Codec<GenerationDTO> GENERATION_DTO_CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("type").forGetter(GenerationDTO::type),
					Codec.STRING.fieldOf("template").forGetter(GenerationDTO::template),
					Codec.STRING.fieldOf("palette").forGetter(GenerationDTO::palette),
					Codec.STRING.fieldOf("output").forGetter(GenerationDTO::output)
			).apply(instance, GenerationDTO::new));


	public static final Codec<PartModelTemplateDTO> PART_MODEL_TEMPLATE_CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("type").forGetter(PartModelTemplateDTO::type),
					TARGET_DTO_CODEC.fieldOf("target").forGetter(PartModelTemplateDTO::target),
					TemplateModelDTO.CODEC.fieldOf("model").forGetter(PartModelTemplateDTO::model)
			).apply(instance, PartModelTemplateDTO::new));


	public static final Codec<ContextualModelTemplateDTO> CONTEXTUAL_MODEL_TEMPLATE_CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("type").forGetter(ContextualModelTemplateDTO::type),
					Codec.STRING.fieldOf("context").forGetter(ContextualModelTemplateDTO::context),
					TARGET_DTO_CODEC.fieldOf("target").forGetter(ContextualModelTemplateDTO::target),
					TemplateModelDTO.CODEC.fieldOf("model").forGetter(ContextualModelTemplateDTO::model)
			).apply(instance, ContextualModelTemplateDTO::new));

	public static final Codec<EquipmentModelTemplateDTO> EQUIPMENT_MODEL_TEMPLATE_CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("type").forGetter(EquipmentModelTemplateDTO::type),
					TARGET_DTO_CODEC.fieldOf("target").forGetter(EquipmentModelTemplateDTO::target),
					TemplateModelDTO.CODEC.fieldOf("model").forGetter(EquipmentModelTemplateDTO::model)
			).apply(instance, EquipmentModelTemplateDTO::new));
}
