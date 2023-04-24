package com.sigmundgranaas.forgero.quilt;

import com.sigmundgranaas.forgero.quilt.patchouli.GemUpgradeRecipePage;
import com.sigmundgranaas.forgero.quilt.patchouli.StateCraftingRecipe;
import com.sigmundgranaas.forgero.quilt.patchouli.StateUpgradeRecipe;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class ForgeroCompatClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		if (FabricLoader.getInstance().isModLoaded("patchouli")) {
			GemUpgradeRecipePage.register();
			StateCraftingRecipe.register();
			StateUpgradeRecipe.register();
		}
	}
}
