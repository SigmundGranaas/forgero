package com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.hardness;


import java.util.Set;

import com.sigmundgranaas.forgero.core.property.v2.feature.ClassKey;
import com.sigmundgranaas.forgero.core.property.v2.feature.HandlerBuilder;
import com.sigmundgranaas.forgero.core.property.v2.feature.JsonBuilder;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

/**
 * This class calculates the average block breaking delta.
 * It computes the average hardness of a collection of blocks
 * based on the player's mining speed and the hardness of each block.
 */
public class Average implements BlockBreakSpeedCalculator {
	public final static Average INSTANCE = new Average();

	public final static String TYPE = "forgero:average";

	public static ClassKey<Average> KEY = new ClassKey<>(TYPE, Average.class);
	public final static JsonBuilder<Average> BUILDER = HandlerBuilder.fromString(KEY.clazz(), HandlerBuilder.fromStringOptional(TYPE, () -> INSTANCE));

	@Override
	public float calculateBlockBreakingDelta(Entity source, BlockPos target, Set<BlockPos> availableBlocks) {
		float totalDelta = 0.0f;
		int blockCount = availableBlocks.size();

		for (BlockPos pos : availableBlocks) {
			BlockState state = Utils.getStateFromWorld(source, pos);
			totalDelta += Utils.calculateDelta(state, (PlayerEntity) source, source.getWorld(), pos);
		}

		return totalDelta / blockCount;
	}
}
