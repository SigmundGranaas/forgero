package com.sigmundgranaas.forgero.core.property.v2.attribute.attributes;

import com.sigmundgranaas.forgero.core.property.v2.Attribute;


public class Weight {

	public static final String KEY = "WEIGHT";

	public static AttributeModification reduceAttackSpeedByWeight() {
		return (attribute, state) -> {
			float weight = Attribute.apply(state, KEY);
			float newValue = attribute.asFloat() - (weight / 100);
			return Attribute.of(newValue, attribute.key());
		};
	}
}
