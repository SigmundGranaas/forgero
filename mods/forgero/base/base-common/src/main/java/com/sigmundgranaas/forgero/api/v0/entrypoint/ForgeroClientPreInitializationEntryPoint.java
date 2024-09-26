package com.sigmundgranaas.forgero.api.v0.entrypoint;

/**
 * Entry point for Forgero client-side pre-initialization.
 * <p>
 * Use this entry point to execute code that needs to be run on the client before Forgero starts the data pipeline
 */
public interface ForgeroClientPreInitializationEntryPoint {
	void onClientPreInitialization();
}
