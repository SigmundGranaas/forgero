package com.sigmundgranaas.forgero.data.v3.dto.condition;

import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier;

/**
 * DTO for the "forgero:not" logical predicate.
 * True if the contained predicate is false.
 *
 * @param type      The type of predicate, always "forgero:not".
 * @param predicate The predicate to logically NOT.
 */
public record NotPredicateData(OpenIdentifier type, PredicateData predicate) implements PredicateData {
}
