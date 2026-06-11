package com.sigmundgranaas.forgero.properties.property.bettercombat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.property.api.PropertyKey;
import com.sigmundgranaas.forgero.core.property.api.ResolutionKey;
import com.sigmundgranaas.forgero.core.property.api.custom.ConditionalProperty;
import com.sigmundgranaas.forgero.core.condition.api.Condition;
import com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public record BetterCombatIdentifierProperty(
		OpenIdentifier identifier,
		@Nullable Condition condition
) implements ConditionalProperty {
	public static final OpenIdentifier KEY_ID = new OpenIdentifier("better_combat", "attribute_container");
	public static final PropertyKey<BetterCombatIdentifierProperty> PROPERTY_KEY = new PropertyKey<>(BetterCombatIdentifierProperty.class, KEY_ID.toString());

	public static Codec<BetterCombatIdentifierProperty> codec(Codec<Condition> conditionCodec) {
		return RecordCodecBuilder.create(instance -> instance.group(
				CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("identifier").forGetter(BetterCombatIdentifierProperty::identifier),
				conditionCodec.optionalFieldOf("condition").forGetter(p -> Optional.ofNullable(p.condition()))
		).apply(instance, (id, cond) -> new BetterCombatIdentifierProperty(id, cond.orElse(null))));
	}

}
