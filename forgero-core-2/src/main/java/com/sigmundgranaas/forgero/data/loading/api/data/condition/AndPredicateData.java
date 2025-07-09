package com.sigmundgranaas.forgero.data.loading.api.data.condition;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import java.util.List;

/**
 * DTO for the "forgero:and" logical predicate.
 * True if all contained predicates are true.
 *
 * @param type       The type of predicate, always "forgero:and".
 * @param predicates The list of predicates to logically AND.
 */
public record AndPredicateData(OpenIdentifier type, List<PredicateData> predicates) implements PredicateData {
}
