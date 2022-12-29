package com.sigmundgranaas.forgeroforge;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableSet;
import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.ForgeroStateRegistry;
import com.sigmundgranaas.forgero.configuration.BuildableConfiguration;
import com.sigmundgranaas.forgero.configuration.ForgeroConfiguration;
import com.sigmundgranaas.forgero.resource.PipelineBuilder;
import com.sigmundgranaas.forgero.resource.data.v2.ResourceLocator;
import com.sigmundgranaas.forgero.resource.data.v2.loading.JsonContentFilter;
import com.sigmundgranaas.forgero.resource.data.v2.loading.PathWalker;
import com.sigmundgranaas.forgero.settings.ForgeroSettings;
import com.sigmundgranaas.forgero.util.loader.ClassLoaderLoader;
import com.sigmundgranaas.forgero.util.loader.PathFinder;
import com.sigmundgranaas.forgeroforge.pack.ForgePackFinder;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forgespi.language.IModInfo;

import java.util.Set;
import java.util.stream.Collectors;


@Mod(Forgero.NAMESPACE)
public class ForgeroInitializer {
    public ForgeroInitializer() {
        IEventBus MOD_BUS = FMLJavaModLoadingContext.get().getModEventBus();

        Stopwatch timer = Stopwatch.createStarted();
        var pipeline = PipelineBuilder
                .builder()
                .register(this::createConfig)
                .register(new ForgePackFinder(createConfig()))
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

    ForgeroConfiguration createConfig() {
        Set<String> dependencies = ModList.get().getMods().stream().map(IModInfo::getModId).collect(Collectors.toSet());
        ResourceLocator locator = PathWalker.builder()
                .contentFilter(new JsonContentFilter())
                .depth(20)
                .pathFinder(PathFinder::ClassLoaderFinder)
                .build();

        return BuildableConfiguration.builder()
                .locator(locator)
                .finder(PathFinder::ClassLoaderFinder)
                .loader(new ClassLoaderLoader())
                .settings(ForgeroSettings.SETTINGS)
                .availableDependencies(ImmutableSet.copyOf(dependencies))
                .build();
    }
}