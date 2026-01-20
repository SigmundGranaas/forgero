package com.sigmundgranaas.forgero.core.property.v2.attribute.attributes;

import java.util.function.BiFunction;

import com.sigmundgranaas.forgero.core.property.CalculationOrder;
import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.v2.ComputedAttribute;

public interface AttributeModification extends BiFunction<ComputedAttribute, PropertyContainer, ComputedAttribute> {
	default CalculationOrder order() {
		return CalculationOrder.BASE;
	}
}
