package com.sigmundgranaas.forgero.properties.minecraft.onhit.handler;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public record StatusEffectHandler(Identifier effect, int duration, int amplifier) implements OnHitHandler {
	public static final String TYPE = "forgero:status_effect";
	public static final Codec<StatusEffectHandler> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Identifier.CODEC.fieldOf("effect").forGetter(StatusEffectHandler::effect),
			Codec.INT.fieldOf("duration").forGetter(StatusEffectHandler::duration),
			Codec.INT.optionalFieldOf("amplifier", 0).forGetter(StatusEffectHandler::amplifier)
	).apply(instance, StatusEffectHandler::new));

	@Override
	public void onHit(Entity source, Entity target) {
		if (target instanceof LivingEntity livingTarget) {
			StatusEffect statusEffect = Registries.STATUS_EFFECT.get(effect);
			if (statusEffect != null) {
				livingTarget.addStatusEffect(new StatusEffectInstance(statusEffect, duration, amplifier));
			}
		}
	}

	@Override
	public String type() {
		return TYPE;
	}
}
