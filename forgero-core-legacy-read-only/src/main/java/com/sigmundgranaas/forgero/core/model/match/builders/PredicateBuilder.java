package com.sigmundgranaas.forgero.core.model.match.builders;

import java.util.Optional;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.core.util.match.Matchable;


/**
 * This interface is used to create Matchable objects from a JsonElement.
 * Implementations of this interface will define how the Matchable object is created.
 */
@FunctionalInterface
public interface PredicateBuilder {

	/**
	 * Create a Matchable object from a JsonElement.
	 *
	 * @param element JsonElement to parse into a Matchable object.
	 * @return An Optional containing a Matchable object if the JsonElement could be parsed, otherwise an empty Optional.
	 */
	Optional<Matchable> create(JsonElement element);
}
