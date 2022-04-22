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

public record PatternBlockBreakingHandler(PatternBreaking breakingPattern) {
    public float getHardness(BlockState rootState, BlockPos rootPos, BlockView world, PlayerEntity player) {
        float hardness = rootState.getHardness(world, rootPos);
        if (hardness == -1.0f) {
            return hardness;
        }
        float breakingSpeed = 0.0f;
        hardness = 0.0f;
        var availableBlocks = getAvailableBlocks(world, rootPos, player);
        for (Pair<BlockState, BlockPos> state : availableBlocks) {
            // I don't know which parameters should be here
            float harvestable = player.canHarvest(state.getLeft()) ? 30 : 100;
            hardness += state.getLeft().getHardness(world, state.getRight()) * harvestable;
            breakingSpeed += player.getBlockBreakingSpeed(state.getLeft());
        }
        breakingSpeed = breakingSpeed / availableBlocks.size();
        return breakingSpeed / hardness;
    }

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
