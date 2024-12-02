package com.sigmundgranaas.forgero.abstractions.utils;

import dev.architectury.injectables.annotations.ExpectPlatform;

public class ModLoaderUtils {
	/**
	 * Checks if a mod is present during loading.
	 */
	@ExpectPlatform
	public static boolean isModPresent(String id) {
		return true;
		// throw new AssertionError("Platform implementation expected.");
	}
}
