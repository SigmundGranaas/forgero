package com.sigmundgranaas.forgero.core.model.match.builders.string;

import java.util.Optional;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.core.model.match.builders.ElementParser;
import com.sigmundgranaas.forgero.core.model.match.builders.PredicateBuilder;
import com.sigmundgranaas.forgero.core.model.match.predicate.TypePredicate;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.core.util.match.Matchable;

/**
 * Attempts to create a Matchable of type TypePredicate from a JsonElement if the element contains "type:".
 */
public class StringTypeBuilder implements PredicateBuilder {
	@Override
	public Optional<Matchable> create(JsonElement element) {
		return ElementParser.fromString(element)
				.filter(string -> string.contains("type:"))
				.map(string -> string.replace("type:", ""))
				.map(Type::of)
				.map(TypePredicate::new);
	}
}
