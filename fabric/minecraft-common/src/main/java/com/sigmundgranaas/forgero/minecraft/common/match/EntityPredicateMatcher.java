package com.sigmundgranaas.forgero.minecraft.common.match;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.core.model.match.builders.ElementParser;
import com.sigmundgranaas.forgero.core.model.match.builders.PredicateBuilder;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Optional;

import static com.sigmundgranaas.forgero.minecraft.common.match.MinecraftContextKeys.ENTITY;

public record EntityPredicateMatcher(EntityPredicate predicate) implements Matchable {
	public static String ID = "minecraft:entity_properties";

	@Override
	public boolean test(Matchable match, MatchContext context) {
		var entity = context.get(ENTITY);
		if (entity.isPresent() && entity.get() instanceof ServerPlayerEntity player) {
			return predicate.test(player, player);
		}
		return false;
	}

	public static class EntityPredicateBuilder implements PredicateBuilder {
		@Override
		public Optional<Matchable> create(JsonElement element) {
			return ElementParser.fromIdentifiedElement(element, ID)
					.map(EntityPredicate::fromJson)
					.map(EntityPredicateMatcher::new);
		}
	}
}
