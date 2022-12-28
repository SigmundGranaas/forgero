package com.sigmundgranaas.forgeroforge;

import com.sigmundgranaas.forgeroforge.patchouli.BookDropOnAdvancement;
import com.sigmundgranaas.forgeroforge.patchouli.GemUpgradeRecipePage;
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
