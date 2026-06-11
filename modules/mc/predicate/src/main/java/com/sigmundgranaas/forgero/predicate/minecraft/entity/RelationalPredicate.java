package com.sigmundgranaas.forgero.predicate.minecraft.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.common.runtime.DynamicContext;
import com.sigmundgranaas.forgero.predicate.minecraft.MinecraftContextKeys;
import com.sigmundgranaas.forgero.predicate.minecraft.util.NumericPredicate;

import net.minecraft.entity.Entity;

import java.util.Optional;

/**
 * Predicate for checks that relate one entity to another or to the context.
 */
public record RelationalPredicate(
		Optional<NumericPredicate> distance_to
) {
	public static final Codec<RelationalPredicate> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					NumericPredicate.CODEC.optionalFieldOf("distance_to").forGetter(RelationalPredicate::distance_to)
			).apply(instance, RelationalPredicate::new)
	);

	public boolean test(DynamicContext context) {
		return distance_to.map(p -> {
			Optional<Entity> self = context.get(MinecraftContextKeys.ENTITY);
			Optional<Entity> targetEntity = context.get(MinecraftContextKeys.TARGET_ENTITY);
			// This check requires both a primary entity and a target.
			if (self.isPresent() && targetEntity.isPresent()) {
				return p.test(self.get().distanceTo(targetEntity.get()));
			}
			return false;
		}).orElse(true);
	}
}
