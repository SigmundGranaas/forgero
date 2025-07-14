package com.sigmundgranaas.forgero.core.property.condition;

import java.util.Collections;
import java.util.List;

/**
 * A unified container for all conditional logic related to a property.
 * It separates static and dynamic conditions. For a property to be active,
 * ALL relevant conditions must pass.
 */
public record Condition(
		List<StaticCondition> staticConditions,
		List<DynamicCondition> dynamicConditions
) {
	public static final Condition ALWAYS_TRUE = new Condition(Collections.emptyList(), Collections.emptyList());
}
