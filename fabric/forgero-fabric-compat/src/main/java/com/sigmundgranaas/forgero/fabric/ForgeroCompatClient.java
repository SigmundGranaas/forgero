package com.sigmundgranaas.forgero.fabric;

import com.sigmundgranaas.forgero.fabric.patchouli.GemUpgradeRecipePage;
import com.sigmundgranaas.forgero.fabric.patchouli.StateCraftingRecipePage;
import com.sigmundgranaas.forgero.fabric.patchouli.StateUpgradeRecipePage;
import com.sigmundgranaas.forgero.fabric.tag.BlockTagCompatRegistration;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.util.Identifier;

import static com.sigmundgranaas.forgero.fabric.ForgeroCompatInitializer.modonomicon;

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
