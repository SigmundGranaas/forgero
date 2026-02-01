package com.sigmundgranaas.forgero.core.attribute.api;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.attribute.api.operator.Operator;
import com.sigmundgranaas.forgero.core.property.api.Property;
import com.sigmundgranaas.forgero.core.condition.api.Condition;
import com.sigmundgranaas.forgero.core.attribute.api.operator.AdditionOperator;

import java.util.Optional;

/**
 * An attribute representing a numerical modification.
 *
 * @param id        Optional unique identifier for this attribute instance
 * @param type      The attribute type (e.g., forgero:durability, forgero:mining_speed)
 * @param value     The numerical value
 * @param operator  How this value combines with others (add, multiply, etc.)
 * @param group     Ordering group for computation
 * @param scope     The composition scope (part-composite, upgrade, etc.), or empty for default
 * @param condition Runtime conditions for when this attribute applies
 */
public record SimpleAttribute(
		Optional<OpenIdentifier> id,
		OpenIdentifier type,
		float value,
		Operator operator,
		int group,
		Optional<OpenIdentifier> scope,
		Optional<Condition> condition
) implements Property, Attribute {

	/**
	 * Creates a simple attribute with just type and value.
	 * Uses addition operator, group 0, no scope, no condition.
	 */
	public SimpleAttribute(OpenIdentifier type, float value) {
		this(Optional.empty(), type, value, AdditionOperator.getInstance(), 0, Optional.empty(), Optional.empty());
	}

	/**
	 * Creates an attribute with a condition.
	 */
	public SimpleAttribute(OpenIdentifier type, float value, Condition condition) {
		this(Optional.empty(), type, value, AdditionOperator.getInstance(), 0, Optional.empty(), Optional.of(condition));
	}

	/**
	 * Creates an attribute with operator and group.
	 */
	public SimpleAttribute(OpenIdentifier type, float value, Operator operator, int group) {
		this(Optional.empty(), type, value, operator, group, Optional.empty(), Optional.empty());
	}

	/**
	 * Creates an attribute with operator, group, and condition.
	 */
	public SimpleAttribute(OpenIdentifier type,
						   float value,
						   Operator operator,
						   int group,
						   Condition condition) {
		this(Optional.empty(), type, value, operator, group, Optional.empty(), Optional.ofNullable(condition));
	}

	/**
	 * Creates a resolved attribute (after composition) with just type and value.
	 * These are "output" attributes with no scope needed.
	 */
	public static SimpleAttribute resolved(OpenIdentifier type, float value) {
		return new SimpleAttribute(type, value);
	}

	/**
	 * Creates an attribute with a specific scope for composition.
	 */
	public static SimpleAttribute withScope(OpenIdentifier type, float value, Operator operator, OpenIdentifier scope) {
		return new SimpleAttribute(Optional.empty(), type, value, operator, 0, Optional.of(scope), Optional.empty());
	}

	/**
	 * Returns a copy of this attribute with a different scope.
	 */
	public SimpleAttribute withScope(OpenIdentifier newScope) {
		return new SimpleAttribute(id, type, value, operator, group, Optional.ofNullable(newScope), condition);
	}

	/**
	 * Returns a copy of this attribute with no scope (default behavior).
	 */
	public SimpleAttribute withoutScope() {
		return new SimpleAttribute(id, type, value, operator, group, Optional.empty(), condition);
	}
}
