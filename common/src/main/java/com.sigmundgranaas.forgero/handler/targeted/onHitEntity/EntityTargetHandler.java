package com.sigmundgranaas.forgero.handler.targeted.onHitEntity;

import com.sigmundgranaas.forgero.core.property.v2.feature.ClassKey;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;

@FunctionalInterface
public interface EntityTargetHandler {
	String type = "minecraft:entity_target";
	ClassKey<EntityTargetHandler> KEY = new ClassKey<>(type, EntityTargetHandler.class);
	EntityTargetHandler DEFAULT = (Entity root, World world, Entity target) -> {
	};

	void onHit(Entity root, World world, Entity target);
}
