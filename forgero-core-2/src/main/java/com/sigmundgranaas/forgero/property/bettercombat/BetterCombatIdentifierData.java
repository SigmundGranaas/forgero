package com.sigmundgranaas.forgero.property.bettercombat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.property.condition.Condition;
import com.sigmundgranaas.forgero.data.loading.api.data.PropertyData;
import com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record BetterCombatIdentifierData(
		OpenIdentifier value,
		@Nullable Condition condition
) implements PropertyData {
	public static Codec<BetterCombatIdentifierData> createCodec(Codec<Condition> conditionCodec) {
		return RecordCodecBuilder.create(instance -> instance.group(
				CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("value").forGetter(BetterCombatIdentifierData::value),
				conditionCodec.optionalFieldOf("condition")
						.forGetter(data -> Optional.ofNullable(data.condition()))
		).apply(instance, (value, conditionOptional) -> new BetterCombatIdentifierData(value, conditionOptional.orElse(null))));
	}
}
