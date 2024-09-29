package com.sigmundgranaas.forgero.abstractions.utils.fabric;

import net.fabricmc.loader.api.FabricLoader;

/**
 * Implements {@link com.sigmundgranaas.forgero.abstractions.utils.ModLoaderUtils}.
 */
@SuppressWarnings("unused")
public class ModLoaderUtilsImpl {
	/**
	 * Checks if a mod is present during loading.
	 */
	public static boolean isModPresent(String id) {
		return FabricLoader.getInstance().isModLoaded(id);
	}
}
