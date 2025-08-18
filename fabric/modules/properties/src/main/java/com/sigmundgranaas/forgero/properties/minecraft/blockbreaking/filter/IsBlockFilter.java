package com.sigmundgranaas.forgero.properties.minecraft.blockbreaking.filter;

import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

public record IsBlockFilter() implements BlockFilter {
	public static final String TYPE = "forgero:is_block";
	public static final Codec<IsBlockFilter> CODEC = Codec.unit(new IsBlockFilter());

	@Override
	public boolean filter(Entity entity, BlockPos currentPos, BlockPos root) {
		if (entity instanceof PlayerEntity player) {
			BlockState state = player.getWorld().getBlockState(currentPos);
			return !state.isAir() && state.getBlock() != Blocks.VOID_AIR;
		}
		return false;
	}

	@Override
	public String type() {
		return TYPE;
	}
}
