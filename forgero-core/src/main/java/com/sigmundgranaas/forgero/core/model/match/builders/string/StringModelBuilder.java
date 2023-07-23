package com.sigmundgranaas.forgero.core.model.match.builders.string;

import java.util.Optional;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.core.model.match.builders.ElementParser;
import com.sigmundgranaas.forgero.core.model.match.builders.PredicateBuilder;
import com.sigmundgranaas.forgero.core.model.match.predicate.ModelPredicate;
import com.sigmundgranaas.forgero.core.util.match.Matchable;

/**
 * Attempts to create a Matchable of type ModelPredicate from a JsonElement if the element contains "model:".
 */
public class StringModelBuilder implements PredicateBuilder {
	@Override
	public Optional<Matchable> create(JsonElement element) {
		return ElementParser.fromString(element)
				.filter(string -> string.contains("model:"))
				.map(string -> string.replace("model:", ""))
				.map(ModelPredicate::new);
	}
}
