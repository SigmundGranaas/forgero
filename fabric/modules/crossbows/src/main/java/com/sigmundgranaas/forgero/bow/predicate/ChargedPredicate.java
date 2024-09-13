package com.sigmundgranaas.forgero.bow.predicate;

import static com.sigmundgranaas.forgero.bow.handler.ChargeCrossbowHandler.isCharged;
import static com.sigmundgranaas.forgero.minecraft.common.match.MinecraftContextKeys.STACK;

import java.util.Optional;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.core.model.match.builders.ElementParser;
import com.sigmundgranaas.forgero.core.model.match.builders.PredicateBuilder;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;

/**
 * Matches if the item is charged.
 */
public record ChargedPredicate(boolean charged) implements Matchable {
	public static String ID = "forgero:charged";

	@Override
	public boolean test(Matchable match, MatchContext context) {
		var stackOpt = context.get(STACK);
		if (stackOpt.isPresent()) {
			var stack = stackOpt.get();
			return isCharged(stack) == charged;
		}
		return false;
	}

	@Override
	public boolean isDynamic() {
		return true;
	}

	/**
	 * Attempts to create a Matchable of type ChargedPredicate from a JsonElement.
	 */
	public static class ChargedPredicateBuilder implements PredicateBuilder {
		@Override
		public Optional<Matchable> create(JsonElement element) {
			return ElementParser.fromIdentifiedElement(element, ID).map(jsonObject -> new ChargedPredicate(jsonObject.get("charged").getAsBoolean()));
		}
	}
}
