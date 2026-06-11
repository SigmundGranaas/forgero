package com.sigmundgranaas.forgero.properties.minecraft;

import com.sigmundgranaas.forgero.effects.entity.ContextualEffectHandler;
import com.sigmundgranaas.forgero.effects.entity.EntityEffectHandler;
import com.sigmundgranaas.forgero.effects.entity.OnHitEffect;
import com.sigmundgranaas.forgero.properties.minecraft.entityselector.EntitySelector;
import net.minecraft.entity.Entity;

import java.util.List;

/**
 * Runs entity-targeted effects: the selector+effects dispatch shared by every entity-event
 * property (on-hit, on-tick, on-kill, on-damage, on-sneak, projectiles). The selector chooses
 * the final targets from (source, initial target); each effect applies to each target, with
 * contextual effects receiving the source.
 */
public final class EntityEffects {

	private EntityEffects() {
	}

	public static void apply(EntitySelector selector, List<OnHitEffect> effects, Entity source, Entity target) {
		for (Entity finalTarget : selector.select(source, target)) {
			for (OnHitEffect effect : effects) {
				if (effect instanceof ContextualEffectHandler contextual) {
					contextual.apply(source, finalTarget);
				} else if (effect instanceof EntityEffectHandler simple) {
					simple.apply(finalTarget);
				}
			}
		}
	}
}
