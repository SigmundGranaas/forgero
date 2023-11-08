package com.sigmundgranaas.forgero.core.property;

import java.util.List;
import java.util.Objects;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;
import org.jetbrains.annotations.NotNull;

public class CompositeContainer implements PropertyContainer {
	private final List<PropertyContainer> containers;

	public CompositeContainer(List<PropertyContainer> containers) {
		this.containers = containers;
	}

	@Override
	public PropertyContainer with(PropertyContainer container) {
		return new CompositeContainer(ImmutableList.<PropertyContainer>builder().addAll(containers).add(container).build());
	}

	@Override
	public @NotNull
	List<Property> getRootProperties() {
		return containers.stream()
				.map(PropertyContainer::getRootProperties)
				.flatMap(List::stream)
				.toList();
	}

	@Override
	public @NotNull List<Property> getRootProperties(Matchable target, MatchContext context) {
		return containers.stream()
				.map(PropertyContainer::getRootProperties)
				.flatMap(List::stream)
				.filter(prop -> prop.applyCondition(target, context))
				.toList();

	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CompositeContainer that = (CompositeContainer) o;
		return Objects.equals(containers, that.containers);
	}

	@Override
	public int hashCode() {
		return Objects.hash(containers);
	}
}
