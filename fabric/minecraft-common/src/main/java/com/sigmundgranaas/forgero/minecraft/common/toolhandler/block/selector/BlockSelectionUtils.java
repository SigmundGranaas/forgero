package com.sigmundgranaas.forgero.minecraft.common.toolhandler.block.selector;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldView;

/**
 * Utility class for working with different block selections
 */
public class BlockSelectionUtils {

	/**
	 * @param blockPos the root position to find blocks around
	 * @return A set of all the blocks around the given block position
	 */
	public static Set<BlockPos> getBlockPositionsAround(BlockPos blockPos) {
		var directions = Direction.values();
		var eightWayDirections = EightWayDirection.values();
		var offsetBlockPositions = new HashSet<BlockPos>();

		for (Direction direction : directions) {
			BlockPos offsetBlockPos = blockPos.offset(direction);
			offsetBlockPositions.add(offsetBlockPos);
		}

		for (EightWayDirection eightWayDirection : eightWayDirections) {
			BlockPos offsetBlockPos = blockPos.add(eightWayDirection.getOffsetX(), 1, eightWayDirection.getOffsetZ());
			BlockPos offsetBlockPos2 = blockPos.add(eightWayDirection.getOffsetX(), -1, eightWayDirection.getOffsetZ());
			offsetBlockPositions.add(offsetBlockPos);
			offsetBlockPositions.add(offsetBlockPos2);
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

	private enum EightWayDirection {
		NORTH(Direction.NORTH),
		NORTH_EAST(Direction.NORTH, Direction.EAST),
		EAST(Direction.EAST),
		SOUTH_EAST(Direction.SOUTH, Direction.EAST),
		SOUTH(Direction.SOUTH),
		SOUTH_WEST(Direction.SOUTH, Direction.WEST),
		WEST(Direction.WEST),
		NORTH_WEST(Direction.NORTH, Direction.WEST);

		private final Direction[] directions;

		EightWayDirection(Direction... directions) {
			this.directions = directions;
		}

		public int getOffsetX() {
			return Arrays.stream(directions).mapToInt(Direction::getOffsetX).sum();
		}

		public int getOffsetZ() {
			return Arrays.stream(directions).mapToInt(Direction::getOffsetZ).sum();
		}
	}
}
