package com.sigmundgranaas.forgero.effects.api;

import com.sigmundgranaas.forgero.effects.entity.ContextualEffectHandler;
import net.minecraft.entity.Entity;

/**
 * Bridges a downstream {@link SourceTargetAction} (and its JSON config {@code C}) onto the internal
 * {@link ContextualEffectHandler}. Holds the config so the registration codec can round-trip it.
 * Package-private: addons interact via {@link OnHitEffects}.
 *
 * @param <C> the addon's config record type
 */
final class FunctionalContextualEffect<C> implements ContextualEffectHandler {

	private final String type;
	private final C config;
	private final SourceTargetAction<C> action;

	FunctionalContextualEffect(String type, C config, SourceTargetAction<C> action) {
		this.type = type;
		this.config = config;
		this.action = action;
	}

	C config() {
		return config;
	}

	@Override
	public void apply(Entity source, Entity target) {
		action.apply(config, source, target);
	}

	@Override
	public String type() {
		return type;
	}
}
