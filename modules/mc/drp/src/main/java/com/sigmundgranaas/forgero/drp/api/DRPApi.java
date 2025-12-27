package com.sigmundgranaas.forgero.drp.api;

import com.sigmundgranaas.forgero.drp.api.debug.DebugExporter;
import com.sigmundgranaas.forgero.drp.api.lifecycle.HotReloadListener;
import com.sigmundgranaas.forgero.drp.api.lifecycle.ResourcePackPhase;
import com.sigmundgranaas.forgero.drp.impl.DRPApiImpl;
import net.minecraft.util.Identifier;

import java.nio.file.Path;
import java.util.List;

/**
 * Main entry point for the Dynamic Resource Pack (DRP) generation system.
 *
 * <p>This API provides a fluent, type-safe interface for generating runtime resources
 * including tags, recipes, models, language files, and textures.</p>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * DRPApi api = DRPApi.getInstance();
 *
 * // Create and configure a resource pack
 * DynamicResourcePack pack = api.createPack("mymod:dynamic_resources")
 *     .description("My dynamic resources")
 *     .priority(10)
 *     .build();
 *
 * // Add resources using builders
 * pack.addTag(TagBuilder.items("mymod:my_items")
 *     .add("minecraft:diamond")
 *     .add("minecraft:emerald")
 *     .includeTag("minecraft:items/swords"));
 *
 * // Register the pack
 * api.register(pack, ResourcePackPhase.BEFORE_VANILLA);
 * }</pre>
 *
 * @see DynamicResourcePack
 * @see ResourcePackPhase
 */
public interface DRPApi {

	/**
	 * Gets the singleton instance of the DRPApi.
	 *
	 * @return The DRPApi instance
	 */
	static DRPApi getInstance() {
		return DRPApiImpl.getInstance();
	}

	/**
	 * Creates a new resource pack builder with the given identifier.
	 *
	 * @param id The unique identifier for this resource pack (e.g., "forgero:dynamic")
	 * @return A new PackBuilder for configuring the pack
	 */
	PackBuilder createPack(String id);

	/**
	 * Creates a new resource pack builder with the given identifier.
	 *
	 * @param id The Minecraft identifier for this resource pack
	 * @return A new PackBuilder for configuring the pack
	 */
	PackBuilder createPack(Identifier id);

	/**
	 * Registers a resource pack for injection at the specified phase.
	 *
	 * @param pack  The resource pack to register
	 * @param phase When to inject the pack (before/after vanilla, etc.)
	 */
	void register(DynamicResourcePack pack, ResourcePackPhase phase);

	/**
	 * Registers a resource pack for injection before vanilla packs.
	 * This is the default and most common registration.
	 *
	 * @param pack The resource pack to register
	 */
	default void register(DynamicResourcePack pack) {
		register(pack, ResourcePackPhase.BEFORE_VANILLA);
	}

	/**
	 * Enables hot-reloading for the specified pack.
	 * When enabled, changes can be pushed without a full game restart.
	 *
	 * @param pack     The pack to enable hot-reloading for
	 * @param listener Callback for reload events
	 */
	void enableHotReload(DynamicResourcePack pack, HotReloadListener listener);

	/**
	 * Triggers a hot-reload for all packs that have hot-reloading enabled.
	 * Typically called in response to F3+T or a custom reload command.
	 */
	void triggerHotReload();

	/**
	 * Exports all registered resource packs to disk for debugging.
	 *
	 * @param outputPath The directory to export to
	 * @return A DebugExporter for configuring the export
	 */
	DebugExporter exportToPath(Path outputPath);

	/**
	 * Gets a read-only view of all registered packs.
	 *
	 * @return Unmodifiable list of registered packs
	 */
	List<DynamicResourcePack> getRegisteredPacks();

	/**
	 * Builder for configuring a new DynamicResourcePack.
	 */
	interface PackBuilder {

		/**
		 * Sets the human-readable description for the pack.
		 *
		 * @param description The pack description
		 * @return This builder
		 */
		PackBuilder description(String description);

		/**
		 * Sets the priority for resource ordering.
		 * Higher priority packs override lower priority packs.
		 *
		 * @param priority The pack priority (default: 0)
		 * @return This builder
		 */
		PackBuilder priority(int priority);

		/**
		 * Enables validation for all resources added to this pack.
		 * Invalid resources will throw exceptions rather than being silently ignored.
		 *
		 * @param validate Whether to enable validation (default: true in dev, false in prod)
		 * @return This builder
		 */
		PackBuilder validateResources(boolean validate);

		/**
		 * Builds the configured resource pack.
		 *
		 * @return A new DynamicResourcePack instance
		 */
		DynamicResourcePack build();
	}
}
