package com.sigmundgranaas.forgero.fabric.api.entrypoint;

/**
 * Entry point for Forgero pre initialization.
 * <p>
 * Use this entry point to execute code that needs to be run before Forgero start the data pipeline
 *
 * @see com.sigmundgranaas.forgero.fabric.initialization.ForgeroPreInit
 */
public interface ForgeroPreInitializationEntryPoint {
	void onPreInitialization();
}
