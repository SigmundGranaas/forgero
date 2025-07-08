package com.sigmundgranaas.forgero.data.dto.condition;

import java.util.List;

/**
 * A DTO for condition blocks. It holds a list of predicates that must all be met
 * for the condition to be considered active.
 *
 * @param predicates A list of predicates. JSON files can represent this as a single object
 *                   or an array of objects.
 */
public record ConditionData(List<PredicateData> predicates) {
}
