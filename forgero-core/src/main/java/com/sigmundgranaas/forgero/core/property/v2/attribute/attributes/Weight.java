package com.sigmundgranaas.forgero.core.property.v2.attribute.attributes;

import com.sigmundgranaas.forgero.core.property.v2.ComputedAttribute;


public class Weight {

	public static final String KEY = "WEIGHT";

	public static AttributeModification reduceAttackSpeedByWeight() {
		return (attribute, state) -> {
			float weight = ComputedAttribute.apply(state, KEY);
			float newValue = attribute.asFloat() - (weight / 100);
			return ComputedAttribute.of(newValue, attribute.key());
		};
	}
}
