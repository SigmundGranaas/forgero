package com.sigmundgranaas.forgero.content.compat;

import com.sigmundgranaas.forgero.content.compat.patchouli.GemUpgradeRecipePage;
import com.sigmundgranaas.forgero.content.compat.patchouli.StateCraftingRecipePage;
import com.sigmundgranaas.forgero.content.compat.patchouli.StateUpgradeRecipePage;
import com.sigmundgranaas.forgero.content.compat.tag.BlockTagCompatRegistration;

import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.util.Identifier;

import static com.sigmundgranaas.forgero.content.compat.ForgeroCompatInitializer.modonomicon;

public class ForgeroCompatClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		if (modonomicon.get()) {
			ModelLoadingPlugin.register(ctx -> ctx.addModels(new ModelIdentifier(new Identifier("forgero:guidebook"), "inventory")));
		}

		if (FabricLoader.getInstance().isModLoaded("patchouli")) {
			GemUpgradeRecipePage.register();
			StateCraftingRecipePage.register();
			StateUpgradeRecipePage.register();
		}

		BlockTagCompatRegistration.register();
	}
}
