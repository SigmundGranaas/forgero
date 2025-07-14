package com.sigmundgranaas.forgero.property.namereplacement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.core.property.condition.Condition;
import com.sigmundgranaas.forgero.data.loading.api.data.PropertyData;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record NameReplacementData(
		String from,
		String to,
		@Nullable Condition condition
) implements PropertyData {
	public static Codec<NameReplacementData> createCodec(Codec<Condition> conditionCodec) {
		return RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.fieldOf("from").forGetter(NameReplacementData::from),
				Codec.STRING.fieldOf("to").forGetter(NameReplacementData::to),
				conditionCodec.optionalFieldOf("condition")
						.forGetter(data -> Optional.ofNullable(data.condition()))
		).apply(instance, (from, to, conditionOptional) -> new NameReplacementData(from, to, conditionOptional.orElse(null))));
	}
}
