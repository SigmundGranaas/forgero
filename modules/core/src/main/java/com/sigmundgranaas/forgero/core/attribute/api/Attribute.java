package com.sigmundgranaas.forgero.core.attribute.api;

import com.sigmundgranaas.forgero.core.attribute.api.operator.Operator;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.property.api.PropertyKey;
import com.sigmundgranaas.forgero.core.condition.api.Condition;

import java.util.Optional;

public sealed interface Attribute permits SimpleAttribute {
	PropertyKey<Attribute> KEY = new PropertyKey<>(Attribute.class, "forgero:attributes");

	Optional<String> id();
	OpenIdentifier type();
	float value();
	Operator operator();
	int group();
	Optional<Condition> condition();
}
