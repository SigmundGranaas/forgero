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
 * This class focuses on the hardness of a single block.
 * It calculates the block breaking delta for a single block
 * based on the player's mining speed and the block's hardness,
 * ignoring any additional selected blocks.
 */
public class Single implements BlockBreakSpeedCalculator {
	public final static Single INSTANCE = new Single();

	public final static String TYPE = "forgero:single";

	public final static ClassKey<Single> KEY = new ClassKey<>(TYPE, Single.class);

	public final static JsonBuilder<Single> BUILDER = HandlerBuilder.fromStringOrType(KEY.clazz(), TYPE, INSTANCE);

	@Override
	public float calculateBlockBreakingDelta(Entity source, BlockPos target, Set<BlockPos> selectedBlocks) {
		if (source instanceof PlayerEntity player) {
			BlockState state = Utils.getStateFromWorld(source, target);
			return Utils.calculateDelta(state, player, source.getWorld(), target);
		}
		return 0.0f;
	}
}
