package com.sigmundgranaas.forgero.blocks;

import com.sigmundgranaas.forgero.blocks.assembly.AssemblyStationScreen;
import com.sigmundgranaas.forgero.blocks.assembly.AssemblyStationScreenHandler;
import com.sigmundgranaas.forgero.blocks.upgrade.UpgradeStationScreen;
import com.sigmundgranaas.forgero.blocks.upgrade.UpgradeStationScreenHandler;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client-side initializer for the blocks module.
 * <p>
 * Registers screen factories for station UIs.
 */
public class BlocksClientPlugin implements ClientModInitializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(BlocksClientPlugin.class);

	@Override
	public void onInitializeClient() {
		// Register screens
		HandledScreens.register(UpgradeStationScreenHandler.TYPE, UpgradeStationScreen::new);
		HandledScreens.register(AssemblyStationScreenHandler.TYPE, AssemblyStationScreen::new);
		LOGGER.debug("Forgero Blocks client initialized");
	}
}
