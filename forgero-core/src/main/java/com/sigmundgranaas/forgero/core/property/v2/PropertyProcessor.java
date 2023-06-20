package com.sigmundgranaas.forgero.core.property.v2;

import java.util.List;

import com.sigmundgranaas.forgero.core.property.Property;

public interface PropertyProcessor {
	List<Property> process(List<Property> propertyList);
}
