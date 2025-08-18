package com.sigmundgranaas.forgero.properties.minecraft.blockbreaking.hardness;

import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.Set;

public record Average() implements BlockBreakSpeedCalculator {
	public static final String TYPE = "forgero:average";
	public static final Average INSTANCE = new Average();
	public static final Codec<Average> CODEC = Codec.unit(INSTANCE);

	@Override
	public float calculateBlockBreakingDelta(Entity source, BlockPos target, Set<BlockPos> availableBlocks) {
		float totalDelta = 0.0f;
		int blockCount = availableBlocks.size();

		if (source instanceof PlayerEntity player) {
			for (BlockPos pos : availableBlocks) {
				BlockState state = Utils.getStateFromWorld(source, pos);
				totalDelta += Utils.calculateDelta(state, player, source.getWorld(), pos);
			}
		}

		return blockCount > 0 ? totalDelta / blockCount : 0.0f;
	}

	@Override
	public String type() {
		return TYPE;
	}
}
