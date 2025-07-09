package com.sigmundgranaas.forgero.data.loading.api.data.condition;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;

/**
 * DTO for predicates that check for the presence of a tag.
 * Used for "forgero:self_has_tag" and "forgero:root_has_tag".
 *
 * @param type The type of predicate.
 * @param tag  The tag to check for.
 */
public record TagMatchPredicateData(OpenIdentifier type, OpenIdentifier tag) implements PredicateData {
}
