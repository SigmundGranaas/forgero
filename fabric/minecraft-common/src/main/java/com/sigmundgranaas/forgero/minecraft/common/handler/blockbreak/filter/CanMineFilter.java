package com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.filter;

import static com.sigmundgranaas.forgero.minecraft.common.match.MinecraftContextKeys.BLOCK_TARGET;
import static com.sigmundgranaas.forgero.minecraft.common.match.MinecraftContextKeys.ENTITY;

import java.util.Optional;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.core.model.match.builders.ElementParser;
import com.sigmundgranaas.forgero.core.model.match.builders.PredicateBuilder;
import com.sigmundgranaas.forgero.core.property.v2.feature.ClassKey;
import com.sigmundgranaas.forgero.core.property.v2.feature.HandlerBuilder;
import com.sigmundgranaas.forgero.core.property.v2.feature.JsonBuilder;
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
	public static final String TYPE = "forgero:can_mine";
	public static final CanMineFilter DEFAULT = new CanMineFilter();

	public final static ClassKey<CanMineFilter> KEY = new ClassKey<>(TYPE, CanMineFilter.class);

	public final static JsonBuilder<CanMineFilter> BUILDER = HandlerBuilder.fromStringOrType(KEY.clazz(), TYPE, DEFAULT);

	@Override
	public boolean filter(Entity entity, BlockPos currentPos, BlockPos root) {
		if (entity instanceof PlayerEntity player) {
			BlockState state = player.getWorld().getBlockState(currentPos);
			if (state.isAir()) {
				return false;
			}
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
			return ElementParser.fromIdentifiedElement(element, TYPE)
					.map(json -> new CanMineFilter());
		}
	}
}
