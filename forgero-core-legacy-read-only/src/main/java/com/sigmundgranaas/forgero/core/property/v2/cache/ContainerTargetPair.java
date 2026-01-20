package com.sigmundgranaas.forgero.core.property.v2.cache;

import com.google.common.base.Objects;
import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;

import static com.sigmundgranaas.forgero.core.util.match.Matchable.DEFAULT_TRUE;

public record ContainerTargetPair(PropertyContainer container, Matchable target, MatchContext context) {

	public static ContainerTargetPair of(PropertyContainer container) {
		return new ContainerTargetPair(container, DEFAULT_TRUE, MatchContext.of());
	}

	public static ContainerTargetPair of(PropertyContainer container, Matchable matchable, MatchContext context) {
		return new ContainerTargetPair(container, matchable, context);
	}

	public static ContainerTargetPair of(PropertyContainer container, MatchContext context) {
		return new ContainerTargetPair(container, DEFAULT_TRUE, context);
	}

	@Override
	public int hashCode() {
		if (target == DEFAULT_TRUE && context == MatchContext.of()) {
			return container.hashCode();
		}
		return Objects.hashCode(container, target, context);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ContainerTargetPair that = (ContainerTargetPair) o;
		return java.util.Objects.equals(container, that.container) && java.util.Objects.equals(target, that.target) && java.util.Objects.equals(context, that.context);
	}
}
