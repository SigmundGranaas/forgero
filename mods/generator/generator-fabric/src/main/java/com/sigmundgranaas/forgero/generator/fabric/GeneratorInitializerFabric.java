package com.sigmundgranaas.forgero.generator.fabric;

import com.sigmundgranaas.forgero.generator.GeneratorInitializer;
import net.fabricmc.api.ModInitializer;

public class GeneratorInitializerFabric implements ModInitializer {
	/**
	 * Runs the mod initializer.
	 */
	@Override
	public void onInitialize() {
		GeneratorInitializer.onInitialize();
	}
}
