package com.sigmundgranaas.forgero.minecraft.common.match.predicate;

import java.util.Optional;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.core.model.match.builders.ElementParser;
import com.sigmundgranaas.forgero.core.model.match.builders.PredicateBuilder;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;

public record RandomPredicate(float value) implements Matchable {
	public static String ID = "forgero:random";

	@Override
	public boolean test(Matchable match, MatchContext context) {
		return Math.random() < value;
	}

	@Override
	public boolean isDynamic() {
		return true;
	}

	/**
	 * Attempts to create a Matchable of type RandomPredicate from a JsonElement.
	 */
	public static class RandomPredicatePredicateBuilder implements PredicateBuilder {
		@Override
		public Optional<Matchable> create(JsonElement element) {
			return ElementParser.fromIdentifiedElement(element, ID)
					.map(jsonObject -> new RandomPredicate(jsonObject.get("value").getAsFloat()));
		}
	}
}
