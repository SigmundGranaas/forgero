package com.sigmundgranaas.forgero.minecraft.common.toolhandler.block;

import java.util.Set;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public record BlockBreakingHandler(BlockBreakingStrategy strategy) implements BlockBreakingStrategy {
	public static boolean isBreakableBlock(BlockView world, BlockPos pos, PlayerEntity player) {
		BlockState state = world.getBlockState(pos);

		if (state.isAir() || state.getHardness(world, pos) < 0) {
			return false;
		} else return player.canHarvest(state);
	}

	public float getHardness(BlockState rootState, BlockPos rootPos, BlockView world, PlayerEntity player) {
		float hardness = rootState.getHardness(world, rootPos);
		if (hardness == -1.0f) {
			return hardness;
		}

		float breakingSpeed = 0.0f;
		hardness = 0.0f;
		var availableBlocks = getAvailableBlocks(world, rootPos, player);

		for (BlockPos pos : availableBlocks) {
			var state = world.getBlockState(pos);

			// I don't know which parameters should be here
			float harvestable = player.canHarvest(state) ? 30 : 100;
			hardness += state.getHardness(world, pos) * harvestable;
			breakingSpeed += player.getBlockBreakingSpeed(state);
		}

		breakingSpeed = breakingSpeed / availableBlocks.size();
		return breakingSpeed / hardness;
	}

	@Override
	public Set<BlockPos> getAvailableBlocks(BlockView world, BlockPos rootPos, PlayerEntity player) {
		return strategy.getAvailableBlocks(world, rootPos, player);
	}
}
