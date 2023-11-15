package com.sigmundgranaas.forgero.minecraft.common.handler.targeted.onHitEntity;

import com.sigmundgranaas.forgero.core.property.v2.feature.ClassKey;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;

@FunctionalInterface
public interface OnHitHandler {
	String type = "minecraft:on_hit";
	ClassKey<OnHitHandler> KEY = new ClassKey<>(type, OnHitHandler.class);
	OnHitHandler DEFAULT = (Entity root, World world, Entity target) -> {
	};

	void onHit(Entity root, World world, Entity target);
}
