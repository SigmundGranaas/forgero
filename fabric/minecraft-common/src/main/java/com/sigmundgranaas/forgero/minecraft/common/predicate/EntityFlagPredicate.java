package com.sigmundgranaas.forgero.minecraft.common.predicate;


import static com.sigmundgranaas.forgero.minecraft.common.predicate.KeyPair.predicate;

import net.minecraft.entity.Entity;

public class EntityFlagPredicate {
	public static final SpecificationRegistry<Predicate<Entity>> REGISTRY = new SpecificationRegistry<>();

	public static KeyPair<Predicate<Entity>> IS_SNEAKING = predicate("is_sneaking", Entity::isSneaking);

	public static KeyPair<Predicate<Entity>> IS_SPRINTING = predicate("is_sprinting", Entity::isSprinting);

	static {
		REGISTRY.register(IS_SNEAKING.key(), IS_SNEAKING);
		REGISTRY.register(IS_SPRINTING.key(), IS_SPRINTING);
	}
}
