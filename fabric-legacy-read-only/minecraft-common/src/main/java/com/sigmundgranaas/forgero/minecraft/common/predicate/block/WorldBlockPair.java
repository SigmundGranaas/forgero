package com.sigmundgranaas.forgero.minecraft.common.predicate.block;

import static com.sigmundgranaas.forgero.minecraft.common.match.MinecraftContextKeys.BLOCK_TARGET;
import static com.sigmundgranaas.forgero.minecraft.common.match.MinecraftContextKeys.WORLD;

import java.util.Optional;

import com.sigmundgranaas.forgero.core.util.match.MatchContext;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public record WorldBlockPair(WorldView world, BlockPos pos) {
	public static Optional<WorldBlockPair> of(MatchContext context) {
		Optional<World> world = context.get(WORLD);
		Optional<BlockPos> block = context.get(BLOCK_TARGET);

		if (world.isPresent() && block.isPresent()) {
			return Optional.of(new WorldBlockPair(world.get(), block.get()));
		}
		return Optional.empty();
	}

	public BlockState state() {
		return world.getBlockState(pos());
	}

	public Block block() {
		return state().getBlock();
	}
}
