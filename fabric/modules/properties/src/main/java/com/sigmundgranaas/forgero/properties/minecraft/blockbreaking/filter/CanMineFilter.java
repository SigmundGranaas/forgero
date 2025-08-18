package com.sigmundgranaas.forgero.properties.minecraft.blockbreaking.filter;

import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

public record CanMineFilter() implements BlockFilter {
	public static final String TYPE = "forgero:can_mine";
	public static final Codec<CanMineFilter> CODEC = Codec.unit(new CanMineFilter());

	@Override
	public boolean filter(Entity entity, BlockPos currentPos, BlockPos root) {
		if (entity instanceof PlayerEntity player) {
			BlockState state = player.getWorld().getBlockState(currentPos);
			if (state.isAir()) {
				return false;
			}
			return player.canHarvest(state);
		}
		return false;
	}

	@Override
	public String type() {
		return TYPE;
	}
}
