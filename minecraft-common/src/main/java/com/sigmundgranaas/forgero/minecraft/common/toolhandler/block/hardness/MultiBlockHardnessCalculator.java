package com.sigmundgranaas.forgero.minecraft.common.toolhandler.block.hardness;

import com.sigmundgranaas.forgero.minecraft.common.toolhandler.block.selector.BlockSelector;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

/**
 * This class is used to calculate the hardness of a block based on the hardness of a selection of blocks.
 * <p>
 * The hardness of a selection of blocks is calculated by taking the average hardness of all the blocks in the selection
 * divided by the mining speed of the player.
 * <p>
 * If the selection is smaller than 2 blocks, the hardness of the single block is returned.
 */
public class MultiBlockHardnessCalculator implements HardnessProvider {
	private final BlockSelector selector;
	private final BlockView view;
	private final PlayerEntity player;

	public MultiBlockHardnessCalculator(BlockSelector selector, BlockView view, PlayerEntity player) {
		this.selector = selector;
		this.view = view;
		this.player = player;
	}


	@Override
	public float hardness(BlockPos rootPos) {
		var singleBlockHardness = HardnessProvider.of(view).hardness(rootPos);
		if (singleBlockHardness == -1.0f) {
			return singleBlockHardness;
		}

		float breakingSpeed = 0.0f;
		float hardness = 0.0f;
		var availableBlocks = selector.select(rootPos);
		if (availableBlocks.size() < 2) {
			return singleBlockHardness;
		}

		for (BlockPos pos : availableBlocks) {
			var state = view.getBlockState(pos);

			float harvestable = player.canHarvest(state) ? 30 : 100;
			hardness += state.getHardness(view, pos) * harvestable;
			breakingSpeed += player.getBlockBreakingSpeed(state);
		}

		breakingSpeed = breakingSpeed / availableBlocks.size();
		return breakingSpeed / hardness;
	}
}
