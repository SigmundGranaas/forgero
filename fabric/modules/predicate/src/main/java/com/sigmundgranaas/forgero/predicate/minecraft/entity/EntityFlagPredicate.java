package com.sigmundgranaas.forgero.predicate.minecraft.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.Entity;

import java.util.Optional;

/**
 * Predicate for an entity's boolean states (flags).
 */
public record EntityFlagPredicate(
		Optional<Boolean> is_sneaking,
		Optional<Boolean> is_on_fire,
		Optional<Boolean> is_on_ground,
		Optional<Boolean> is_sprinting,
		Optional<Boolean> is_swimming
) {
	public static final Codec<EntityFlagPredicate> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					Codec.BOOL.optionalFieldOf("is_sneaking").forGetter(EntityFlagPredicate::is_sneaking),
					Codec.BOOL.optionalFieldOf("is_on_fire").forGetter(EntityFlagPredicate::is_on_fire),
					Codec.BOOL.optionalFieldOf("is_on_ground").forGetter(EntityFlagPredicate::is_on_ground),
					Codec.BOOL.optionalFieldOf("is_sprinting").forGetter(EntityFlagPredicate::is_sprinting),
					Codec.BOOL.optionalFieldOf("is_swimming").forGetter(EntityFlagPredicate::is_swimming)
			).apply(instance, EntityFlagPredicate::new)
	);

	public boolean test(Entity entity) {
		boolean sneakMatch = is_sneaking.map(value -> entity.isSneaking() == value).orElse(true);
		boolean fireMatch = is_on_fire.map(value -> entity.isOnFire() == value).orElse(true);
		boolean groundMatch = is_on_ground.map(value -> entity.isOnGround() == value).orElse(true);
		boolean sprintMatch = is_sprinting.map(value -> entity.isSprinting() == value).orElse(true);
		boolean swimMatch = is_swimming.map(value -> entity.isSwimming() == value).orElse(true);
		return sneakMatch && fireMatch && groundMatch && sprintMatch && swimMatch;
	}
}
