package com.sigmundgranaas.forgero.minecraft.common.toolhandler.block;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.sigmundgranaas.forgero.core.property.active.BreakingDirection;
import com.sigmundgranaas.forgero.core.property.v2.feature.PropertyData;
import com.sigmundgranaas.forgero.minecraft.common.property.handler.PatternBreaking;
import com.sigmundgranaas.forgero.minecraft.common.property.handler.TaggedPatternBreaking;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

public class PatternBreakingStrategy implements BlockBreakingStrategy {
	private final PatternBreaking breakingPattern;

	public PatternBreakingStrategy(PatternBreaking breakingPattern) {
		this.breakingPattern = breakingPattern;
	}

	public PatternBreakingStrategy(PropertyData data) {
		BreakingDirection dir = data.getDescription() == null ? BreakingDirection.ANY : data.getDirection();
		if (data.getTags().size() > 0) {
			this.breakingPattern = new TaggedPatternBreaking(data.getPattern(), dir, data.getTags().get(0));
		} else {
			this.breakingPattern = new PatternBreaking(data.getPattern(), dir);
		}
	}

	@Override
	public Set<BlockPos> getAvailableBlocks(BlockView world, BlockPos rootPos, PlayerEntity player) {
		return findBlocks(world, player, rootPos, List.of(breakingPattern.getPattern()));
	}

	private Set<BlockPos> findBlocks(BlockView world, PlayerEntity player, BlockPos rootPos, List<String> pattern) {
		Direction facing = player.getHorizontalFacing();
		Direction[] primaryFacing = Direction.getEntityFacingOrder(player);
		Set<BlockPos> blocks = new HashSet<>();
		//iterate through the pattern list, and find all the blocks that match the pattern
		var primaryOffset = pattern.size() == 1 ? 1 : -pattern.size() / 2;

		//determine how the pattern should be rotated based on player facing direction
		int rotate = 0;

		//determine if the pattern should be applied horizontally or vertically based on player facing direction
		boolean vertical = primaryFacing[0] != Direction.DOWN && primaryFacing[0] != Direction.UP;

		if (vertical) {
			switch (facing) {
				case EAST -> rotate = 3;
				case SOUTH -> rotate = 2;
				case WEST -> rotate = 1;
				case NORTH -> rotate = 0;
			}
		} else {
			switch (facing) {
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
				var secondaryOffset = slice.length() == 1 ? 1 : -slice.length() / 2;
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
