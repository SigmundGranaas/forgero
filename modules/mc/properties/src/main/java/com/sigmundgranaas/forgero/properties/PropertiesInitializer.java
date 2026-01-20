package com.sigmundgranaas.forgero.properties;

import com.sigmundgranaas.forgero.common.api.ForgeroInitializedCallback;
import com.sigmundgranaas.forgero.common.api.ForgeroServices;
import com.sigmundgranaas.forgero.properties.minecraft.blockbreaking.BlockBreakingManager;
import com.sigmundgranaas.forgero.properties.minecraft.loot.LootManager;
import com.sigmundgranaas.forgero.properties.minecraft.useinteraction.UseInteractionManager;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Initializes all property managers when Forgero is initialized.
 * This class registers a callback to receive ForgeroServices and
 * passes them to all manager classes that need them.
 */
public class PropertiesInitializer implements ModInitializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesInitializer.class);

	@Override
	public void onInitialize() {
		// Use registerAndReplay to handle both cases:
		// - If ForgeroDataLoader hasn't run yet, register for the future event
		// - If ForgeroDataLoader has already run, immediately invoke with stored services
		ForgeroInitializedCallback.registerAndReplay(this::initializeManagers);
	}

	private void initializeManagers(ForgeroServices services) {
		// Most managers refactored to use ItemPropertyApi - no initialization needed
		// Only these three still require initialization:
		BlockBreakingManager.initialize(services);
		LootManager.initialize(services);
		UseInteractionManager.initialize(services);

		LOGGER.debug("Property managers initialized");
	}
}
