package com.sigmundgranaas.forgero.loader.internal;

import com.sigmundgranaas.forgero.common.api.ForgeroApi;
import com.sigmundgranaas.forgero.common.api.ForgeroServices;

/**
 * Internal access point for ForgeroApi initialization.
 * <p>
 * <b>INTERNAL USE ONLY:</b> This class is intended for use by the Forgero loader
 * infrastructure only. Do not call these methods from external code.
 * <p>
 * This class exists to allow the loader impl package to initialize the API.
 */
public final class ForgeroApiAccess {

	private ForgeroApiAccess() {
		// Private constructor to prevent instantiation
	}

	/**
	 * Initializes the ForgeroApi with the provided services.
	 * <p>
	 * <b>INTERNAL USE ONLY:</b> This method should only be called by the Forgero
	 * loader during initialization. Calling this from external code will result
	 * in an IllegalStateException.
	 *
	 * @param services The services to initialize with
	 * @throws IllegalArgumentException if services is null
	 * @throws IllegalStateException if already initialized
	 */
	public static void initialize(ForgeroServices services) {
		ForgeroApi.Initializer.initialize(services);
	}

	/**
	 * Resets the ForgeroApi state.
	 * <p>
	 * <b>TEST USE ONLY:</b> This method should only be called from test code
	 * to reset state between tests.
	 */
	public static void reset() {
		ForgeroApi.Initializer.reset();
	}
}
