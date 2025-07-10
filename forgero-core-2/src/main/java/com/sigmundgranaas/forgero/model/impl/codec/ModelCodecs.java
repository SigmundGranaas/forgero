package com.sigmundgranaas.forgero.model.impl.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.model.impl.dto.*;

import java.util.List;
import java.util.Optional;

public class ModelCodecs {

	// Codecs for sub-parts of the models
	public static final Codec<SelfDTO> SELF_DTO_CODEC = RecordCodecBuilder.create(instance -> instance.group(Codec.STRING.fieldOf("texture").forGetter(SelfDTO::texture), Codec.INT.fieldOf("order").forGetter(SelfDTO::order)).apply(instance, SelfDTO::new));
	public static final Codec<PartDTO> PART_DTO_CODEC = RecordCodecBuilder.create(instance -> instance.group(Codec.STRING.fieldOf("slot").forGetter(PartDTO::slot), Codec.INT.fieldOf("order").forGetter(PartDTO::order)).apply(instance, PartDTO::new));
	public static final Codec<ModelSelectorDTO> MODEL_SELECTOR_DTO_CODEC = RecordCodecBuilder.create(instance -> instance.group(Codec.STRING.fieldOf("target_slot").forGetter(ModelSelectorDTO::target_slot), Codec.STRING.optionalFieldOf("target_tag").forGetter(dto -> Optional.ofNullable(dto.target_tag())), Codec.STRING.fieldOf("model").forGetter(ModelSelectorDTO::model)).apply(instance, (slot, tag, model) -> new ModelSelectorDTO(slot, tag.orElse(null), model)));

	// Codec for the "forgero:static_model" type
	public static final Codec<StaticModelDTO> STATIC_MODEL_DTO_CODEC = RecordCodecBuilder.create(instance -> instance.group(Codec.STRING.fieldOf("model_type").forGetter(StaticModelDTO::model_type), Codec.STRING.fieldOf("texture").forGetter(StaticModelDTO::texture), JsonElementCodec.INSTANCE.optionalFieldOf("display").forGetter(dto -> Optional.ofNullable(dto.display()))).apply(instance, (type, texture, display) -> new StaticModelDTO(type, texture, display.orElse(null))));

	// Codec for the "forgero:composite_model" type
	public static final Codec<CompositeModelDTO> COMPOSITE_MODEL_DTO_CODEC = RecordCodecBuilder.create(instance -> instance.group(Codec.STRING.fieldOf("model_type").forGetter(CompositeModelDTO::model_type), Codec.list(SELF_DTO_CODEC).optionalFieldOf("self", List.of()).forGetter(dto -> Optional.ofNullable(dto.self()).orElse(List.of())), Codec.list(PART_DTO_CODEC).optionalFieldOf("parts", List.of()).forGetter(dto -> Optional.ofNullable(dto.parts()).orElse(List.of())), Codec.list(PART_DTO_CODEC).optionalFieldOf("upgrades", List.of()).forGetter(dto -> Optional.ofNullable(dto.upgrades()).orElse(List.of())), Codec.list(MODEL_SELECTOR_DTO_CODEC).optionalFieldOf("model_selectors", List.of()).forGetter(dto -> Optional.ofNullable(dto.model_selectors()).orElse(List.of())), JsonElementCodec.INSTANCE.optionalFieldOf("display").forGetter(dto -> Optional.ofNullable(dto.display())), Codec.list(JsonElementCodec.INSTANCE).optionalFieldOf("overrides", List.of()).forGetter(dto -> Optional.ofNullable(dto.overrides()).orElse(List.of()))).apply(instance, (type, self, parts, upgrades, selectors, display, overrides) -> new CompositeModelDTO(type, self, parts, upgrades, selectors, display.orElse(null), overrides)));

	/**
	 * A custom Codec class to handle dispatching between different ModelDTO types.
	 * This declarative, class-based approach resolves compiler issues with generic type inference.
	 */
	private static final class ModelDtoCodec implements Codec<ModelDTO> {
		@Override
		public <T> DataResult<Pair<ModelDTO, T>> decode(final DynamicOps<T> ops, final T input) {
			// Peek at the "model_type" field to decide which decoder to use
			return ops.get(input, "model_type").flatMap(ops::getStringValue).flatMap(type -> switch (type) {
				case "forgero:static_model" ->
					// Decode with the specific codec, then widen the pair's type from StaticModelDTO to ModelDTO
						STATIC_MODEL_DTO_CODEC.decode(ops, input).map(pair -> pair.mapFirst(dto -> dto));
				case "forgero:composite_model" ->
						COMPOSITE_MODEL_DTO_CODEC.decode(ops, input).map(pair -> pair.mapFirst(dto -> dto));
				default -> DataResult.error(() -> "Unknown model_type: " + type);
			});
		}

		@Override
		public <T> DataResult<T> encode(final ModelDTO input, final DynamicOps<T> ops, final T prefix) {
			// Check the runtime type to decide which encoder to use
			if (input instanceof StaticModelDTO staticDto) {
				return STATIC_MODEL_DTO_CODEC.encode(staticDto, ops, prefix);
			} else if (input instanceof CompositeModelDTO compositeDto) {
				return COMPOSITE_MODEL_DTO_CODEC.encode(compositeDto, ops, prefix);
			}
			return DataResult.error(() -> "Unknown ModelDTO type for encoding: " + input.getClass().getName());
		}
	}

	public static final Codec<ModelDTO> MODEL_DTO_CODEC = new ModelDtoCodec();
}
