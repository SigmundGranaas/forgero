package com.sigmundgranaas.forgero.core.attribute.api;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.attribute.api.operator.Operator;
import com.sigmundgranaas.forgero.core.property.api.Property;
import com.sigmundgranaas.forgero.core.condition.api.Condition;
import com.sigmundgranaas.forgero.core.attribute.api.operator.AdditionOperator;

import java.util.Optional;

/**
 * An attribute representing a numerical modification.
 */
public record SimpleAttribute(
		Optional<OpenIdentifier> id,
		OpenIdentifier type,
		float value,
		Operator operator,
		int group,
		Optional<Condition> condition
) implements Property, Attribute {

	public SimpleAttribute(OpenIdentifier type, float value) {
		this(Optional.empty(), type, value, AdditionOperator.getInstance(), 0, Optional.empty());
	}

	public SimpleAttribute(OpenIdentifier type, float value, Condition condition) {
		this(Optional.empty(), type, value, AdditionOperator.getInstance(), 0, Optional.of(condition));
	}

	public SimpleAttribute(OpenIdentifier type, float value, Operator operator, int group) {
		this(Optional.empty(), type, value, operator, group, Optional.empty());
	}

	public SimpleAttribute(OpenIdentifier type,
						   float value,
						   Operator operator,
						   int group,
						   Condition condition) {
		this(Optional.empty(), type, value, operator, group, Optional.ofNullable(condition));
	}
}
