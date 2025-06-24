package com.sigmundgranaas.forgero.core.property.condition;

import java.util.Collections;
import java.util.List;

/**
 * A unified container for all conditional logic related to a property.
 * It separates static conditions (evaluated at bake time against the component's structure)
 * from dynamic conditions (evaluated at apply time against the game world context).
 *
 * For a property to be active, ALL relevant conditions (static then dynamic) must pass.
 */
public record Condition(
		List<StaticCondition> staticConditions,
		List<DynamicCondition> dynamicConditions
) {
	/**
	 * A default, always-true condition for properties that should always be active.
	 */
	public static final Condition ALWAYS_TRUE = new Condition(Collections.emptyList(), Collections.emptyList());
}
