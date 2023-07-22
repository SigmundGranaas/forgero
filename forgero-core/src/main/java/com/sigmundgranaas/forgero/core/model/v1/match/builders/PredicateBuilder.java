package com.sigmundgranaas.forgero.core.model.v1.match.builders;

import java.util.Optional;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.core.util.match.Matchable;

@FunctionalInterface
public interface PredicateBuilder {

	Optional<Matchable> create(JsonElement element);
}
