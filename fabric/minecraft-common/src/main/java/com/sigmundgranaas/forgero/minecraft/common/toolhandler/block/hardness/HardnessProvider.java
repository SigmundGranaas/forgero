package com.sigmundgranaas.forgero.minecraft.common.toolhandler.block.hardness;

import com.sigmundgranaas.forgero.minecraft.common.toolhandler.block.selector.BlockSelector;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

/**
 * Interface for calculating the hardness of blocks.
 * Can be used to calculate the hardness of a single block or a selection of blocks.
 */
public interface HardnessProvider {
	static HardnessProvider of(BlockView view) {
		return new SingleBlockHardnessProvider(view);
	}

	static HardnessProvider of(BlockView view, PlayerEntity player, BlockSelector selector) {
		return new MultiBlockHardnessCalculator(selector, view, player);
	}

	/**
	 * @param pos the root position of the block to calculate the hardness of. Can be used to calculate the hardness of a selection of blocks.
	 * @return The hardness of the block/blocks at the given position. Returns -1 if the block is not valid.
	 */
	float getHardnessAt(BlockPos pos);
}
