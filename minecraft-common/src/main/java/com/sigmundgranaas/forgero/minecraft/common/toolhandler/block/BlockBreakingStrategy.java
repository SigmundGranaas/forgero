package com.sigmundgranaas.forgero.minecraft.common.toolhandler.block;

import java.util.Set;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public interface BlockBreakingStrategy {
	Set<BlockPos> getAvailableBlocks(BlockView world, BlockPos rootPos, PlayerEntity player);
}
