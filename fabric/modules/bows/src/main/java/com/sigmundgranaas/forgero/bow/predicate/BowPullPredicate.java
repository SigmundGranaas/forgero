package com.sigmundgranaas.forgero.bow.predicate;

import static com.sigmundgranaas.forgero.minecraft.common.match.MinecraftContextKeys.ENTITY;
import static com.sigmundgranaas.forgero.minecraft.common.match.MinecraftContextKeys.STACK;

import java.util.Optional;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.core.model.match.builders.ElementParser;
import com.sigmundgranaas.forgero.core.model.match.builders.PredicateBuilder;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;

import net.minecraft.entity.LivingEntity;

/**
 * Matches if the bow is pulling and checks if the pull-progress matches the criteria
 */
public record BowPullPredicate(float pullProgress) implements Matchable {
	public static String ID = "forgero:bow_pull";

	@Override
	public boolean test(Matchable match, MatchContext context) {
		var entityOpt = context.get(ENTITY);
		var stackOpt = context.get(STACK);
		if (entityOpt.isPresent() && stackOpt.isPresent() && entityOpt.get() instanceof LivingEntity livingEntity) {
			var stack = stackOpt.get();
			var pull = livingEntity.getActiveItem() != stack ? 0.0F : (float) (stack.getMaxUseTime() - livingEntity.getItemUseTimeLeft()) / 20.0F;
			return pull >= pullProgress;
		}

		return false;
	}

	@Override
	public boolean isDynamic() {
		return true;
	}

	/**
	 * Attempts to create a Matchable of type BowPullPredicateBuilder from a JsonElement if the element is identified as a "BowPullPredicateBuilder".
	 */
	public static class BowPullPredicateBuilder implements PredicateBuilder {
		@Override
		public Optional<Matchable> create(JsonElement element) {
			return ElementParser.fromIdentifiedElement(element, ID).map(jsonObject -> new BowPullPredicate(jsonObject.get("pull").getAsFloat()));
		}
	}
}
