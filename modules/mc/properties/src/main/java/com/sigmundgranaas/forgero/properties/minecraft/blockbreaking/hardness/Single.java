package com.sigmundgranaas.forgero.properties.minecraft.blockbreaking.hardness;

import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.Set;

public record Single() implements BlockBreakSpeedCalculator {
	public static final String TYPE = "forgero:single";
	public static final Single INSTANCE = new Single();
	public static final Codec<Single> CODEC = Codec.unit(INSTANCE);

	@Override
	public float calculateBlockBreakingDelta(Entity source, BlockPos target, Set<BlockPos> selectedBlocks) {
		if (source instanceof PlayerEntity player) {
			BlockState state = Utils.getStateFromWorld(source, target);
			return Utils.calculateDelta(state, player, source.getWorld(), target);
		}
		return 0.0f;
	}

	@Override
	public String type() {
		return TYPE;
	}
}
