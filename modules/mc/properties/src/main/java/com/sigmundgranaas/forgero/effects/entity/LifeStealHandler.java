package com.sigmundgranaas.forgero.effects.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

public record LifeStealHandler(float amount) implements ContextualEffectHandler {
	public static final String TYPE = "forgero:life_steal";
	public static final Codec<LifeStealHandler> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.FLOAT.fieldOf("amount").forGetter(LifeStealHandler::amount)
	).apply(instance, LifeStealHandler::new));

	@Override
	public void apply(Entity source, Entity target) {
		if (target instanceof LivingEntity livingTarget && source instanceof LivingEntity livingSource) {
			float healthToSteal = Math.min(livingTarget.getHealth(), amount);
			livingTarget.damage(target.getDamageSources().magic(), healthToSteal);
			livingSource.heal(healthToSteal);
		}
	}

	@Override
	public String type() {
		return TYPE;
	}
}
