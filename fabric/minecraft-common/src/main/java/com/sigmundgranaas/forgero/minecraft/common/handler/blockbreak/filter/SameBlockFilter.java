package com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.filter;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

public class SameBlockFilter implements BlockFilter {
	public static final SameBlockFilter DEFAULT = new SameBlockFilter();

	public static final String Key = "forgero:same_block";

	@Override
	public boolean filter(Entity entity, BlockPos currentPos, BlockPos root) {
		return entity.getWorld().getBlockState(currentPos).getBlock().equals(entity.getWorld().getBlockState(root).getBlock());
	}
}
