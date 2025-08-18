package com.sigmundgranaas.forgero.core.attribute.api;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.attribute.api.operator.Operator;
import com.sigmundgranaas.forgero.core.property.api.Property;
import com.sigmundgranaas.forgero.core.condition.api.Condition;
import com.sigmundgranaas.forgero.core.attribute.api.operator.AdditionOperator;

import javax.annotation.Nullable;

import java.util.Optional;

/**
 * An attribute representing a numerical modification.
 */
public record SimpleAttribute(
		Optional<String> id,
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
		this(Optional.empty(), type, value, AdditionOperator.getInstance(), 0, Condition.ALWAYS_TRUE);
	}

	public SimpleAttribute(OpenIdentifier type, float value, Condition condition) {
		this(Optional.empty(), type, value, AdditionOperator.getInstance(), 0, condition);
	}

	public SimpleAttribute(OpenIdentifier type, float value, Operator operator, int group) {
		this(Optional.empty(), type, value, operator, group, Condition.ALWAYS_TRUE);
	}

	public SimpleAttribute(OpenIdentifier type,
						   float value,
						   Operator operator,
						   int group,
						   @Nullable
						   Condition localCondition) {
		this(Optional.empty(), type, value, operator, group, Condition.ALWAYS_TRUE);
	}
}
