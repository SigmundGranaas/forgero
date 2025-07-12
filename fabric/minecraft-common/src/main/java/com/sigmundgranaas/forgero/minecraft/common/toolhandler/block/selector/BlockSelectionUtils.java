package com.sigmundgranaas.forgero.minecraft.common.toolhandler.block.selector;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;

/**
 * Utility class for working with different block selections
 */
public class BlockSelectionUtils {

	/**
	 * @param blockPos the root position to find blocks around
	 * @return A set of all the blocks that are adjacent to the given block position, including diagonals
	 */
	public static Set<BlockPos> getBlockPositionsAround(BlockPos blockPos) {
		Set<BlockPos> offsetBlockPositions = new HashSet<>();

		// Get all 26 blocks surrounding the center block (3x3x3 cube excluding center)
		for (int x = -1; x <= 1; x++) {
			for (int y = -1; y <= 1; y++) {
				for (int z = -1; z <= 1; z++) {
					// Skip the center block
					if (x == 0 && y == 0 && z == 0) {
						continue;
					}
					offsetBlockPositions.add(blockPos.add(x, y, z));
				}
			}
		}

		return offsetBlockPositions;
	}

	/**
	 * Predicate for excluding common invalid blocks from the selection
	 *
	 * @param pos  of the checked block
	 * @param view A world view
	 * @return A predicate that returns false if the pos is a common invalid block
	 */
	@SuppressWarnings("RedundantIfStatement")
	public static Predicate<BlockPos> getCommonInvalidBlocks(BlockPos pos, WorldView view) {
		return (blockPos) -> {
			BlockState blockState = view.getBlockState(blockPos);
			if (blockState.isAir()) {
				return false;
			} else if (blockState.getHardness(view, pos) < 0) {
				return false;
			}
			return true;
		};
	}
}
