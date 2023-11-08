package com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.filter;

import static com.sigmundgranaas.forgero.minecraft.common.match.MinecraftContextKeys.BLOCK_TARGET;
import static com.sigmundgranaas.forgero.minecraft.common.match.MinecraftContextKeys.ENTITY;

import java.util.Optional;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.core.model.match.builders.ElementParser;
import com.sigmundgranaas.forgero.core.model.match.builders.PredicateBuilder;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

/**
 * Class for filtering blocks based on if the player can harvest the block
 */
public class CanMineFilter implements BlockFilter, Matchable {
	public static final String Key = "forgero:can_mine";

	public static final CanMineFilter DEFAULT = new CanMineFilter();

	@Override
	public boolean filter(Entity entity, BlockPos currentPos, BlockPos root) {
		if (entity instanceof PlayerEntity player) {
			BlockState state = player.getWorld().getBlockState(currentPos);
			return player.canHarvest(state);
		}
		return false;
	}

	@Override
	public boolean test(Matchable match, MatchContext context) {
		var playerOpt = context.get(ENTITY);
		var block = context.get(BLOCK_TARGET);
		return playerOpt.isPresent() && block.isPresent() && filter(playerOpt.get(), block.get(), block.get());
	}

	public static class CanMineFilterBuilder implements PredicateBuilder {
		@Override
		public Optional<Matchable> create(JsonElement element) {
			return ElementParser.fromIdentifiedElement(element, Key)
					.map(json -> new CanMineFilter());
		}
	}
}
