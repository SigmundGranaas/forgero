package com.sigmundgranaas.forgero.core.condition;

import java.util.List;

import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;
import org.jetbrains.annotations.NotNull;

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

	@Override
	public @NotNull List<Property> getRootProperties(Matchable target, MatchContext context) {
		return propertyList;
	}
}
