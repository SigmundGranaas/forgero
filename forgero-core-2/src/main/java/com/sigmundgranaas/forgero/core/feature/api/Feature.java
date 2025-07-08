package com.sigmundgranaas.forgero.core.feature.api;

import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.property.api.Property;
import com.sigmundgranaas.forgero.core.property.condition.Condition;

/**
 * Represents a functional capability or special effect of a component.
 * It now includes a Condition object to allow for both static and dynamic activation.
 *
 * @param type      The unique identifier for this specific feature instance or type.
 * @param level     A numerical group for the feature, which can be used for scaling effects.
 * @param condition A container for all static and dynamic conditions for this feature.
 */
public record Feature(
		OpenIdentifier type,
		int level,
		Condition condition
) implements Property {

	public Feature(OpenIdentifier type) {
		this(type, 1, Condition.ALWAYS_TRUE);
	}

	public Feature(OpenIdentifier type, Condition condition) {
		this(type, 1, condition);
	}
}
