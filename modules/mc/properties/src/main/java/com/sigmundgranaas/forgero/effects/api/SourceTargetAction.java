package com.sigmundgranaas.forgero.effects.api;

import net.minecraft.entity.Entity;

/**
 * A custom on-hit effect that needs both the attacker (source) and the victim (target), plus its
 * parsed JSON config. Used with
 * {@link OnHitEffects#registerSourceTarget(String, com.mojang.serialization.Codec, SourceTargetAction)}.
 *
 * @param <C> the effect's config record type
 */
@FunctionalInterface
public interface SourceTargetAction<C> {
	void apply(C config, Entity source, Entity target);
}
