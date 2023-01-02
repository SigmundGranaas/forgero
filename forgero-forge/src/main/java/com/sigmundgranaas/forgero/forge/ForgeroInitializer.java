package com.sigmundgranaas.forgero.forge;

import com.google.common.collect.ImmutableSet;

import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.ForgeroStateRegistry;
import com.sigmundgranaas.forgero.core.configuration.BuildableConfiguration;
import com.sigmundgranaas.forgero.core.configuration.ForgeroConfiguration;
import com.sigmundgranaas.forgero.core.property.AttributeType;
import com.sigmundgranaas.forgero.core.resource.PipelineBuilder;
import com.sigmundgranaas.forgero.core.resource.data.v2.ResourceLocator;
import com.sigmundgranaas.forgero.core.resource.data.v2.loading.JsonContentFilter;
import com.sigmundgranaas.forgero.core.resource.data.v2.loading.PathWalker;
import com.sigmundgranaas.forgero.core.settings.ForgeroSettings;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.util.loader.ClassLoaderLoader;
import com.sigmundgranaas.forgero.core.util.loader.PathFinder;
import com.sigmundgranaas.forgero.forge.pack.ForgePackFinder;

import net.minecraft.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.sigmundgranaas.forgero.core.identifier.Common.ELEMENT_SEPARATOR;


@Mod(Forgero.NAMESPACE)
public class ForgeroInitializer {

    public static DeferredRegister<Item> ITEM_REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, Forgero.NAMESPACE);

    public ForgeroInitializer() {
        IEventBus MOD_BUS = FMLJavaModLoadingContext.get().getModEventBus();

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

        ITEM_REGISTRY.register(MOD_BUS);
    }

    private ForgeroConfiguration createConfig() {
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

}