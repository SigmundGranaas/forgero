package com.sigmundgranaas.forgero.properties.minecraft.entityfilter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

/**
 * Filters entities that have a specific status effect active.
 * Example: "minecraft:poison", "minecraft:strength", "minecraft:regeneration"
 */
public record HasEffectFilter(String effect) implements EntityFilter {
	public static final String TYPE = "forgero:has_effect";
	public static final Codec<HasEffectFilter> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("effect").forGetter(HasEffectFilter::effect)
	).apply(instance, HasEffectFilter::new));

	@Override
	public boolean test(Entity source, Entity candidate) {
		if (!(candidate instanceof LivingEntity living)) {
			return false;
		}

		Identifier effectId = Identifier.tryParse(effect);
		if (effectId == null) {
			return false;
		}

		// Check if any active effect matches the requested identifier
		for (StatusEffectInstance instance : living.getStatusEffects()) {
			StatusEffect activeEffect = instance.getEffectType();
			Identifier activeEffectId = Registries.STATUS_EFFECT.getId(activeEffect);
			if (activeEffectId != null && activeEffectId.equals(effectId)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public String type() {
		return TYPE;
	}
}
