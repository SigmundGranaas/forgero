package com.sigmundgranaas.forgeroforge.toolhandler;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

import java.util.List;

public record BlockBreakingHandler(
        BlockBreakingStrategy strategy) implements BlockBreakingStrategy {

    public static boolean isBreakableBlock(BlockView world, BlockPos pos, PlayerEntity player) {
        BlockState state = world.getBlockState(pos);
        if (state.isAir()) {
            return false;
        } else if (state.getHardness(world, pos) < 0) {
            return false;
        } else if (player.canHarvest(state)) {
            return true;
        } else {
            return true;
        }
    }

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

    @Override
    public List<Pair<BlockState, BlockPos>> getAvailableBlocks(BlockView world, BlockPos rootPos, PlayerEntity player) {
        return strategy.getAvailableBlocks(world, rootPos, player);
    }
}
