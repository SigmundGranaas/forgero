package com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.hardness;

import java.util.Set;

import com.sigmundgranaas.forgero.core.property.v2.feature.ClassKey;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

@FunctionalInterface
public interface BlockHardnessCalculator {
	ClassKey<BlockHardnessCalculator> KEY = new ClassKey<>("forgero:hardness_calculator", BlockHardnessCalculator.class);

	BlockHardnessCalculator DEFAULT = (Entity source, BlockPos target, Set<BlockPos> selectedBlocks) ->
			source.getWorld().getBlockState(target).getHardness(source.getWorld(), target);

	float calculateBlockHardness(Entity source, BlockPos target, Set<BlockPos> selectedBlocks);
}
