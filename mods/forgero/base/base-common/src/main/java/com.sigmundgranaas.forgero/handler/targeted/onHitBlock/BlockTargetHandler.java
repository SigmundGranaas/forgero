package com.sigmundgranaas.forgero.handler.targeted.onHitBlock;

import com.sigmundgranaas.forgero.core.property.v2.feature.ClassKey;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface BlockTargetHandler {
	String type = "minecraft:block_target";
	ClassKey<BlockTargetHandler> KEY = new ClassKey<>(type, BlockTargetHandler.class);
	BlockTargetHandler DEFAULT = (Entity root, World world, BlockPos pos) -> {
	};

	void onHit(Entity root, World world, BlockPos pos);
}
