package com.sigmundgranaas.forgero.property.bettercombat;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.property.api.ResolutionKey;
import com.sigmundgranaas.forgero.core.property.api.custom.AbstractConditionalPropertyEngine;
import com.sigmundgranaas.forgero.core.property.api.custom.OptimizedBakedResult;
import com.sigmundgranaas.forgero.core.property.context.DynamicContext;

import java.util.Optional;

import static com.sigmundgranaas.forgero.property.bettercombat.DefaultBetterCombatKeys.BETTER_COMBAT_IDENTIFIER;

public class BetterCombatIdentifierEngine extends AbstractConditionalPropertyEngine<BetterCombatIdentifierProperty, Optional<OpenIdentifier>> {
	public static final ResolutionKey<Optional<OpenIdentifier>> KEY = new ResolutionKey<>(BETTER_COMBAT_IDENTIFIER);

	public BetterCombatIdentifierEngine() {
		super(KEY, BetterCombatIdentifierProperty.class);
	}

	@Override
	public Optional<OpenIdentifier> apply(OptimizedBakedResult<BetterCombatIdentifierProperty> baked, DynamicContext context) {
		return baked.stream(context)
				.map(BetterCombatIdentifierProperty::identifier)
				.findFirst();
	}
}
