package com.sigmundgranaas.forgero.property.bettercombat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.property.api.custom.CustomPropertyData;
import com.sigmundgranaas.forgero.data.loading.api.data.condition.ConditionData;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

import static com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants.OPEN_IDENTIFIER_CODEC;
import static com.sigmundgranaas.forgero.data.loading.impl.codec.ConditionCodecs.CONDITION_DATA_CODEC;

public record BetterCombatIdentifierData(
		OpenIdentifier value,
		@Nullable ConditionData condition
) implements CustomPropertyData {
	public static final Codec<BetterCombatIdentifierData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			OPEN_IDENTIFIER_CODEC.fieldOf("value").forGetter(BetterCombatIdentifierData::value),
			CONDITION_DATA_CODEC.optionalFieldOf("condition")
					.forGetter(data -> Optional.ofNullable(data.condition()))
	).apply(instance, (value, conditionOptional) -> new BetterCombatIdentifierData(value, conditionOptional.orElse(null))));
}
