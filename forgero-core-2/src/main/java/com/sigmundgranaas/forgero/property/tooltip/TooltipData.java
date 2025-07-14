package com.sigmundgranaas.forgero.property.tooltip;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.property.condition.Condition;
import com.sigmundgranaas.forgero.data.loading.api.data.PropertyData;
import com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record TooltipData(
		OpenIdentifier key,
		String value,
		@Nullable String format,
		@Nullable Condition condition
) implements PropertyData {
	public static Codec<TooltipData> createCodec(Codec<Condition> conditionCodec) {
		return RecordCodecBuilder.create(instance -> instance.group(
				CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("key").forGetter(TooltipData::key),
				Codec.STRING.fieldOf("value").forGetter(TooltipData::value),
				Codec.STRING.optionalFieldOf("format").forGetter(data -> Optional.ofNullable(data.format)),
				conditionCodec.optionalFieldOf("condition")
						.forGetter(data -> Optional.ofNullable(data.condition()))
		).apply(instance, (key, value, format, conditionOptional) -> new TooltipData(key, value, format.orElse("TEXT"), conditionOptional.orElse(null))));
	}
}
