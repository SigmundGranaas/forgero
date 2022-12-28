package com.sigmundgranaas.forgeroforge;

import com.google.common.base.Stopwatch;
import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.ForgeroStateRegistry;
import com.sigmundgranaas.forgero.resource.PipelineBuilder;
import com.sigmundgranaas.forgero.settings.ForgeroSettings;
import com.sigmundgranaas.forgeroforge.pack.ForgePackFinder;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forgespi.language.IModInfo;

import java.util.stream.Collectors;


@Mod(Forgero.NAMESPACE)
public class ForgeroInitializer {
    public ForgeroInitializer() {
        IEventBus MOD_BUS = FMLJavaModLoadingContext.get().getModEventBus();

        var dependencies = ModList.get().getMods().stream().map(IModInfo::getModId).collect(Collectors.toSet());
        Stopwatch timer = Stopwatch.createStarted();
        var pipeline = PipelineBuilder
                .builder()
                .register(ForgeroSettings.SETTINGS)
                .register(dependencies)
                .register(new ForgePackFinder(dependencies))
                .state(ForgeroStateRegistry.stateListener())
                .state(ForgeroStateRegistry.compositeListener())
                .createStates(ForgeroStateRegistry.createStateListener())
                .inflated(ForgeroStateRegistry.constructListener())
                .inflated(ForgeroStateRegistry.containerListener())
                .recipes(ForgeroStateRegistry.recipeListener())
                .build();


        pipeline.execute();
        Forgero.LOGGER.info("Total load time: " + timer.stop());
    }
}