package com.sigmundgranaas.forgero.data.v3.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.data.v3.dto.feature.FeatureCodecRegistry;
import com.sigmundgranaas.forgero.data.v3.dto.feature.FeatureData;
import com.sigmundgranaas.forgero.data.v3.dto.feature.VeinMiningFeatureData;
import com.sigmundgranaas.forgero.data.v3.dto.feature.VeinMiningSelectorData;


import java.util.Optional;

public class FeatureCodecs {

	public static final Codec<VeinMiningSelectorData> VEIN_MINING_SELECTOR_CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("type").forGetter(VeinMiningSelectorData::type),
					Codec.INT.fieldOf("radius").forGetter(VeinMiningSelectorData::radius),
					CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("tag").forGetter(VeinMiningSelectorData::tag)
			).apply(instance, VeinMiningSelectorData::new));


	public static final Codec<VeinMiningFeatureData> VEIN_MINING_FEATURE_CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("type").forGetter(VeinMiningFeatureData::type),
					Codec.STRING.fieldOf("title").forGetter(VeinMiningFeatureData::title),
					Codec.STRING.fieldOf("description").forGetter(VeinMiningFeatureData::description),
					VEIN_MINING_SELECTOR_CODEC.fieldOf("selector").forGetter(VeinMiningFeatureData::selector),
					ConditionCodecs.CONDITION_DATA_CODEC.optionalFieldOf("condition").forGetter(data -> Optional.ofNullable(data.condition()))
			).apply(instance, (type, title, description, selector, condition) ->
					new VeinMiningFeatureData(type, title, description, selector, condition.orElse(null))));


	public static final Codec<FeatureData> FEATURE_DATA_CODEC = new Codec<>() {
		@Override
		public <T> DataResult<Pair<FeatureData, T>> decode(DynamicOps<T> ops, T input) {
			return ops.get(input, "type")
					.flatMap(ops::getStringValue)
					.flatMap(FeatureCodecRegistry::get)
					.flatMap(codec -> codec.decode(ops, input)
							.map(pair -> Pair.of(pair.getFirst(), pair.getSecond()))
					);
		}

		@Override
		@SuppressWarnings({"unchecked", "rawtypes"})
		public <T> DataResult<T> encode(FeatureData input, DynamicOps<T> ops, T prefix) {
			return FeatureCodecRegistry.get(input.type().toString())
					.flatMap(codec -> ((Codec) codec).encode(input, ops, prefix));
		}
	};

	static {
		FeatureCodecRegistry.register("forgero:vein_mining", VEIN_MINING_FEATURE_CODEC);
	}
}
