package com.sigmundgranaas.forgero.core.property.v2.attribute.attributes;

import java.util.function.BiFunction;

import com.sigmundgranaas.forgero.core.property.CalculationOrder;
import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.v2.Attribute;

public interface AttributeModification extends BiFunction<Attribute, PropertyContainer, Attribute> {
	default CalculationOrder order() {
		return CalculationOrder.BASE;
	}
}
