package com.sigmundgranaas.forgero;

import com.sigmundgranaas.forgero.command.CommandRegistry;
import com.sigmundgranaas.forgero.item.StateToItemConverter;
import com.sigmundgranaas.forgero.loot.TreasureInjector;
import com.sigmundgranaas.forgero.property.AttributeType;
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
import com.sigmundgranaas.forgero.state.State;
import com.sigmundgranaas.forgero.type.Type;
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

import java.util.HashMap;
import java.util.Map;

import static com.sigmundgranaas.forgero.identifier.Common.ELEMENT_SEPARATOR;


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
                .recipes(ForgeroStateRegistry.recipeListener())
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
        var sortingMap = new HashMap<String, Integer>();

        ForgeroStateRegistry.STATES.all().stream().filter(state -> !state.test(Type.WEAPON) && !state.test(Type.TOOL)).forEach(state -> sortingMap.compute(materialName(state), (key, value) -> value == null || rarity(state) > value ? rarity(state) : value));

        ForgeroStateRegistry.STATES.all().stream()
                .filter(state -> !Registry.ITEM.containsId(new Identifier(ForgeroStateRegistry.STATE_TO_CONTAINER.get(state.identifier()))))
                .sorted((element1, element2) -> compareStates(element1, element2, sortingMap))
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

    private int getOrderingFromState(Map<String, Integer> map, State state) {
        var name = materialName(state);
        int rarity = (int) state.stream().applyAttribute(AttributeType.RARITY);
        return map.getOrDefault(name, rarity);
    }

    private String materialName(State state) {
        var elements = state.name().split(ELEMENT_SEPARATOR);
        if (elements.length > 1) {
            return elements[0];
        } else {
            return state.name();
        }
    }

    private int compareStates(State element1, State element2, Map<String, Integer> map) {


        int elementOrdering = getOrderingFromState(map, element1) - getOrderingFromState(map, element2);
        int nameOrdering = materialName(element1).compareTo(materialName(element2));

        if (elementOrdering != 0) {
            return elementOrdering;
        } else if (nameOrdering != 0) {
            return nameOrdering;
        } else {
            return rarity(element1) - rarity(element2);
        }
    }

    private int rarity(State state) {
        return (int) state.stream().applyAttribute(AttributeType.RARITY);
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
