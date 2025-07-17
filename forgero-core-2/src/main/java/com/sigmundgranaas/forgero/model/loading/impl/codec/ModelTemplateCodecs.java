package com.sigmundgranaas.forgero.model.loading.impl.codec;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants;
import com.sigmundgranaas.forgero.model.loading.impl.dto.templates.*;

import java.util.List;

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

	private static final Codec<List<TemplateModelDTO>> SINGLE_OR_LIST_OF_MODELS_CODEC = Codec.either(TemplateModelDTO.CODEC, Codec.list(TemplateModelDTO.CODEC))
			.xmap(
					either -> either.map(List::of, list -> list),
					list -> (list.size() == 1) ? Either.left(list.get(0)) : Either.right(list)
			);

	public static final Codec<PartModelTemplateDTO> PART_MODEL_TEMPLATE_CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("type").forGetter(PartModelTemplateDTO::type),
					TARGET_DTO_CODEC.fieldOf("target").forGetter(PartModelTemplateDTO::target),
					Codec.mapEither(SINGLE_OR_LIST_OF_MODELS_CODEC.fieldOf("model"), SINGLE_OR_LIST_OF_MODELS_CODEC.fieldOf("models"))
							.xmap(
									either -> either.map(l -> l, r -> r),
									Either::left
							)
							.forGetter(PartModelTemplateDTO::models)
			).apply(instance, PartModelTemplateDTO::new));


	public static final Codec<ContextualModelTemplateDTO> CONTEXTUAL_MODEL_TEMPLATE_CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("type").forGetter(ContextualModelTemplateDTO::type),
					Codec.STRING.fieldOf("context").forGetter(ContextualModelTemplateDTO::context),
					TARGET_DTO_CODEC.fieldOf("target").forGetter(ContextualModelTemplateDTO::target),
					Codec.mapEither(SINGLE_OR_LIST_OF_MODELS_CODEC.fieldOf("model"), SINGLE_OR_LIST_OF_MODELS_CODEC.fieldOf("models"))
							.xmap(
									either -> either.map(l -> l, r -> r), // Unpack the Either<List, List>
									Either::left
							)
							.forGetter(ContextualModelTemplateDTO::models)
			).apply(instance, ContextualModelTemplateDTO::new));

	public static final Codec<EquipmentModelTemplateDTO> EQUIPMENT_MODEL_TEMPLATE_CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("type").forGetter(EquipmentModelTemplateDTO::type),
					TARGET_DTO_CODEC.fieldOf("target").forGetter(EquipmentModelTemplateDTO::target),
					Codec.mapEither(SINGLE_OR_LIST_OF_MODELS_CODEC.fieldOf("model"), SINGLE_OR_LIST_OF_MODELS_CODEC.fieldOf("models"))
							.xmap(
									either -> either.map(l -> l, r -> r), // Unpack the Either<List, List>
									Either::left
							)
							.forGetter(EquipmentModelTemplateDTO::models)
			).apply(instance, EquipmentModelTemplateDTO::new));
}
