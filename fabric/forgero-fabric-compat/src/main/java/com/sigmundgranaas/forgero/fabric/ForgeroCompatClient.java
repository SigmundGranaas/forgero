package com.sigmundgranaas.forgero.fabric;

import com.sigmundgranaas.forgero.fabric.patchouli.GemUpgradeRecipePage;
import com.sigmundgranaas.forgero.fabric.patchouli.StateCraftingRecipePage;
import com.sigmundgranaas.forgero.fabric.patchouli.StateUpgradeRecipePage;
import com.sigmundgranaas.forgero.fabric.tag.BlockTagCompatRegistration;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class ForgeroCompatClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		if (FabricLoader.getInstance().isModLoaded("patchouli")) {
			GemUpgradeRecipePage.register();
			StateCraftingRecipePage.register();
			StateUpgradeRecipePage.register();
		}

		BlockTagCompatRegistration.register();
	}
}
