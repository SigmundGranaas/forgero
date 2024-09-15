package com.sigmundgranaas.forgero.predicate.entity;


import static com.sigmundgranaas.forgero.predicate.codecs.KeyPair.predicate;

import java.util.function.Predicate;

import com.sigmundgranaas.forgero.predicate.codecs.KeyPair;

import net.minecraft.entity.Entity;

public class EntityFlagPredicates {
	public static KeyPair<Predicate<Entity>> IS_SNEAKING = predicate("is_sneaking", Entity::isSneaking);
	public static KeyPair<Predicate<Entity>> IS_SPRINTING = predicate("is_sprinting", Entity::isSprinting);
	public static KeyPair<Predicate<Entity>> IS_SWIMMING = predicate("is_swimming", Entity::isSwimming);
	public static KeyPair<Predicate<Entity>> IS_ON_GROUND = predicate("is_on_ground", Entity::isOnGround);
}
