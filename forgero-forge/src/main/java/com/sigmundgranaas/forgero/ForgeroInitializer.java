package com.sigmundgranaas.forgero;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;


@Mod(Forgero.NAMESPACE)
public class ForgeroInitializer {
    public ForgeroInitializer() {
        IEventBus MOD_BUS = FMLJavaModLoadingContext.get().getModEventBus();
    }
}