package com.sigmundgranaas.forgero.loader.impl;

import com.sigmundgranaas.forgero.common.api.ForgeroServices;
import com.sigmundgranaas.forgero.loader.internal.ForgeroApiAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles the API initialization and event firing.
 * Responsible for phases 11 and 12 of the initialization pipeline.
 */
public class ApiInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger(ApiInitializer.class);

	/**
	 * Phase 11: Fire the initialization event for DI-based subscribers.
	 * <p>
	 * This sets the services on ForgeroApi and fires the callback event.
	 *
	 * @param services The services to provide to subscribers
	 */
	public void fireInitializationEvent(ForgeroServices services) {
		ForgeroApiAccess.initialize(services);
	}
}
