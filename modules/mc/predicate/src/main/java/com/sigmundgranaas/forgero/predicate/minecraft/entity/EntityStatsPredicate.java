package com.sigmundgranaas.forgero.predicate.minecraft.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.predicate.minecraft.util.NumericPredicate;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

import java.util.Optional;

/**
 * Predicate for an entity's numerical stats.
 */
public record EntityStatsPredicate(
		Optional<NumericPredicate> health,
		Optional<NumericPredicate> fall_distance
) {
	public static final Codec<EntityStatsPredicate> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					NumericPredicate.CODEC.optionalFieldOf("health").forGetter(EntityStatsPredicate::health),
					NumericPredicate.CODEC.optionalFieldOf("fall_distance").forGetter(EntityStatsPredicate::fall_distance)
			).apply(instance, EntityStatsPredicate::new)
	);

	public boolean test(Entity entity) {
		boolean fallDistMatch = fall_distance.map(p -> p.test(entity.fallDistance)).orElse(true);
		boolean healthMatch = health.map(p -> {
			if (entity instanceof LivingEntity living) {
				return p.test(living.getHealth());
			}
			return false; // Or true, depending on desired behavior for non-living entities
		}).orElse(true);

		return fallDistMatch && healthMatch;
	}
}
