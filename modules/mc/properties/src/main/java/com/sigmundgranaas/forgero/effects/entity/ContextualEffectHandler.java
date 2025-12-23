package com.sigmundgranaas.forgero.effects.entity;

import net.minecraft.entity.Entity;

/**
 * An effect that requires knowledge of both the source of the effect (e.g., the attacker)
 * and the target.
 * <p>
 * Example: Life Steal needs to know who to heal (source) and who to damage (target).
 */
public interface ContextualEffectHandler extends OnHitEffect {
	void apply(Entity source, Entity target);
}
