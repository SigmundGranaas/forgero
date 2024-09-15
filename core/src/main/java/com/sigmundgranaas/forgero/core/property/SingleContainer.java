package com.sigmundgranaas.forgero.core.property;

import java.util.List;
import java.util.Objects;

import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;
import org.jetbrains.annotations.NotNull;

public class SingleContainer implements PropertyContainer {
	private final Property property;

	public SingleContainer(Property property) {
		this.property = property;
	}

	@Override
	public @NotNull
	List<Property> getRootProperties() {
		return List.of(property);
	}

	@Override
	public @NotNull List<Property> getRootProperties(Matchable target, MatchContext context) {
		return getRootProperties();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		SingleContainer that = (SingleContainer) o;
		return Objects.equals(property, that.property);
	}

	@Override
	public int hashCode() {
		return Objects.hash(property);
	}
}
