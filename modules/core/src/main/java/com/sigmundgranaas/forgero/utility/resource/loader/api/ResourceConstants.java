package com.sigmundgranaas.forgero.utility.resource.loader.api;

import java.util.Set;

/**
 * Constants for the resource loading system.
 * <p>
 * These constants define standard namespaces, directories, and file extensions
 * used throughout the Forgero resource loading architecture.
 */
public final class ResourceConstants {

	private ResourceConstants() {
		// Utility class - no instantiation
	}

	// ==================== Namespaces ====================

	/**
	 * The Forgero mod namespace.
	 */
	public static final String NAMESPACE_FORGERO = "forgero";

	/**
	 * The Minecraft namespace.
	 */
	public static final String NAMESPACE_MINECRAFT = "minecraft";

	/**
	 * The common/convention namespace used by Fabric.
	 */
	public static final String NAMESPACE_COMMON = "c";

	/**
	 * Default namespaces used by Forgero resource providers.
	 */
	public static final Set<String> DEFAULT_NAMESPACES = Set.of(NAMESPACE_FORGERO, NAMESPACE_MINECRAFT);

	// ==================== Top-Level Directories ====================

	/**
	 * The data directory for server-side resources.
	 */
	public static final String DIRECTORY_DATA = "data";

	/**
	 * The assets directory for client-side resources.
	 */
	public static final String DIRECTORY_ASSETS = "assets";

	/**
	 * The textures subdirectory within assets.
	 */
	public static final String DIRECTORY_TEXTURES = "textures";

	/**
	 * The models subdirectory within assets.
	 */
	public static final String DIRECTORY_MODELS = "models";

	// ==================== Resource Subdirectories ====================

	/**
	 * The materials subdirectory for material definitions.
	 */
	public static final String SUBDIRECTORY_MATERIALS = "materials";

	/**
	 * The schematics subdirectory for schematic definitions.
	 */
	public static final String SUBDIRECTORY_SCHEMATICS = "schematics";

	/**
	 * The parts subdirectory for part definitions.
	 */
	public static final String SUBDIRECTORY_PARTS = "parts";

	/**
	 * The equipment subdirectory for equipment definitions.
	 */
	public static final String SUBDIRECTORY_EQUIPMENT = "equipment";

	/**
	 * The tags subdirectory for tag definitions.
	 */
	public static final String SUBDIRECTORY_TAGS = "tags";

	/**
	 * The properties subdirectory for property definitions.
	 */
	public static final String SUBDIRECTORY_PROPERTIES = "properties";

	// ==================== File Extensions ====================

	/**
	 * JSON file extension.
	 */
	public static final String EXTENSION_JSON = "json";

	/**
	 * PNG image file extension.
	 */
	public static final String EXTENSION_PNG = "png";

	/**
	 * MCMeta file extension for Minecraft metadata.
	 */
	public static final String EXTENSION_MCMETA = "mcmeta";

	// ==================== Provider Priorities ====================

	/**
	 * Default priority for base/fallback providers (lowest).
	 */
	public static final int PRIORITY_BASE = 0;

	/**
	 * Default priority for mod content providers.
	 */
	public static final int PRIORITY_MOD = 50;

	/**
	 * Default priority for Minecraft resource manager (resource packs).
	 */
	public static final int PRIORITY_RESOURCE_PACK = 100;

	/**
	 * Default priority for programmatic/override providers (highest).
	 */
	public static final int PRIORITY_PROGRAMMATIC = 200;

	// ==================== Fabric Integration ====================

	/**
	 * The custom value key in fabric.mod.json for Forgero resource mods.
	 */
	public static final String FABRIC_FORGERO_RESOURCE_KEY = "forgeroResource";
}
