package com.sigmundgranaas.forgero;

import com.sigmundgranaas.forgero.command.CommandRegistry;
import com.sigmundgranaas.forgero.item.StateToItemConverter;
import com.sigmundgranaas.forgero.loot.TreasureInjector;
import com.sigmundgranaas.forgero.registry.CustomItemRegistry;
import com.sigmundgranaas.forgero.registry.ForgeroItemRegistry;
import com.sigmundgranaas.forgero.registry.RecipeRegistry;
import com.sigmundgranaas.forgero.registry.impl.MineCraftRegistryHandler;
import com.sigmundgranaas.forgero.resource.PipelineBuilder;
import com.sigmundgranaas.forgero.resources.DynamicResourceGenerator;
import com.sigmundgranaas.forgero.resources.FabricPackFinder;
import com.sigmundgranaas.forgero.resources.ModContainerService;
import com.sigmundgranaas.forgero.resources.loader.FabricResourceLoader;
import com.sigmundgranaas.forgero.resources.loader.ReloadableResourceLoader;
import com.sigmundgranaas.forgero.resources.loader.ResourceManagerStreamProvider;
import com.sigmundgranaas.forgero.state.Identifiable;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Comparator;


public class ForgeroInitializer implements ModInitializer {
    public static final String MOD_NAMESPACE = "forgero";
    public static final Logger LOGGER = LogManager.getLogger(ForgeroInitializer.MOD_NAMESPACE);

    @Override
    public void onInitialize() {
        PipelineBuilder
                .builder()
                .register(FabricPackFinder.supplier())
                .state(ForgeroStateRegistry.stateListener())
                .state(ForgeroStateRegistry.compositeListener())
                .inflated(ForgeroStateRegistry.constructListener())
                .inflated(ForgeroStateRegistry.containerListener())
                .build()
                .execute();

        var loader = new FabricResourceLoader(new ModContainerService().getAllModsAsSet());
        var registry = ForgeroItemRegistry.INSTANCE.loadResourcesIfEmpty(loader);
        registry.register(new MineCraftRegistryHandler());
        resourceReloader();
        new CustomItemRegistry().register();
        registerRecipes();
        new CommandRegistry().registerCommand();
        new TreasureInjector().registerLoot();
        new DynamicResourceGenerator().generateResources();

        register();
    }

    private void register() {
        ForgeroStateRegistry.STATES.all().stream()
                .filter(state -> !Registry.ITEM.containsId(new Identifier(ForgeroStateRegistry.STATE_TO_CONTAINER.get(state.identifier()))))
                .sorted(Comparator.comparing(Identifiable::name))
                .forEach(state -> {
                    try {
                        var converter = StateToItemConverter.of(state);
                        Identifier identifier = converter.id();
                        var item = converter.convert();
                        Registry.register(Registry.ITEM, identifier, item);
                    } catch (InvalidIdentifierException e) {
                        LOGGER.error("invalid identifier: {}", state.identifier());
                        LOGGER.error(e);
                    }
                });
    }

    private void registerRecipes() {
        RecipeRegistry.INSTANCE.registerRecipeSerializers();
    }


    private void resourceReloader() {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public void reload(ResourceManager manager) {
                ForgeroItemRegistry.INSTANCE.updateResources(new ReloadableResourceLoader(new ModContainerService().getAllModsAsSet(), new ResourceManagerStreamProvider(manager)));
            }

            @Override
            public Identifier getFabricId() {
                return new Identifier(ForgeroInitializer.MOD_NAMESPACE, "data");
            }
        });
    }
}
