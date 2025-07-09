package com.sigmundgranaas.forgero.core.attribute.api;

import com.sigmundgranaas.forgero.core.attribute.api.operator.Operator;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.property.condition.Condition;

import java.util.Optional;

public sealed interface Attribute permits CompositeAttribute, CompositeAttributeComponent, SimpleAttribute {
	OpenIdentifier type();
	float value();
	Operator operator();
	int group();
	Optional<Condition> condition();
}
