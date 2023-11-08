package com.sigmundgranaas.forgero.minecraft.common.toolhandler.block;

import java.util.Optional;

import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.minecraft.common.feature.BlockBreakFeature;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;


public class BlockHandlerHelper {
	public static Optional<Float> cachedHardness(PropertyContainer container, BlockPos pos, PlayerEntity player) {
		return container.stream()
				.features(BlockBreakFeature.KEY)
				.findFirst()
				.map(feature -> feature.calculateBlockBreakingDelta(player, pos, feature.selectBlocks(player, pos)));
	}
}
