package com.sigmundgranaas.forgero.common.api;

/**
 * Plugin interface for modules to register their data processing needs
 * before data loading occurs.
 */
public interface DataPlugin {
	/**
	 * Called during early initialization to register item creators and other
	 * data processing requirements.
	 */
	void register(PluginRegistrationContext context);

	/**
	 * @return The unique identifier for this plugin
	 */
	String getId();
}
