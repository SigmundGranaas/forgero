package com.sigmundgranaas.forgero.loader.impl;

import com.sigmundgranaas.forgero.common.tags.api.TagResolver;
import com.sigmundgranaas.forgero.common.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Supplier;

/**
 * Orchestrates plugin discovery, registration, and lifecycle management.
 * Handles phases 1, 3, 7, and 9 of the initialization pipeline.
 */
public class PluginOrchestrator {
	private static final Logger LOGGER = LoggerFactory.getLogger(PluginOrchestrator.class);

	private final PluginRegistry registry;

	public PluginOrchestrator() {
		this.registry = new PluginRegistry();
	}

	/**
	 * Phase 1: Discover all plugins from Fabric entrypoints.
	 * <p>
	 * All plugins (including ForgeroDefaultsPlugin and CoreSlotTypesPlugin) are now
	 * discovered via Fabric entrypoints defined in fabric.mod.json files.
	 */
	public void discoverPlugins() {
		registry.discoverPlugins();

		int dataPlugins = registry.getDataPlugins().size();
		int itemRegPlugins = registry.getItemRegistrationPlugins().size();
		int postLoadPlugins = registry.getPostLoadPlugins().size();

		LOGGER.debug("Discovered {} plugins: {} data, {} item registration, {} post-load",
				dataPlugins + itemRegPlugins + postLoadPlugins, dataPlugins, itemRegPlugins, postLoadPlugins);
	}

	/**
	 * Phase 3: Register plugin requirements (item creators, codecs, conditions).
	 *
	 * @param tagResolverSupplier Supplier for the TagResolver (allows lazy loading)
	 * @return The registration context containing all registered requirements
	 */
	public PluginRegistrationContextImpl registerPluginRequirements(Supplier<TagResolver> tagResolverSupplier) {
		PluginRegistrationContextImpl registrationContext = new PluginRegistrationContextImpl(tagResolverSupplier);

		for (DataPlugin plugin : registry.getDataPlugins()) {
			try {
				LOGGER.debug("Registering requirements for data plugin: {}", plugin.getId());
				plugin.register(registrationContext);
			} catch (Exception e) {
				LOGGER.error("Failed to register data plugin: {}", plugin.getId(), e);
			}
		}

		return registrationContext;
	}

	/**
	 * Phase 7: Setup item registration callbacks from plugins.
	 *
	 * @param itemRegistrar The item registrar to add callbacks to
	 */
	public void setupItemCallbacks(ItemRegistrar itemRegistrar) {
		ItemRegistrationCallbackContext callbackContext = callback -> itemRegistrar.addRegistrationCallback(callback);

		for (ItemRegistrationPlugin plugin : registry.getItemRegistrationPlugins()) {
			try {
				LOGGER.debug("Registering item callbacks from plugin: {}", plugin.getId());
				plugin.registerCallbacks(callbackContext);
			} catch (Exception e) {
				LOGGER.error("Failed to register item callbacks from plugin: {}", plugin.getId(), e);
			}
		}
	}

	/**
	 * Phase 9: Notify all post-load plugins that data loading is complete.
	 *
	 * @param context The data loading context with all services
	 */
	public void notifyPostLoad(DataLoadingContext context) {
		for (PostLoadPlugin plugin : registry.getPostLoadPlugins()) {
			try {
				LOGGER.debug("Notifying post-load plugin: {}", plugin.getId());
				plugin.onDataLoaded(context);
			} catch (Exception e) {
				LOGGER.error("Error in post-load plugin: {}", plugin.getId(), e);
			}
		}
	}

	/**
	 * Gets the list of discovered data plugins.
	 */
	public List<DataPlugin> getDataPlugins() {
		return registry.getDataPlugins();
	}

	/**
	 * Gets the list of discovered item registration plugins.
	 */
	public List<ItemRegistrationPlugin> getItemRegistrationPlugins() {
		return registry.getItemRegistrationPlugins();
	}

	/**
	 * Gets the list of discovered post-load plugins.
	 */
	public List<PostLoadPlugin> getPostLoadPlugins() {
		return registry.getPostLoadPlugins();
	}
}
