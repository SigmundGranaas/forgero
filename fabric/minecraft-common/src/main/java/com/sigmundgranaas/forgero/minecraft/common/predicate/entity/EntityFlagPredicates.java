package com.sigmundgranaas.forgero.minecraft.common.predicate.entity;


import static com.sigmundgranaas.forgero.minecraft.common.predicate.codecs.KeyPair.predicate;

import java.util.function.Predicate;

import com.sigmundgranaas.forgero.minecraft.common.predicate.codecs.KeyPair;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public class EntityFlagPredicates {
	public static final Predicate<Entity> isUsing = new IsUsing();

	public static KeyPair<Predicate<Entity>> IS_SNEAKING = predicate("is_sneaking", Entity::isSneaking);
	public static KeyPair<Predicate<Entity>> IS_SPRINTING = predicate("is_sprinting", Entity::isSprinting);
	public static KeyPair<Predicate<Entity>> IS_SWIMMING = predicate("is_swimming", Entity::isSwimming);
	public static KeyPair<Predicate<Entity>> IS_ON_GROUND = predicate("is_on_ground", Entity::isOnGround);
	public static KeyPair<Predicate<Entity>> IS_USING = predicate("is_using", isUsing);


	private static class IsUsing implements Predicate<Entity> {

		@Override
		public boolean test(Entity entity) {
			return entity instanceof PlayerEntity player && player.getItemUseTime() != player.getItemUseTimeLeft();
		}
	}
}
