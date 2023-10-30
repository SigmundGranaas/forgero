package com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak;

import java.util.Set;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

public interface BlockBreakHandler {

	Set<BlockPos> selectBlocks(Entity source, BlockPos target);

	float calculateBlockBreakingDelta(Entity source, BlockPos target, Set<BlockPos> selectedBlocks);

	void onUsed(Entity source, BlockPos target, Set<BlockPos> selectedBlocks);
}
