package com.sigmundgranaas.forgero.core.model.match.builders.string;

import java.util.Optional;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.core.model.match.builders.ElementParser;
import com.sigmundgranaas.forgero.core.model.match.builders.PredicateBuilder;
import com.sigmundgranaas.forgero.core.util.match.Matchable;
import com.sigmundgranaas.forgero.core.util.match.NameMatch;

/**
 * Attempts to create a Matchable of type NameMatch from a JsonElement if the element contains "name:".
 */
public class StringNameBuilder implements PredicateBuilder {
	@Override
	public Optional<Matchable> create(JsonElement element) {
		return ElementParser.fromString(element)
				.filter(string -> string.contains("name:"))
				.map(string -> string.replace("name:", ""))
				.map(NameMatch::new);
	}
}
