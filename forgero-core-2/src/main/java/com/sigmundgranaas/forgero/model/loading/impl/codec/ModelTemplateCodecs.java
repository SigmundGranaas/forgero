package com.sigmundgranaas.forgero.model.loading.impl.codec;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants;
import com.sigmundgranaas.forgero.model.loading.impl.dto.templates.*;

import java.util.List;
import java.util.stream.Stream;

public class ModelTemplateCodecs {

	public static final Codec<TargetDTO> TARGET_DTO_CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("tag").forGetter(TargetDTO::tag)
			).apply(instance, TargetDTO::new));

	/**
	 * Creates a codec that can handle either a single object or a list of those objects.
	 */
	private static <A> Codec<List<A>> singleOrListOf(Codec<A> codec) {
		return Codec.either(codec, Codec.list(codec))
				.xmap(
						either -> either.map(List::of, list -> list),
						list -> list.size() == 1 ? Either.left(list.get(0)) : Either.right(list)
				);
	}

	/**
	 * A custom MapCodec that attempts to decode from a "models" field, falling back to a "model" field.
	 * It always encodes to the "models" field for consistency.
	 *
	 * @param <A> The type of model DTO in the list (e.g., TemplateModelDTO).
	 * @param elementCodec The codec for the single model DTO element.
	 * @return A MapCodec handling the legacy "model" and current "models" fields.
	 */
	private static <A> MapCodec<List<A>> modelsOrModelMapCodec(Codec<A> elementCodec) {
		return new MapCodec<>() {
			private final String PREFERRED_FIELD = "models";
			private final String LEGACY_FIELD = "model";
			private final Codec<List<A>> listCodec = singleOrListOf(elementCodec);

			@Override
			public <T> DataResult<List<A>> decode(DynamicOps<T> ops, MapLike<T> input) {
				if(input.get(LEGACY_FIELD) != null) {
					return listCodec.parse(ops, input.get(LEGACY_FIELD));
				}

				return listCodec.parse(ops, input.get(PREFERRED_FIELD));
			}

			@Override
			public <T> RecordBuilder<T> encode(List<A> input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
				return prefix.add(PREFERRED_FIELD, listCodec.encodeStart(ops, input));
			}

			@Override
			public <T> Stream<T> keys(DynamicOps<T> ops) {
				return Stream.of(ops.createString(PREFERRED_FIELD), ops.createString(LEGACY_FIELD));
			}
		};
	}

	private static final MapCodec<List<TemplateModelDTO>> ITEM_MODELS_MAP_CODEC = modelsOrModelMapCodec(TemplateModelDTO.CODEC);
	private static final MapCodec<List<TemplateArmorModelDTO>> ARMOR_MODELS_MAP_CODEC = modelsOrModelMapCodec(TemplateArmorModelDTO.CODEC);

	public static final Codec<PartModelTemplateDTO> PART_MODEL_TEMPLATE_CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("type").forGetter(PartModelTemplateDTO::type),
					TARGET_DTO_CODEC.fieldOf("target").forGetter(PartModelTemplateDTO::target),
					ITEM_MODELS_MAP_CODEC.forGetter(PartModelTemplateDTO::models)
			).apply(instance, PartModelTemplateDTO::new));


	public static final Codec<ContextualModelTemplateDTO> CONTEXTUAL_MODEL_TEMPLATE_CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("type").forGetter(ContextualModelTemplateDTO::type),
					Codec.STRING.fieldOf("context").forGetter(ContextualModelTemplateDTO::context),
					TARGET_DTO_CODEC.fieldOf("target").forGetter(ContextualModelTemplateDTO::target),
					ITEM_MODELS_MAP_CODEC.forGetter(ContextualModelTemplateDTO::models)
			).apply(instance, ContextualModelTemplateDTO::new));

	public static final Codec<EquipmentModelTemplateDTO> EQUIPMENT_MODEL_TEMPLATE_CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("type").forGetter(EquipmentModelTemplateDTO::type),
					TARGET_DTO_CODEC.fieldOf("target").forGetter(EquipmentModelTemplateDTO::target),
					ITEM_MODELS_MAP_CODEC.forGetter(EquipmentModelTemplateDTO::models)
			).apply(instance, EquipmentModelTemplateDTO::new));

	public static final Codec<ArmorModelTemplateDTO> ARMOR_MODEL_TEMPLATE_CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("type").forGetter(ArmorModelTemplateDTO::type),
					TARGET_DTO_CODEC.fieldOf("target").forGetter(ArmorModelTemplateDTO::target),
					ARMOR_MODELS_MAP_CODEC.forGetter(ArmorModelTemplateDTO::models)
			).apply(instance, ArmorModelTemplateDTO::new));
}
