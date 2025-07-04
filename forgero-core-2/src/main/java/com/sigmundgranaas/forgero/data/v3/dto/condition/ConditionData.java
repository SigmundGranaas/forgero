package com.sigmundgranaas.forgero.data.v3.dto.condition;

import java.util.Collections;
import java.util.List;

/**
 * A DTO for condition blocks. It holds a list of predicates that must all be met
 * for the condition to be considered active.
 *
 * @param predicates A list of predicates. JSON files can represent this as a single object
 *                   or an array of objects.
 */
public record ConditionData(List<PredicateData> predicates) {
	public static final ConditionData EMPTY = new ConditionData(Collections.emptyList());
}
