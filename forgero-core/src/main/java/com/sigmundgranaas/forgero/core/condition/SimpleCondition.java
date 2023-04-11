package com.sigmundgranaas.forgero.core.condition;

import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SimpleCondition implements PropertyContainer {
	protected final List<Property> propertyList;

	public SimpleCondition(List<Property> propertyList) {
		this.propertyList = propertyList;
	}

	@Override
	public @NotNull
	List<Property> getRootProperties() {
		return propertyList;
	}
}
