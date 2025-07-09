package com.sigmundgranaas.forgero.property.tooltip;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.property.api.custom.CustomPropertyData;
import com.sigmundgranaas.forgero.data.loading.api.data.condition.ConditionData;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

import static com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants.OPEN_IDENTIFIER_CODEC;
import static com.sigmundgranaas.forgero.data.loading.impl.codec.ConditionCodecs.CONDITION_DATA_CODEC;

public record TooltipData(
		OpenIdentifier key,
		String value,
		@Nullable String format,
		@Nullable ConditionData condition
) implements CustomPropertyData {
	public static final Codec<TooltipData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			OPEN_IDENTIFIER_CODEC.fieldOf("key").forGetter(TooltipData::key),
			Codec.STRING.fieldOf("value").forGetter(TooltipData::value),
			Codec.STRING.optionalFieldOf("format", "TEXT").forGetter(TooltipData::format),
			CONDITION_DATA_CODEC.optionalFieldOf("condition")
					.forGetter(data -> Optional.ofNullable(data.condition()))
	).apply(instance, (key, value, format, conditionOptional) -> new TooltipData(key, value, format, conditionOptional.orElse(null))));
}
