package com.sigmundgranaas.forgero.property.namereplacement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.core.property.api.custom.CustomPropertyData;
import com.sigmundgranaas.forgero.data.loading.api.data.condition.ConditionData;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

import static com.sigmundgranaas.forgero.data.loading.impl.codec.ConditionCodecs.CONDITION_DATA_CODEC;

public record NameReplacementData(
		String from,
		String to,
		@Nullable ConditionData condition
) implements CustomPropertyData {
	public static final Codec<NameReplacementData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("from").forGetter(NameReplacementData::from),
			Codec.STRING.fieldOf("to").forGetter(NameReplacementData::to),
			CONDITION_DATA_CODEC.optionalFieldOf("condition")
					.forGetter(data -> Optional.ofNullable(data.condition()))
	).apply(instance, (from, to, conditionOptional) -> new NameReplacementData(from, to, conditionOptional.orElse(null))));
}
