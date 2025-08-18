package com.sigmundgranaas.forgero.core.condition.api;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.property.context.DynamicContext;

/**
 * A condition evaluated during the dynamic "apply" phase of resolution.
 * It tests properties against the runtime game context (e.g., target, world).
 */
public interface DynamicCondition {
	boolean test(DynamicContext context);

	OpenIdentifier type();
}
