package com.sigmundgranaas.forgero.core.attribute.api;

import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.attribute.api.operator.Operator;
import com.sigmundgranaas.forgero.core.property.api.Property;
import com.sigmundgranaas.forgero.core.property.condition.Condition;
import com.sigmundgranaas.forgero.core.attribute.api.operator.AdditionOperator;

import javax.annotation.Nullable;

import java.util.Optional;

/**
 * An attribute representing a numerical modification.
 * It now uses a single, unified Condition object to handle all conditional logic.
 */
public record SimpleAttribute(
		OpenIdentifier type,
		float value,
		Operator operator,
		int group,
		@Nullable
		Condition localCondition
) implements Property, Attribute {

	public Optional<Condition> condition(){
		return Optional.ofNullable(localCondition);
	}

	public SimpleAttribute(OpenIdentifier type, float value) {
		this(type, value, AdditionOperator.getInstance(), 0, Condition.ALWAYS_TRUE);
	}

	public SimpleAttribute(OpenIdentifier type, float value, Condition condition) {
		this(type, value, AdditionOperator.getInstance(), 0, condition);
	}

	public SimpleAttribute(OpenIdentifier type, float value, Operator operator, int group) {
		this(type, value, operator, group, Condition.ALWAYS_TRUE);
	}
}
