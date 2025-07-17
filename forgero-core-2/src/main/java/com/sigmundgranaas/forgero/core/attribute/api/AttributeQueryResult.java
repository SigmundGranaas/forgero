package com.sigmundgranaas.forgero.core.attribute.api;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.property.api.Resolver;

/**
 * Represents the final, resolved result for an attribute query.
 * This object is the product of the {@link Resolver} when using the {@code AttributeEngine.KEY}.
 * It is context-aware and can be queried for the value of any specific attribute.
 * This design handles the "asymmetric" nature of attribute resolution, where you resolve
 * all attributes at once but then query for them one by one.
 */
@FunctionalInterface
public interface AttributeQueryResult {
	/**
	 * A constant representing an empty result, which always returns 0 for any attribute.
	 * This is useful for avoiding null checks.
	 */
	AttributeQueryResult EMPTY = attributeType -> 0f;

	/**
	 * Gets the final computed value of a specific attribute.
	 *
	 * @param attributeType The ID of the attribute to get (e.g., {@code DefaultAttributes.ATTACK_DAMAGE}).
	 * @return The final computed value of the attribute for the context this result was created with.
	 */
	float getValue(OpenIdentifier attributeType);
}
