package com.sigmundgranaas.forgero.drp.api.lifecycle;

/**
 * Defines when a dynamic resource pack is injected into the resource loading pipeline.
 */
public enum ResourcePackPhase {
	/**
	 * Injected before vanilla resource packs.
	 * Resources can be overridden by vanilla and mod packs.
	 * This is the most common choice for Forgero content.
	 */
	BEFORE_VANILLA,

	/**
	 * Injected after vanilla resource packs but before mod packs.
	 * Resources override vanilla but can be overridden by other mods.
	 */
	AFTER_VANILLA,

	/**
	 * Injected after all other packs.
	 * Resources cannot be overridden (use with caution).
	 */
	HIGHEST_PRIORITY
}
