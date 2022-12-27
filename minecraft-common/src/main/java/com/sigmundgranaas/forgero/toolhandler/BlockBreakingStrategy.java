package com.sigmundgranaas.forgero.toolhandler;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

import java.util.List;

public interface BlockBreakingStrategy {
    List<Pair<BlockState, BlockPos>> getAvailableBlocks(BlockView world, BlockPos rootPos, PlayerEntity player);
}
