package com.sigmundgranaas.forgero.toolhandler;

import com.sigmundgranaas.forgero.core.property.active.PatternBreaking;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PatternBreakingStrategy implements BlockBreakingStrategy {
    private final PatternBreaking breakingPattern;

    public PatternBreakingStrategy(PatternBreaking breakingPattern) {
        this.breakingPattern = breakingPattern;
    }

    @Override
    public List<Pair<BlockState, BlockPos>> getAvailableBlocks(BlockView world, BlockPos rootPos, PlayerEntity player) {
        Direction dir = Direction.getEntityFacingOrder(player)[0];
        var list = new ArrayList<Pair<BlockState, BlockPos>>();
        if (breakingPattern.pattern().length == 0 || breakingPattern.pattern().length % 2 == 0 || breakingPattern.pattern()[0].length() % 2 == 0) {
            return Collections.emptyList();
        }
        int centerY = (breakingPattern.pattern().length - 1) / 2;
        int centerX = (breakingPattern.pattern()[0].length() - 1) / 2;
        if (breakingPattern.pattern().length == 1) {
            centerY = 0;
        }
        if (breakingPattern.pattern()[0].length() == 1) {
            centerX = 0;
        }

        for (int i = 0; i < breakingPattern.pattern().length; i++) {
            for (int j = 0; j < breakingPattern.pattern()[i].length(); j++) {
                if (breakingPattern.pattern()[i].charAt(j) == 'x' || breakingPattern.pattern()[i].charAt(j) == 'X') {
                    BlockPos newPos;
                    if (dir == Direction.EAST || dir == Direction.WEST) {
                        newPos = new BlockPos(rootPos.getX(), rootPos.getY() + i - centerY, rootPos.getZ() + j - centerX);

                    } else if (dir == Direction.NORTH || dir == Direction.SOUTH) {
                        newPos = new BlockPos(rootPos.getX() + j - centerX, rootPos.getY() + i - centerY, rootPos.getZ());

                    } else {
                        newPos = new BlockPos(rootPos.getX() + j - centerX, rootPos.getY(), rootPos.getZ() + i - centerY);

                    }
                    BlockState newState = world.getBlockState(newPos);
                    if (!newState.isAir() && player.canHarvest(newState)) {
                        list.add(new Pair<>(newState, newPos));
                    }
                }
            }
        }
        return list;
    }
}
