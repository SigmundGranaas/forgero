package com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.filter;

import static com.sigmundgranaas.forgero.minecraft.common.match.MinecraftContextKeys.BLOCK_TARGET;
import static com.sigmundgranaas.forgero.minecraft.common.match.MinecraftContextKeys.ENTITY;

import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

/**
 * Class for filtering blocks. Air will be ignored.
 */
public class IsBlockFilter implements BlockFilter, Matchable {
	public static final String Key = "forgero:is_block";

	public static final IsBlockFilter DEFAULT = new IsBlockFilter();

	@Override
	public boolean filter(Entity entity, BlockPos currentPos, BlockPos root) {
		if (entity instanceof PlayerEntity player) {
			BlockState state = player.getWorld().getBlockState(currentPos);
			if (state.isAir() || state.getBlock() == Blocks.AIR) {
				return false;
			} else if (state.getBlock() == Blocks.VOID_AIR) {
				return false;
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean test(Matchable match, MatchContext context) {
		var playerOpt = context.get(ENTITY);
		var block = context.get(BLOCK_TARGET);
		return playerOpt.isPresent() && block.isPresent() && filter(playerOpt.get(), block.get(), block.get());
	}
}
