package com.sigmundgranaas.forgero.common.identifier.api;

@FunctionalInterface
public interface Identifiable {
	/**
	 * @return The unique OpenIdentifier for this object.
	 */
	OpenIdentifier id();
}
