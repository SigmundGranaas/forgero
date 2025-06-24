package com.sigmundgranaas.forgero.core.attribute.api;

import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.attribute.computation.operator.Operator;
import com.sigmundgranaas.forgero.core.property.api.Property;
import com.sigmundgranaas.forgero.core.property.condition.Condition;
import com.sigmundgranaas.forgero.core.attribute.computation.operator.AdditionOperator;

/**
 * An attribute representing a numerical modification.
 * It now uses a single, unified Condition object to handle all conditional logic.
 */
public record Attribute(
		OpenIdentifier type,
		float value,
		Operator operator,
		int level,
		Condition condition
) implements Property {

	// Convenience constructors for simpler attribute definitions
	public Attribute(OpenIdentifier type, float value) {
		this(type, value, AdditionOperator.getInstance(), 0, Condition.ALWAYS_TRUE);
	}

	public Attribute(OpenIdentifier type, float value, Condition condition) {
		this(type, value, AdditionOperator.getInstance(), 0, condition);
	}
}
