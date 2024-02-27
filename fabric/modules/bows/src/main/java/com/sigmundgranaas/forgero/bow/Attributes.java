package com.sigmundgranaas.forgero.bow;

import com.sigmundgranaas.forgero.core.property.v2.ComputedAttribute;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.AttributeModification;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.Weight;

public class Attributes {
	public static final String DRAW_SPEED = "forgero:draw_speed";
	public static final String DRAW_POWER = "forgero:draw_power";
	public static final String ACCURACY = "forgero:accuracy";

	public static AttributeModification reduceByWeight = (attribute, state) -> {
		float weight = ComputedAttribute.apply(state, Weight.KEY);
		float newValue = attribute.asFloat() - (weight / 100);
		return ComputedAttribute.of(newValue, attribute.key());
	};
	
}
