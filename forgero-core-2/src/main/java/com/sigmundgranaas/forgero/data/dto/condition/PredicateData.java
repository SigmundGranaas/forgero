package com.sigmundgranaas.forgero.data.dto.condition;

import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier;

/**
 * An interface representing a single condition predicate.
 * This can be implemented by any class to create a new, custom condition type.
 */
public interface PredicateData {
	/**
	 * @return The unique type identifier for this predicate, e.g., "forgero:self_has_tag". This string is used
	 * to look up the correct Codec in the registry.
	 */
	OpenIdentifier type();
}
