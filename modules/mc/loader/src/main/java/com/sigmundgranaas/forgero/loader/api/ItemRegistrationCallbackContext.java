package com.sigmundgranaas.forgero.loader.api;

/**
 * Context for registering item callbacks.
 */
public interface ItemRegistrationCallbackContext {
	/**
	 * Register a callback to be notified about item registrations.
	 */
	void addCallback(ItemRegistrationCallback callback);
}
