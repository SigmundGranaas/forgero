package com.sigmundgranaas.forgero.fabric.client;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.ForgeroStateRegistry;
import com.sigmundgranaas.forgero.core.configuration.BuildableConfiguration;
import com.sigmundgranaas.forgero.core.model.ModelRegistry;
import com.sigmundgranaas.forgero.core.model.PaletteTemplateModel;
import com.sigmundgranaas.forgero.core.resource.PipelineBuilder;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.texture.V2.TextureGenerator;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.fabric.client.model.ForgeroModelVariantProvider;
import com.sigmundgranaas.forgero.fabric.client.texture.Generator;
import com.sigmundgranaas.forgero.fabric.resources.FabricPackFinder;
import com.sigmundgranaas.forgero.fabric.resources.FileService;
import com.sigmundgranaas.forgero.minecraft.common.block.assemblystation.AssemblyStationScreen;
import net.devtech.arrp.api.RRPCallback;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.sigmundgranaas.forgero.minecraft.common.block.assemblystation.AssemblyStationScreenHandler.ASSEMBLY_STATION_SCREEN_HANDLER;

@Environment(EnvType.CLIENT)
public class ForgeroClient implements ClientModInitializer {
    public static Map<String, PaletteTemplateModel> TEXTURES = new HashMap<>();

    @Override
    public void onInitializeClient() {
        initializeItemModels();
        HandledScreens.register(ASSEMBLY_STATION_SCREEN_HANDLER, AssemblyStationScreen::new);
    }

    private void initializeItemModels() {
        var modelRegistry = new ModelRegistry();
        var availableDependencies = FabricLoader.getInstance().getAllMods().stream().map(ModContainer::getMetadata).map(ModMetadata::getId).collect(Collectors.toSet());

        PipelineBuilder
                .builder()
                .register(() -> BuildableConfiguration.builder().availableDependencies(ImmutableSet.copyOf(availableDependencies)).build())
                .register(FabricPackFinder.supplier())
                .data(modelRegistry.paletteListener())
                .data(modelRegistry.modelListener())
                .build()
                .execute();
        assetReloader();
        registerToolPartTextures(modelRegistry);
        var modelProvider = new ForgeroModelVariantProvider(modelRegistry);
        ModelLoadingRegistry.INSTANCE.registerVariantProvider(variant -> modelProvider);
    }

    private void registerToolPartTextures(ModelRegistry modelRegistry) {
        var materials = ForgeroStateRegistry.TREE.find(Type.TOOL_MATERIAL)
                .map(node -> node.getResources(State.class))
                .orElse(ImmutableList.<State>builder().build());
        for (State material : materials) {
            ForgeroClient.TEXTURES.put(String.format("forgero:%s-repair_kit.png", material.name()), new PaletteTemplateModel(material.name(), "repair_kit.png", 30, null));
        }

        TEXTURES.putAll(modelRegistry.getTextures());
        Generator.generate();
        RRPCallback.BEFORE_VANILLA.register(a -> a.add(Generator.RESOURCE_PACK_CLIENT));

    }

    private void assetReloader() {
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public void reload(ResourceManager manager) {
                TextureGenerator.getInstance(new FileService()).clear();
            }

            @Override
            public Identifier getFabricId() {
                return new Identifier(Forgero.NAMESPACE, "dynamic_textures");
            }
        });
    }
}
