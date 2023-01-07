package com.sigmundgranaas.forgero.fabric;

import com.sigmundgranaas.forgero.fabric.ipn.IpnNextCompat;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class ForgeroCompatInitializer implements ModInitializer {
    @Override
    public void onInitialize() {
        if (FabricLoader.getInstance().isModLoaded("inventoryprofilesnext")) {
            //IpnNextCompat.blackListForgero();
        }
    }
}
