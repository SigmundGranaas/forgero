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
	 * @return A set of all the blocks that are directly adjacent to the given block position (sharing a face)
	 */
	public static Set<BlockPos> getBlockPositionsAround(BlockPos blockPos) {
		Set<BlockPos> offsetBlockPositions = new HashSet<>();

		// Add only the 6 directly adjacent blocks (sharing a face)
		offsetBlockPositions.add(blockPos.add(1, 0, 0));  // East
		offsetBlockPositions.add(blockPos.add(-1, 0, 0)); // West
		offsetBlockPositions.add(blockPos.add(0, 1, 0));  // Up
		offsetBlockPositions.add(blockPos.add(0, -1, 0)); // Down
		offsetBlockPositions.add(blockPos.add(0, 0, 1));  // South
		offsetBlockPositions.add(blockPos.add(0, 0, -1)); // North

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
