package com.sigmundgranaas.forgero.smithing.screen;

import com.sigmundgranaas.forgero.core.Forgero;

import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;

public class ModScreenHandlers {
    public static ScreenHandlerType<BloomeryScreenHandler> BLOOMERY_SCREEN_HANDLER;

    public static void registerAllScreenHandlers() {
        BLOOMERY_SCREEN_HANDLER = ScreenHandlerRegistry.registerSimple(
            new Identifier(Forgero.NAMESPACE, "bloomery"),
            BloomeryScreenHandler::new
        );
    }
}
