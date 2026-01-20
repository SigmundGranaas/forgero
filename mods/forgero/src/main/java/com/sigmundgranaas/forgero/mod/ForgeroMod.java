package com.sigmundgranaas.forgero.mod;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main entry point for the Forgero mod.
 * <p>
 * Forgero provides deep tool, weapon, and armor customization through
 * a component-based system. Tools are crafted from parts (handles, blades, bindings)
 * that can be combined and upgraded.
 */
public class ForgeroMod implements ModInitializer {
	public static final String MOD_ID = "forgero";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// Main initialization done through ForgeroDataLoader
	}
}
