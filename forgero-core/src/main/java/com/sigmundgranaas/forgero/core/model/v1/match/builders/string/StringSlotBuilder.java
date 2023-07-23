package com.sigmundgranaas.forgero.core.model.v1.match.builders.string;

import java.util.Optional;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.core.model.v1.match.builders.ElementParser;
import com.sigmundgranaas.forgero.core.model.v1.match.builders.PredicateBuilder;
import com.sigmundgranaas.forgero.core.model.v1.match.predicate.SlotPredicate;
import com.sigmundgranaas.forgero.core.util.match.Matchable;

/**
 * Attempts to create a Matchable of type SlotPredicate from a JsonElement if the element contains "slot:".
 */
public class StringSlotBuilder implements PredicateBuilder {
	@Override
	public Optional<Matchable> create(JsonElement element) {
		return ElementParser.fromString(element)
				.filter(string -> string.contains("slot:"))
				.map(string -> string.replace("slot:", ""))
				.map(Integer::valueOf)
				.map(SlotPredicate::new);
	}
}
