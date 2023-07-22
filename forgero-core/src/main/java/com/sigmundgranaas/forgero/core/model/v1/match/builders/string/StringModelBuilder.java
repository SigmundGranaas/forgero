package com.sigmundgranaas.forgero.core.model.v1.match.builders.string;

import java.util.Optional;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.core.model.v1.match.builders.ElementParser;
import com.sigmundgranaas.forgero.core.model.v1.match.builders.PredicateBuilder;
import com.sigmundgranaas.forgero.core.model.v1.match.predicate.ModelPredicate;
import com.sigmundgranaas.forgero.core.util.match.Matchable;

public class StringModelBuilder implements PredicateBuilder {
	@Override
	public Optional<Matchable> create(JsonElement element) {
		return ElementParser.fromString(element)
				.filter(string -> string.contains("model:"))
				.map(string -> string.replace("model:", ""))
				.map(ModelPredicate::new);
	}
}
