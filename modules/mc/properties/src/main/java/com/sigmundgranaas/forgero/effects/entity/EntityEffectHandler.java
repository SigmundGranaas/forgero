package com.sigmundgranaas.forgero.effects.entity;

import net.minecraft.entity.Entity;

/**
 * An effect that can be applied to a single entity, without needing any other context.
 * <p>
 * Example: Setting an entity on fire only needs to know which entity to ignite.
 */
public interface EntityEffectHandler extends OnHitEffect {
	void apply(Entity entity);
}
