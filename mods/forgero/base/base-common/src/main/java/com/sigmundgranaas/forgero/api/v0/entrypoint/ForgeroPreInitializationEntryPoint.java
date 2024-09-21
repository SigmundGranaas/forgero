package com.sigmundgranaas.forgero.api.v0.entrypoint;

/**
 * Entry point for Forgero pre initialization.
 * <p>
 * Use this entry point to execute code that needs to be run before Forgero start the data pipeline
 */
public interface ForgeroPreInitializationEntryPoint {
	void onPreInitialization();
}
