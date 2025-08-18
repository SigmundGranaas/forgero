package com.sigmundgranaas.forgero.properties.minecraft.blockbreaking.hardness;

import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.Set;

public class All implements BlockBreakSpeedCalculator {
	public static final All INSTANCE = new All();
	public static final String TYPE = "forgero:all";
	public static final Codec<All> CODEC = Codec.unit(INSTANCE);

	@Override
	public float calculateBlockBreakingDelta(Entity source, BlockPos target, Set<BlockPos> availableBlocks) {
		float totalDelta = 0.0f;
		if (source instanceof PlayerEntity player) {
			for (BlockPos pos : availableBlocks) {
				BlockState state = Utils.getStateFromWorld(source, pos);
				totalDelta += Utils.calculateDelta(state, player, source.getWorld(), pos);
			}
		}
		return (float) (totalDelta / Math.pow(availableBlocks.size(), 2));
	}

	@Override
	public String type() {
		return TYPE;
	}
}
