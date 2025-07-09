package com.sigmundgranaas.forgero.core.attribute.api;

import java.util.Optional;

import com.sigmundgranaas.forgero.core.attribute.api.operator.AdditionOperator;
import com.sigmundgranaas.forgero.core.attribute.api.operator.Operator;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.property.api.Property;
import com.sigmundgranaas.forgero.core.property.condition.Condition;

/**
 * Represents a single, conditional piece of a larger, internally complex attribute calculation.
 * These components are the building blocks for a {@link CompositeAttribute}.
 *
 * <p><b>Usage and Grouping</b></p>
 * Multiple {@code CompositeAttributeComponent} instances are intended to be grouped together
 * <strong>within the properties of a single parent {@link com.sigmundgranaas.forgero.core.component.api.Component}</strong>.
 * The grouping is determined by the {@link #compositeKey()} and {@link #type()}.
 * For a {@link CompositeAttribute} to be successfully formed from these components, the group
 * must contain at least two different {@link com.sigmundgranaas.forgero.core.attribute.computation.operator.Operator} types.
 *
 * <p><b>Important:</b> If these requirements are not met, the components are typically discarded by the
 * {@link com.sigmundgranaas.forgero.core.attribute.impl.CompositeAttributeBakingStrategy} and will not contribute any value to the final stats.
 */
public record CompositeAttributeComponent(
		OpenIdentifier type,
		float value,
		Operator operator,
		int group,
		OpenIdentifier compositeKey
) implements Property, Attribute, AttributeComponent {

	public Optional<Condition> condition(){
		return Optional.empty();
	}

	public CompositeAttributeComponent(OpenIdentifier type, float value, OpenIdentifier compositeKey) {
		this(type, value, AdditionOperator.getInstance(), 0, compositeKey);
	}

	public Attribute asComputableAttribute() {
		return new SimpleAttribute(type, value, operator, group);
	}
}
