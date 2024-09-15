package com.sigmundgranaas.forgero.handler.blockbreak.hardness;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;

public class Utils {

	public static float calculateDelta(BlockState state, PlayerEntity player, WorldView world, BlockPos pos) {
		float hardness = state.getHardness(world, pos);
		if (hardness == -1.0f) {
			return 0.0f;
		}
		int modifier = player.canHarvest(state) ? 30 : 100;
		return player.getBlockBreakingSpeed(state) / hardness / (float) modifier;
	}

	public static BlockState getStateFromWorld(Entity source, BlockPos pos) {
		return source.getWorld().getBlockState(pos);
	}
}
