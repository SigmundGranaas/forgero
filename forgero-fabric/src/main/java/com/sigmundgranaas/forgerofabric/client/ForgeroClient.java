package com.sigmundgranaas.forgerofabric.client;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.ForgeroStateRegistry;
import com.sigmundgranaas.forgero.model.ModelRegistry;
import com.sigmundgranaas.forgero.model.PaletteTemplateModel;
import com.sigmundgranaas.forgero.resource.PipelineBuilder;
import com.sigmundgranaas.forgero.settings.ForgeroSettings;
import com.sigmundgranaas.forgero.state.State;
import com.sigmundgranaas.forgero.texture.V2.TextureGenerator;
import com.sigmundgranaas.forgero.type.Type;
import com.sigmundgranaas.forgerocommon.block.assemblystation.AssemblyStationScreen;
import com.sigmundgranaas.forgerofabric.client.model.ForgeroModelVariantProvider;
import com.sigmundgranaas.forgerofabric.resources.FabricPackFinder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.sigmundgranaas.forgerocommon.block.assemblystation.AssemblyStationScreenHandler.ASSEMBLY_STATION_SCREEN_HANDLER;

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
                .register(availableDependencies)
                .register(ForgeroSettings.SETTINGS)
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
        TEXTURES.values().forEach(texture -> {
            ClientSpriteRegistryCallback.event(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).register((atlasTexture, atlasRegistry) -> atlasRegistry.register(new Identifier(texture.nameSpace(), "item/" + texture.name().replace(".png", ""))));
        });
        ClientSpriteRegistryCallback.event(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).register((atlasTexture, atlasRegistry) -> atlasRegistry.register(new Identifier(Forgero.NAMESPACE, "item/" + "repair_kit_leather_base")));
        ClientSpriteRegistryCallback.event(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).register((atlasTexture, atlasRegistry) -> atlasRegistry.register(new Identifier(Forgero.NAMESPACE, "item/" + "repair_kit_needle_base")));
    }

    private void assetReloader() {
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public void reload(ResourceManager manager) {
                TextureGenerator.INSTANCE.clear();
            }

            @Override
            public Identifier getFabricId() {
                return new Identifier(Forgero.NAMESPACE, "dynamic_textures");
            }
        });
    }
}
