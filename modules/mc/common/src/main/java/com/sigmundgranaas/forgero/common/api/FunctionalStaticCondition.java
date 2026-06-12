package com.sigmundgranaas.forgero.common.api;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.condition.api.StaticCondition;
import com.sigmundgranaas.forgero.core.property.compilation.ResolutionContext;

import java.util.function.BiPredicate;

/**
 * Bridges a downstream-authored {@code BiPredicate<C, ConditionContext>} (and its JSON config
 * {@code C}) onto the internal {@link StaticCondition} type. Holds the config so the registration
 * codec can round-trip it. Package-private: addons interact via
 * {@link PluginRegistrationContext#registerStaticCondition}.
 *
 * @param <C> the addon's config record type
 */
final class FunctionalStaticCondition<C> implements StaticCondition {

	private final OpenIdentifier type;
	private final C config;
	private final BiPredicate<C, ConditionContext> predicate;

	FunctionalStaticCondition(OpenIdentifier type, C config, BiPredicate<C, ConditionContext> predicate) {
		this.type = type;
		this.config = config;
		this.predicate = predicate;
	}

	C config() {
		return config;
	}

	@Override
	public boolean test(ResolutionContext context) {
		return predicate.test(config, ConditionContext.of(context));
	}

	@Override
	public OpenIdentifier type() {
		return type;
	}
}
