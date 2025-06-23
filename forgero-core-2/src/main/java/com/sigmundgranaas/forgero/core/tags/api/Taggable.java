package com.sigmundgranaas.forgero.core.tags.api;

import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier;

import java.util.Set;

@FunctionalInterface
public interface Taggable {
	/**
	 * @return The set of tags directly applied to this object.
	 */
	Set<OpenIdentifier> getTags();
}
