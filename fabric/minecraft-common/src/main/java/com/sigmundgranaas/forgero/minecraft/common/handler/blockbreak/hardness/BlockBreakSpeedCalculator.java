package com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.hardness;

import java.util.Set;

import com.sigmundgranaas.forgero.core.property.v2.feature.ClassKey;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

@FunctionalInterface
public interface BlockBreakSpeedCalculator {
	ClassKey<BlockBreakSpeedCalculator> KEY = new ClassKey<>("forgero:speed", BlockBreakSpeedCalculator.class);

	BlockBreakSpeedCalculator DEFAULT = (Entity source, BlockPos target, Set<BlockPos> selectedBlocks) ->
			source.getWorld().getBlockState(target).getHardness(source.getWorld(), target);

	float calculateBlockBreakingDelta(Entity source, BlockPos target, Set<BlockPos> selectedBlocks);
}
