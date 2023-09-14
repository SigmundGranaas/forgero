package com.sigmundgranaas.forgero.core.property.v2.cache;

import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.util.match.Matchable;

public record ContainerTargetPair(PropertyContainer container, Matchable target) {
	public static ContainerTargetPair of(PropertyContainer container) {
		return new ContainerTargetPair(container, Matchable.DEFAULT_TRUE);
	}

	@Override
	public int hashCode() {
		return container.hashCode() + target.hashCode();
	}
}
