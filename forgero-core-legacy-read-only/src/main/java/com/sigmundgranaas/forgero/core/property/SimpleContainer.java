package com.sigmundgranaas.forgero.core.property;

import java.util.List;

import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;
import org.jetbrains.annotations.NotNull;

public class SimpleContainer implements PropertyContainer {
	private final List<Property> propertyList;

	public SimpleContainer(List<Property> propertyList) {
		this.propertyList = propertyList;
	}

	@Override
	public @NotNull
	List<Property> getRootProperties() {
		return propertyList;
	}

	@Override
	public @NotNull List<Property> getRootProperties(Matchable target, MatchContext context) {
		return getRootProperties();
	}
}
