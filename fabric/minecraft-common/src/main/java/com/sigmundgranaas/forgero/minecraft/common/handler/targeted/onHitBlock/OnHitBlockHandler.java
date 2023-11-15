package com.sigmundgranaas.forgero.minecraft.common.handler.targeted.onHitBlock;

import com.sigmundgranaas.forgero.core.property.v2.feature.ClassKey;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface OnHitBlockHandler {
	String type = "minecraft:on_hit_block";
	ClassKey<OnHitBlockHandler> KEY = new ClassKey<>(type, OnHitBlockHandler.class);
	OnHitBlockHandler DEFAULT = (Entity root, World world, BlockPos pos) -> {
	};

	void onHit(Entity root, World world, BlockPos pos);

}
