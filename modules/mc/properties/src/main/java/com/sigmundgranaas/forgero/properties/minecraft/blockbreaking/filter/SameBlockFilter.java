package com.sigmundgranaas.forgero.properties.minecraft.blockbreaking.filter;

import com.mojang.serialization.Codec;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

public record SameBlockFilter() implements BlockFilter {
	public static final String TYPE = "forgero:same_block";
	public static final Codec<SameBlockFilter> CODEC = Codec.unit(new SameBlockFilter());

	@Override
	public boolean filter(Entity entity, BlockPos currentPos, BlockPos root) {
		return entity.getWorld().getBlockState(currentPos).getBlock().equals(entity.getWorld().getBlockState(root).getBlock());
	}

	@Override
	public String type() {
		return TYPE;
	}
}
