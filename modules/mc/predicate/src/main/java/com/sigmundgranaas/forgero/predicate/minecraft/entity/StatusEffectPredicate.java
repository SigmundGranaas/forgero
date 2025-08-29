package com.sigmundgranaas.forgero.predicate.minecraft.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.predicate.minecraft.util.NumericPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.Optional;

/**
 * Predicate for checking status effects on a living entity.
 * Checks for the presence of effects and optionally their amplifier and duration.
 *
 * <p><h3>Example:</h3>
 * <pre>
 * "effects": {
 *   "minecraft:speed": {
 *     "amplifier": { "min": 1 },
 *     "duration": { "min": 200 }
 *   },
 *   "minecraft:haste": {}
 * }
 * </pre>
 */
public record StatusEffectPredicate(Map<StatusEffect, EffectData> effects) {

	public record EffectData(
			Optional<NumericPredicate> amplifier,
			Optional<NumericPredicate> duration
	) {
		public static final Codec<EffectData> CODEC = RecordCodecBuilder.create(instance ->
				instance.group(
						NumericPredicate.CODEC.optionalFieldOf("amplifier").forGetter(EffectData::amplifier),
						NumericPredicate.CODEC.optionalFieldOf("duration").forGetter(EffectData::duration)
				).apply(instance, EffectData::new)
		);

		public boolean test(StatusEffectInstance instance) {
			// Amplifiers are 0-indexed (e.g., Speed II is amplifier 1)
			boolean ampMatch = amplifier.map(p -> p.test(instance.getAmplifier())).orElse(true);
			// Duration is in ticks
			boolean durMatch = duration.map(p -> p.test(instance.getDuration())).orElse(true);
			return ampMatch && durMatch;
		}
	}

	private static final Codec<StatusEffect> EFFECT_CODEC = Identifier.CODEC.xmap(Registries.STATUS_EFFECT::get, Registries.STATUS_EFFECT::getId);
	public static final Codec<StatusEffectPredicate> CODEC = Codec.unboundedMap(EFFECT_CODEC, EffectData.CODEC)
			.xmap(StatusEffectPredicate::new, StatusEffectPredicate::effects);

	public boolean test(LivingEntity entity) {
		// This predicate requires ALL specified effects to be present and match their criteria.
		for (Map.Entry<StatusEffect, EffectData> entry : effects.entrySet()) {
			StatusEffect effect = entry.getKey();
			EffectData data = entry.getValue();

			StatusEffectInstance instance = entity.getStatusEffect(effect);
			if (instance == null) {
				return false; // The entity does not have this effect.
			}

			if (!data.test(instance)) {
				return false; // The effect instance does not match the predicate.
			}
		}
		return true;
	}
}
