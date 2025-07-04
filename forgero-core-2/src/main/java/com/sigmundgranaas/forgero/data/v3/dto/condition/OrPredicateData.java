package com.sigmundgranaas.forgero.data.v3.dto.condition;

import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier; // Import OpenIdentifier
import java.util.List;

/**
 * DTO for the "forgero:or" logical predicate.
 * True if at least one contained predicate is true.
 *
 * @param type       The type of predicate, always "forgero:or".
 * @param predicates The list of predicates to logically OR.
 */
public record OrPredicateData(OpenIdentifier type, List<PredicateData> predicates) implements PredicateData { // Changed type from String
}
