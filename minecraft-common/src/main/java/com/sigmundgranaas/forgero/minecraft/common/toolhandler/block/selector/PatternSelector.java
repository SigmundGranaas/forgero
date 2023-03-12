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
public class PatternSelector implements BlockSelector {
	private final List<String> pattern;
	private final Direction facingHorizontal;
	private final Direction[] primaryFacing;

	public PatternSelector(List<String> pattern, Direction[] primaryFacing, Direction facing) {
		this.pattern = pattern;
		this.facingHorizontal = facing;
		this.primaryFacing = primaryFacing;
	}

	@NotNull
	@Override
	public Set<BlockPos> select(BlockPos rootPos) {
		Set<BlockPos> blocks = new HashSet<>();
		//iterate through the pattern list, and find all the blocks that match the pattern


		//determine if the pattern should be applied horizontally or vertically based on player facing direction
		boolean vertical = primaryFacing[0] != Direction.DOWN && primaryFacing[0] != Direction.UP;

		//iterate through the pattern and check if the blocks match the pattern
		for (int i = 0; i < pattern.size(); i++) {
			for (int j = 0; j < pattern.get(i).length(); j++) {
				var slice = pattern.get(i);
				var secondaryOffset = slice.length() == 1 ? -1 : -slice.length() / 2;
				//If the pattern matches, add the block to the list of blocks that should be broken
				if (isValidEntry(slice.charAt(j))) {

					//determine the position of the block relative to the root position
					int x = j;
					int y = i;
					int z = 0;

					BlockPos pos = new BlockPos(x, y, z);
					//Center the pattern
					pos = centerOffset().subtract(pos);

					//Flatten the pattern player is facing up or down
					if (!vertical) {
						pos = rotate(pos, 1, Direction.Axis.X);
					}
					//Rotate the block based on the player's facing direction
					pos = rotate(pos, rotationAmount(facingHorizontal), Direction.Axis.Y);

					//Apply absolute position
					pos = rootPos.add(pos);
					blocks.add(pos);
				}
			}
		}
		return blocks;
	}

	private BlockPos centerOffset() {
		int height = pattern.size();
		int width = pattern.get(0).length();

		int x = width / 2;
		int y = height == 2 ? 0 : height / 2;
		int z = 0;
		return new BlockPos(x, y, z);
	}

	private BlockPos rotate(BlockPos pos, int times, Direction.Axis axis) {
		for (int i = 0; i < times; i++) {
			pos = applyRotation(pos, axis);
		}
		return pos;
	}

	private int rotationAmount(Direction direction) {
		int rotation;
		rotation = switch (direction) {
			case EAST -> 1;
			case SOUTH -> 2;
			case WEST -> 3;
			default -> 0;
		};
		return rotation;
	}

	private BlockPos applyRotation(BlockPos pos, Direction.Axis axis) {
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();
		switch (axis) {
			case X -> {
				return new BlockPos(x, z, -y);
			}
			case Y -> {
				return new BlockPos(-z, y, x);
			}
			case Z -> {
				return new BlockPos(y, -x, z);
			}
		}
		return pos;
	}

	private boolean isValidEntry(char c) {
		return c == 'x' || c == 'X' || c == 'c' || c == 'C' || c == ' ';
	}
}
