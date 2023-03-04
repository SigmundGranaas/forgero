package com.sigmundgranaas.forgero.minecraft.common.toolhandler;

import com.sigmundgranaas.forgero.core.property.active.BreakingDirection;
import com.sigmundgranaas.forgero.core.property.v2.feature.PropertyData;
import com.sigmundgranaas.forgero.minecraft.common.property.handler.PatternBreaking;
import com.sigmundgranaas.forgero.minecraft.common.property.handler.TaggedPatternBreaking;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

import java.util.HashSet;
import java.util.Set;

import static com.sigmundgranaas.forgero.minecraft.common.toolhandler.BlockBreakingHandler.isBreakableBlock;

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
		Direction[] directions = Direction.getEntityFacingOrder(player);
		var availableBlocks = new HashSet<BlockPos>();

		if (breakingPattern.getPattern().length == 0 || breakingPattern.getPattern()[0].length() % 2 == 0) {
			return new HashSet<>();
		}

		int centerY = (breakingPattern.getPattern().length - 1) / 2;
		if (breakingPattern.getPattern().length == 2) {
			centerY = 1;
		}
		if (breakingPattern.getPattern().length == 1) {
			centerY = 0;
		}

		int centerX = (breakingPattern.getPattern()[0].length() - 1) / 2;
		if (breakingPattern.getPattern()[0].length() == 1) {
			centerX = 0;
		}

		for (int i = 0; i < breakingPattern.getPattern().length; i++) {
			for (int j = 0; j < breakingPattern.getPattern()[i].length(); j++) {
				if (breakingPattern.getPattern()[i].charAt(j) == 'x' || breakingPattern.getPattern()[i].charAt(j) == 'X') {
					BlockPos newPos;

					if (breakingPattern.getDirection() == BreakingDirection.ANY) {
						Direction primary = directions[0];

						if (primary == Direction.EAST || primary == Direction.WEST) {
							newPos = new BlockPos(rootPos.getX(), rootPos.getY() + i - centerY, rootPos.getZ() + j - centerX);
						} else if (primary == Direction.NORTH || primary == Direction.SOUTH) {
							newPos = new BlockPos(rootPos.getX() + j - centerX, rootPos.getY() + i - centerY, rootPos.getZ());
						} else {
							Direction secondary = directions[1];

							if (secondary == Direction.EAST || secondary == Direction.WEST) {
								newPos = new BlockPos(rootPos.getX() + j - centerX, rootPos.getY(), rootPos.getZ());
							} else if (secondary == Direction.NORTH || secondary == Direction.SOUTH) {
								newPos = new BlockPos(rootPos.getX(), rootPos.getY(), rootPos.getZ() + i - centerY);
							} else {
								newPos = new BlockPos(rootPos.getX() + j - centerX, rootPos.getY(), rootPos.getZ() + i - centerY);
							}
						}
					} else {
						newPos = new BlockPos(rootPos.getX() + j - centerX, rootPos.getY(), rootPos.getZ() + j - centerX);
					}

					BlockState newState = world.getBlockState(newPos);
					if (isBreakableBlock(world, newPos, player) && (breakingPattern.checkBlock(newState) || newPos.equals(rootPos))) {
						availableBlocks.add(newPos);
					}
				}
			}
		}

		return availableBlocks;
	}
}
