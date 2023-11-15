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
 * This class calculates the total block breaking delta of all blocks in the selection.
 * It accumulates the hardness of each block in the selection based on the player's mining speed,
 * essentially treating the multiple blocks as one combined block.
 */
public class All implements BlockBreakSpeedCalculator {

	public final static All INSTANCE = new All();

	public final static String TYPE = "forgero:all";

	public final static ClassKey<All> KEY = new ClassKey<>(TYPE, All.class);

	public final static JsonBuilder<All> BUILDER = HandlerBuilder.fromString(KEY.clazz(), HandlerBuilder.fromStringOptional(TYPE, () -> INSTANCE));

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
}
