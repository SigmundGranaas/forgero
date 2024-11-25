package com.sigmundgranaas.forgero.content.compat;

import com.sigmundgranaas.forgero.abstractions.utils.ModLoaderUtils;
import com.sigmundgranaas.forgero.api.v0.entrypoint.ForgeroClientPreInitializationEntryPoint;
import com.sigmundgranaas.forgero.content.compat.patchouli.GemUpgradeRecipePage;
import com.sigmundgranaas.forgero.content.compat.patchouli.StateCraftingRecipePage;
import com.sigmundgranaas.forgero.content.compat.patchouli.StateUpgradeRecipePage;

import com.sigmundgranaas.forgero.core.Forgero;

import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.util.Identifier;

import static com.sigmundgranaas.forgero.content.compat.ForgeroCompatInitializer.modonomicon;

public class ForgeroCompatClient implements ForgeroClientPreInitializationEntryPoint {
	@Override
	public void onClientPreInitialization() {


		if (ModLoaderUtils.isModPresent("patchouli")) {
			GemUpgradeRecipePage.register();
			StateCraftingRecipePage.register();
			StateUpgradeRecipePage.register();
		}

	}
}
