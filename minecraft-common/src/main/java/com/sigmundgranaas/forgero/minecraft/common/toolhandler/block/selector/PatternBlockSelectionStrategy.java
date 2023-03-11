package com.sigmundgranaas.forgero.minecraft.common.toolhandler.block.selector;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

/**
 * A block selector that selects blocks based on a String pattern.
 * The pattern is a list of strings, where each string represents a row in the pattern.
 * Each character in the string represents a block in the pattern.
 * <p>
 * X values represent blocks that should be selected, and empty spaces should be ignored.
 * A C value represents the root position of the pattern. If a c is not present, the root position is assumed to be the center of the pattern.
 * <p>
 * The pattern is rotated based on the player's facing direction.
 * The pattern is applied horizontally or vertically based on the player's facing direction.
 */
public class PatternBlockSelectionStrategy implements BlockSelector {
	private final List<String> pattern;
	private final Direction facingHorizontal;
	private final Direction[] primaryFacing;

	public PatternBlockSelectionStrategy(List<String> pattern, Direction[] primaryFacing, Direction facing) {
		this.pattern = pattern;
		this.facingHorizontal = facing;
		this.primaryFacing = primaryFacing;
	}

	@NotNull
	@Override
	public Set<BlockPos> select(BlockPos rootPos) {
		Set<BlockPos> blocks = new HashSet<>();
		//iterate through the pattern list, and find all the blocks that match the pattern
		var primaryOffset = pattern.size() == 1 ? 1 : -pattern.size() / 2;

		//determine how the pattern should be rotated based on player facing direction
		int rotate = 0;

		//determine if the pattern should be applied horizontally or vertically based on player facing direction
		boolean vertical = primaryFacing[0] != Direction.DOWN && primaryFacing[0] != Direction.UP;

		if (vertical) {
			switch (facingHorizontal) {
				case EAST -> rotate = 3;
				case SOUTH -> rotate = 2;
				case WEST -> rotate = 1;
				case NORTH -> rotate = 0;
			}
		} else {
			switch (facingHorizontal) {
				case EAST -> rotate = 0;
				case SOUTH -> rotate = 3;
				case WEST -> rotate = 2;
				case NORTH -> rotate = 1;
			}
		}
		//iterate through the pattern and check if the blocks match the pattern
		for (int i = 0; i < pattern.size(); i++) {
			for (int j = 0; j < pattern.get(i).length(); j++) {
				var slice = pattern.get(i);
				var secondaryOffset = slice.length() == 1 ? -1 : -slice.length() / 2;
				//If the pattern matches, add the block to the list of blocks that should be broken
				if (slice.charAt(j) == 'x') {

					//determine the position of the block relative to the root position
					int x = vertical ? i + primaryOffset : j + secondaryOffset;
					int y = vertical ? j + secondaryOffset : 0;
					int z = vertical ? 0 : i + primaryOffset;
					//rotate the block position based on the player facing direction
					for (int k = 0; k < rotate; k++) {
						int temp = x;
						x = z;
						z = -temp;
					}
					//Add block to set
					BlockPos pos = rootPos.add(x, y, z);
					blocks.add(pos);
				}
			}
		}
		return blocks;
	}
}
