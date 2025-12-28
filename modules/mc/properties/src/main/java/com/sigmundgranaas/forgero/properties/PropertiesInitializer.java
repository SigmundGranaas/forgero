package com.sigmundgranaas.forgero.properties;

import com.sigmundgranaas.forgero.loader.api.ForgeroInitializedCallback;
import com.sigmundgranaas.forgero.loader.api.ForgeroServices;
import com.sigmundgranaas.forgero.properties.minecraft.blockbreaking.BlockBreakingManager;
import com.sigmundgranaas.forgero.properties.minecraft.blockuse.BlockUseManager;
import com.sigmundgranaas.forgero.properties.minecraft.entityuse.EntityUseManager;
import com.sigmundgranaas.forgero.properties.minecraft.loot.LootManager;
import com.sigmundgranaas.forgero.properties.minecraft.onblockplace.OnBlockPlaceManager;
import com.sigmundgranaas.forgero.properties.minecraft.ondamage.OnDamageReceivedManager;
import com.sigmundgranaas.forgero.properties.minecraft.onhit.OnHitManager;
import com.sigmundgranaas.forgero.properties.minecraft.onhitblock.OnHitBlockManager;
import com.sigmundgranaas.forgero.properties.minecraft.onkill.OnKillManager;
import com.sigmundgranaas.forgero.properties.minecraft.onsneak.OnSneakToggleManager;
import com.sigmundgranaas.forgero.properties.minecraft.ontick.OnTickManager;
import com.sigmundgranaas.forgero.properties.minecraft.swing.SwingHandManager;
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
		LOGGER.info("Properties module registered for Forgero initialization callback.");
	}

	private void initializeManagers(ForgeroServices services) {
		LOGGER.debug("Initializing property managers...");

		OnHitManager.initialize(services);
		OnHitBlockManager.initialize(services);
		OnTickManager.initialize(services);
		OnKillManager.initialize(services);
		OnSneakToggleManager.initialize(services);
		OnDamageReceivedManager.initialize(services);
		OnBlockPlaceManager.initialize(services);
		BlockBreakingManager.initialize(services);
		BlockUseManager.initialize(services);
		EntityUseManager.initialize(services);
		LootManager.initialize(services);
		SwingHandManager.initialize(services);
		UseInteractionManager.initialize(services);

		LOGGER.info("Property managers initialized successfully.");
	}
}
