package com.sigmundgranaas.forgero.loader.impl;

import com.sigmundgranaas.forgero.loader.api.DataPlugin;
import com.sigmundgranaas.forgero.loader.api.ItemRegistrationPlugin;
import com.sigmundgranaas.forgero.loader.api.PostLoadPlugin;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Registry that discovers and manages all Forgero plugins using Fabric's entrypoint system.
 */
public class PluginRegistry {
	private static final Logger LOGGER = LoggerFactory.getLogger(PluginRegistry.class);

	// Entrypoint keys that modules will use in their fabric.mod.json
	public static final String DATA_PLUGIN_ENTRYPOINT = "forgero:data_plugin";
	public static final String POST_LOAD_PLUGIN_ENTRYPOINT = "forgero:post_load_plugin";
	public static final String ITEM_REGISTRATION_PLUGIN_ENTRYPOINT = "forgero:item_registration_plugin";

	private final List<DataPlugin> dataPlugins = new ArrayList<>();
	private final List<PostLoadPlugin> postLoadPlugins = new ArrayList<>();
	private final List<ItemRegistrationPlugin> itemRegistrationPlugins = new ArrayList<>();
	private final Set<String> registeredPluginIds = new HashSet<>();

	/**
	 * Discovers all plugins from Fabric entrypoints.
	 * This should be called once during initialization.
	 */
	public void discoverPlugins() {
		LOGGER.info("Discovering Forgero plugins...");

		// Discover data plugins
		discoverDataPlugins();

		// Discover post-load plugins
		discoverPostLoadPlugins();

		// Discover item registration plugins
		discoverItemRegistrationPlugins();

		LOGGER.info("Plugin discovery complete. Found {} data plugins, {} post-load plugins, and {} item registration plugins",
				dataPlugins.size(), postLoadPlugins.size(), itemRegistrationPlugins.size());
	}

	private void discoverDataPlugins() {
		List<EntrypointContainer<DataPlugin>> containers =
				FabricLoader.getInstance().getEntrypointContainers(DATA_PLUGIN_ENTRYPOINT, DataPlugin.class);

		for (EntrypointContainer<DataPlugin> container : containers) {
			try {
				DataPlugin plugin = container.getEntrypoint();
				String pluginId = plugin.getId();

				if (registeredPluginIds.contains(pluginId)) {
					LOGGER.warn("Duplicate plugin ID '{}' from mod '{}'. Skipping.",
							pluginId, container.getProvider().getMetadata().getId());
					continue;
				}

				dataPlugins.add(plugin);
				registeredPluginIds.add(pluginId(pluginId, DATA_PLUGIN_ENTRYPOINT));

				LOGGER.debug("Registered data plugin '{}' from mod '{}'",
						pluginId, container.getProvider().getMetadata().getId());

			} catch (Exception e) {
				LOGGER.error("Failed to load data plugin from mod '{}'",
						container.getProvider().getMetadata().getId(), e);
			}
		}
	}

	private String pluginId(String pluginId, String type) {
		return type + "_" + pluginId;
	}

	private void discoverPostLoadPlugins() {
		List<EntrypointContainer<PostLoadPlugin>> containers =
				FabricLoader.getInstance().getEntrypointContainers(POST_LOAD_PLUGIN_ENTRYPOINT, PostLoadPlugin.class);

		for (EntrypointContainer<PostLoadPlugin> container : containers) {
			try {
				PostLoadPlugin plugin = container.getEntrypoint();
				String pluginId = plugin.getId();

				if (registeredPluginIds.contains(pluginId)) {
					LOGGER.warn("Duplicate plugin ID '{}' from mod '{}'. Skipping.",
							pluginId, container.getProvider().getMetadata().getId());
					continue;
				}

				postLoadPlugins.add(plugin);
				registeredPluginIds.add(pluginId(pluginId, POST_LOAD_PLUGIN_ENTRYPOINT));

				LOGGER.debug("Registered post-load plugin '{}' from mod '{}'",
						pluginId, container.getProvider().getMetadata().getId());

			} catch (Exception e) {
				LOGGER.error("Failed to load post-load plugin from mod '{}'",
						container.getProvider().getMetadata().getId(), e);
			}
		}
	}

	private void discoverItemRegistrationPlugins() {
		List<EntrypointContainer<ItemRegistrationPlugin>> containers =
				FabricLoader.getInstance().getEntrypointContainers(ITEM_REGISTRATION_PLUGIN_ENTRYPOINT, ItemRegistrationPlugin.class);

		for (EntrypointContainer<ItemRegistrationPlugin> container : containers) {
			try {
				ItemRegistrationPlugin plugin = container.getEntrypoint();
				String pluginId = plugin.getId();

				if (registeredPluginIds.contains(pluginId)) {
					LOGGER.warn("Duplicate plugin ID '{}' from mod '{}'. Skipping.",
							pluginId, container.getProvider().getMetadata().getId());
					continue;
				}

				itemRegistrationPlugins.add(plugin);
				registeredPluginIds.add(pluginId(pluginId, ITEM_REGISTRATION_PLUGIN_ENTRYPOINT));

				LOGGER.debug("Registered item registration plugin '{}' from mod '{}'",
						pluginId, container.getProvider().getMetadata().getId());

			} catch (Exception e) {
				LOGGER.error("Failed to load item registration plugin from mod '{}'",
						container.getProvider().getMetadata().getId(), e);
			}
		}
	}

	/**
	 * Gets all registered data plugins in the order they were discovered.
	 */
	public List<DataPlugin> getDataPlugins() {
		return Collections.unmodifiableList(dataPlugins);
	}

	/**
	 * Gets all registered post-load plugins in the order they were discovered.
	 */
	public List<PostLoadPlugin> getPostLoadPlugins() {
		return Collections.unmodifiableList(postLoadPlugins);
	}

	/**
	 * Gets all registered item registration plugins in the order they were discovered.
	 */
	public List<ItemRegistrationPlugin> getItemRegistrationPlugins() {
		return Collections.unmodifiableList(itemRegistrationPlugins);
	}

	/**
	 * Allows manual registration of plugins
	 */
	public void registerPlugin(DataPlugin plugin) {
		String pluginId = plugin.getId();
		if (!registeredPluginIds.contains(pluginId)) {
			dataPlugins.add(plugin);
			registeredPluginIds.add(pluginId);
			LOGGER.debug("Manually registered data plugin '{}'", pluginId);
		}
	}

	/**
	 * Allows manual registration of plugins
	 */
	public void registerPlugin(PostLoadPlugin plugin) {
		String pluginId = plugin.getId();
		if (!registeredPluginIds.contains(pluginId)) {
			postLoadPlugins.add(plugin);
			registeredPluginIds.add(pluginId);
			LOGGER.debug("Manually registered post-load plugin '{}'", pluginId);
		}
	}

	/**
	 * Allows manual registration of plugins for testing or special cases.
	 */
	public void registerPlugin(ItemRegistrationPlugin plugin) {
		String pluginId = plugin.getId();
		if (!registeredPluginIds.contains(pluginId)) {
			itemRegistrationPlugins.add(plugin);
			registeredPluginIds.add(pluginId);
			LOGGER.debug("Manually registered item registration plugin '{}'", pluginId);
		}
	}
}
