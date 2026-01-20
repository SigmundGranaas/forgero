package com.sigmundgranaas.forgero.common.api;

/**
 * Plugin interface for modules that want to be notified about item registrations.
 * This is separate from DataPlugin to keep concerns separated.
 */
public interface ItemRegistrationPlugin {
	/**
	 * Called to register item callbacks before items are processed.
	 */
	void registerCallbacks(ItemRegistrationCallbackContext context);

	/**
	 * @return The unique identifier for this plugin
	 */
	String getId();
}
