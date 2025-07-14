package com.sigmundgranaas.forgero.core.property.condition;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.property.context.ResolutionContext;

/**
 * A condition evaluated during the static "bake" phase of resolution.
 * It tests properties against the structure of the component itself.
 */
public interface StaticCondition {
	/**
	 * Tests this condition against the component's own structural context.
	 *
	 * @param context The context of the component being resolved.
	 * @return true if the condition is met, false otherwise.
	 */
	boolean test(ResolutionContext context);

	OpenIdentifier type();
}
