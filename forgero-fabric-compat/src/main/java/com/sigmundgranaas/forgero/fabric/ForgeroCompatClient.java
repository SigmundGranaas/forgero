package com.sigmundgranaas.forgero.fabric;

import com.sigmundgranaas.forgero.fabric.ipn.IpnNextCompat;
import com.sigmundgranaas.forgero.fabric.patchouli.BookDropOnAdvancement;
import com.sigmundgranaas.forgero.fabric.patchouli.GemUpgradeRecipePage;
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
