package com.sigmundgranaas.forgero.smithing;

import com.sigmundgranaas.forgero.smithing.screen.BloomeryScreen;
import com.sigmundgranaas.forgero.smithing.screen.ModScreenHandlers;

import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.fabricmc.api.ClientModInitializer;

public class SmithingModuleClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        HandledScreens.register(ModScreenHandlers.BLOOMERY_SCREEN_HANDLER, BloomeryScreen::new);
    }
}
