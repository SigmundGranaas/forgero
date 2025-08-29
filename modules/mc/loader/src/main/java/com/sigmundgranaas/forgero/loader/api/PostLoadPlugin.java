package com.sigmundgranaas.forgero.loader.api;

/**
 * Plugin interface for modules that need to perform initialization
 * after all data has been loaded and processed.
 */
public interface PostLoadPlugin {
	/**
	 * Called after all data has been loaded and items registered.
	 */
	void onDataLoaded(DataLoadingContext context);

	/**
	 * @return The unique identifier for this plugin
	 */
	String getId();
}
