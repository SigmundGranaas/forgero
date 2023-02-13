package com.sigmundgranaas.forgero.minecraft.common.toolhandler;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

import java.util.List;

public interface BlockBreakingStrategy {
	List<BlockPos> getAvailableBlocks(BlockView world, BlockPos rootPos, PlayerEntity player);
}
