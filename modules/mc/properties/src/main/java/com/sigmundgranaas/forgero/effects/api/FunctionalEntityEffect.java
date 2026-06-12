package com.sigmundgranaas.forgero.effects.api;

import com.sigmundgranaas.forgero.effects.entity.EntityEffectHandler;
import net.minecraft.entity.Entity;

import java.util.function.BiConsumer;

/**
 * Bridges a downstream {@code BiConsumer<C, Entity>} (and its JSON config {@code C}) onto the
 * internal {@link EntityEffectHandler}. Holds the config so the registration codec can round-trip
 * it. Package-private: addons interact via {@link OnHitEffects}.
 *
 * @param <C> the addon's config record type
 */
final class FunctionalEntityEffect<C> implements EntityEffectHandler {

	private final String type;
	private final C config;
	private final BiConsumer<C, Entity> action;

	FunctionalEntityEffect(String type, C config, BiConsumer<C, Entity> action) {
		this.type = type;
		this.config = config;
		this.action = action;
	}

	C config() {
		return config;
	}

	@Override
	public void apply(Entity entity) {
		action.accept(config, entity);
	}

	@Override
	public String type() {
		return type;
	}
}
