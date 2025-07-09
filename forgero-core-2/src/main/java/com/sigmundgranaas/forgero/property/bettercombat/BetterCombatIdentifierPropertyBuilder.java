package com.sigmundgranaas.forgero.property.bettercombat;

import com.sigmundgranaas.forgero.core.property.api.custom.GenericPropertyBuilder;


public class BetterCombatIdentifierPropertyBuilder extends GenericPropertyBuilder<BetterCombatIdentifierData, BetterCombatIdentifierProperty> {

	public BetterCombatIdentifierPropertyBuilder() {
		super(
				DefaultBetterCombatKeys.BETTER_COMBAT_IDENTIFIER.toString(),
				BetterCombatIdentifierData.CODEC,
				(data, condition) -> new BetterCombatIdentifierProperty(data.value(), condition)
		);
	}
}
