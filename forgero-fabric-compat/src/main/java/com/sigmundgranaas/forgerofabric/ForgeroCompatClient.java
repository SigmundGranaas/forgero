package com.sigmundgranaas.forgerofabric;

import com.sigmundgranaas.forgerofabric.patchouli.BookDropOnAdvancement;
import com.sigmundgranaas.forgerofabric.patchouli.GemUpgradeRecipePage;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class ForgeroCompatClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        if (FabricLoader.getInstance().isModLoaded("patchouli")) {
            GemUpgradeRecipePage.register();
            BookDropOnAdvancement.registerBookDrop();
        }
    }
}
