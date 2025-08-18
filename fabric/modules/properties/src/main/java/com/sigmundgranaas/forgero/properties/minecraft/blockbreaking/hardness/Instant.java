package com.sigmundgranaas.forgero.properties.minecraft.blockbreaking.hardness;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

import java.util.Set;

public record Instant(boolean canBreakUnmineable) implements BlockBreakSpeedCalculator {
	public static final String TYPE = "forgero:instant";
	public static final Codec<Instant> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.BOOL.optionalFieldOf("can_break_unmineable", false).forGetter(Instant::canBreakUnmineable)
	).apply(instance, Instant::new));

	@Override
	public float calculateBlockBreakingDelta(Entity source, BlockPos target, Set<BlockPos> availableBlocks) {
		if (canBreakUnmineable) {
			return 1.0f;
		}

		BlockState state = Utils.getStateFromWorld(source, target);
		if (state.getHardness(source.getWorld(), target) >= 0) {
			return 1.0f;
		}

		return 0.0f;
	}

	@Override
	public String type() {
		return TYPE;
	}
}
