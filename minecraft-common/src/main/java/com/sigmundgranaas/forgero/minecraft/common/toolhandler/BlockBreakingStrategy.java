package com.sigmundgranaas.forgero.minecraft.common.toolhandler;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

import java.util.Set;

public interface BlockBreakingStrategy {
	Set<BlockPos> getAvailableBlocks(BlockView world, BlockPos rootPos, PlayerEntity player);
}
