package com.sigmundgranaas.forgero.core.condition;

import java.util.List;
import java.util.Objects;

import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;
import org.jetbrains.annotations.NotNull;

public class SimpleCondition implements PropertyContainer {
	protected final List<Property> propertyList;
	private Integer hash = null;

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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		SimpleCondition that = (SimpleCondition) o;
		return Objects.equals(hashCode(), that.hashCode());
	}

	@Override
	public int hashCode() {
		if (hash == null) {
			hash = Objects.hash(propertyList);
		}
		return hash;
	}
}
