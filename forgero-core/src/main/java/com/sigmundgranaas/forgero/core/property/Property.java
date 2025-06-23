package com.sigmundgranaas.forgero.core.property;

import java.util.List;

import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;

/**
 * Base interface for properties which will change attributes, create passive buffs and enabling special actions
 * Properties are designed to be pooled together and filtered every time they are used.
 * <p>
 * Properties can either be an attribute, active effect and a passive effect.
 */

public interface Property {
	static PropertyStream stream(List<Property> properties) {
		return new PropertyStream(properties.stream(), Matchable.DEFAULT_TRUE, MatchContext.of());
	}

	static PropertyStream stream(List<Property> properties, Matchable match, MatchContext context) {
		return new PropertyStream(properties.stream(), match, context);
	}

	String type();

	default float applyAttribute(Matchable target, MatchContext context, float currentAttribute) {
		return currentAttribute;
	}

	default boolean applyCondition(Matchable target, MatchContext context) {
		return true;
	}

	default boolean isDynamic() {
		return false;
	}
}
