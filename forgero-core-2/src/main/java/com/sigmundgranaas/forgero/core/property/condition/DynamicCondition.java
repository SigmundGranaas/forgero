package com.sigmundgranaas.forgero.core.property.condition;

import com.sigmundgranaas.forgero.core.property.context.DynamicContext;

/**
 * A condition evaluated during the dynamic "apply" phase of resolution.
 * It tests properties against the runtime game context (e.g., target, world).
 * This was formerly the Predicate interface.
 */
@FunctionalInterface
public interface DynamicCondition {
	boolean test(DynamicContext context);
}
