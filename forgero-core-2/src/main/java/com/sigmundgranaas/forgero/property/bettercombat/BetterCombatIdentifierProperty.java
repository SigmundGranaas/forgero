package com.sigmundgranaas.forgero.property.bettercombat;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.property.api.custom.ConditionalProperty;
import com.sigmundgranaas.forgero.core.property.condition.Condition;

import javax.annotation.Nullable;

public record BetterCombatIdentifierProperty(
		OpenIdentifier identifier,
		@Nullable Condition condition
) implements ConditionalProperty {

}
