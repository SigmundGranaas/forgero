package com.sigmundgranaas.forgero.minecraft.common.handler.targeted.onHitEntity;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public class MultiOnHitHandler implements OnHitHandler {
	private final List<OnHitHandler> handlers;

	public MultiOnHitHandler(List<OnHitHandler> handlers) {
		this.handlers = handlers;
	}

	@Override
	public void onHit(Entity root, World world, Entity target) {
		handlers.forEach(handler -> handler.onHit(root, world, target));
	}
}
