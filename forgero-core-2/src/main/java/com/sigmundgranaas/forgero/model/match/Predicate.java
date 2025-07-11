package com.sigmundgranaas.forgero.model.match;

import com.sigmundgranaas.forgero.model.api.ModelResolutionContext;

/**
 * A functional interface for conditions that can be tested against a ModelResolutionContext.
 * Used to conditionally apply model variants.
 */
@FunctionalInterface
public interface Predicate {
	boolean test(ModelResolutionContext context);
}
