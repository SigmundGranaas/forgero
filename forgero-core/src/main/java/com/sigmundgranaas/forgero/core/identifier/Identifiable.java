package com.sigmundgranaas.forgero.core.identifier;

@FunctionalInterface
public interface Identifiable {
	/**
	 * @return The unique OpenIdentifier for this object.
	 */
	OpenIdentifier id();
}
