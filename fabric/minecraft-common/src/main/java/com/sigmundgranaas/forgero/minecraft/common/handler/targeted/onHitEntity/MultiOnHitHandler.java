package com.sigmundgranaas.forgero.minecraft.common.handler.targeted.onHitEntity;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public class MultiOnHitHandler implements EntityTargetHandler {
	private final List<EntityTargetHandler> handlers;

	public MultiOnHitHandler(List<EntityTargetHandler> handlers) {
		this.handlers = handlers;
	}

	@Override
	public void onHit(Entity root, World world, Entity target) {
		handlers.forEach(handler -> handler.onHit(root, world, target));
	}
}
