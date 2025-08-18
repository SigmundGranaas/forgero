package com.sigmundgranaas.forgero.properties.minecraft.blockbreaking.hardness;

import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.utility.codec.DispatchCodecUtils;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

import java.util.Set;

public interface BlockBreakSpeedCalculator {
	float calculateBlockBreakingDelta(Entity source, BlockPos target, Set<BlockPos> selectedBlocks);

	String type();

	static Codec<? extends BlockBreakSpeedCalculator> getCodec(String type) {
		return switch (type) {
			case All.TYPE -> All.CODEC;
			case Average.TYPE -> Average.CODEC;
			case Instant.TYPE -> Instant.CODEC;
			case Single.TYPE -> Single.CODEC;
			default -> throw new IllegalArgumentException("Unknown BlockBreakSpeedCalculator type: " + type);
		};
	}

	Codec<BlockBreakSpeedCalculator> CODEC = DispatchCodecUtils.create(
			BlockBreakSpeedCalculator::getCodec,
			BlockBreakSpeedCalculator::type
	);
}
