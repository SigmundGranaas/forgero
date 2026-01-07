package com.sigmundgranaas.forgero.data.loading.impl.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.data.loading.api.data.GenerationConfigData;
import com.sigmundgranaas.forgero.data.loading.api.data.SlotGenerationFilter;

import java.util.Map;
import java.util.Optional;

public class GenerationConfigCodecs {

	public static final Codec<SlotGenerationFilter> SLOT_GENERATION_FILTER_CODEC =
			RecordCodecBuilder.create(instance ->
					instance.group(
							Codec.list(CodecConstants.TAG_IDENTIFIER_CODEC).optionalFieldOf("require_all_tags").forGetter(data -> Optional.ofNullable(data.requireAllTags())),
							Codec.list(CodecConstants.TAG_IDENTIFIER_CODEC).optionalFieldOf("require_any_tags").forGetter(data -> Optional.ofNullable(data.requireAnyTags())),
							Codec.list(CodecConstants.TAG_IDENTIFIER_CODEC).optionalFieldOf("exclude_any_tags").forGetter(data -> Optional.ofNullable(data.excludeAnyTags())),
							Codec.list(CodecConstants.TAG_IDENTIFIER_CODEC).optionalFieldOf("exclude_all_tags").forGetter(data -> Optional.ofNullable(data.excludeAllTags())),
							Codec.list(CodecConstants.OPEN_IDENTIFIER_CODEC).optionalFieldOf("explicit_list").forGetter(data -> Optional.ofNullable(data.explicitList()))
					).apply(instance, (requireAll, requireAny, excludeAny, excludeAll, explicit) ->
							new SlotGenerationFilter(
									requireAll.orElse(null),
									requireAny.orElse(null),
									excludeAny.orElse(null),
									excludeAll.orElse(null),
									explicit.orElse(null)
							)));

	/**
	 * Codec for GenerationConfigData.
	 */
	public static final Codec<GenerationConfigData> GENERATION_CONFIG_DATA_CODEC =
			RecordCodecBuilder.create(instance ->
					instance.group(
							Codec.unboundedMap(Codec.STRING, SLOT_GENERATION_FILTER_CODEC).optionalFieldOf("slots").forGetter(data -> Optional.ofNullable(data.slots()))
					).apply(instance, slots ->
							new GenerationConfigData(slots.orElse(null))));
}
