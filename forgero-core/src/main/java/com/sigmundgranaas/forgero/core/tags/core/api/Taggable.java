package com.sigmundgranaas.forgero.core.tags.core.api;

import com.sigmundgranaas.forgero.core.identifier.OpenIdentifier;

import java.util.Set;

@FunctionalInterface
public interface Taggable {
	/**
	 * @return The set of tags directly applied to this object.
	 */
	Set<OpenIdentifier> getTags();
}
