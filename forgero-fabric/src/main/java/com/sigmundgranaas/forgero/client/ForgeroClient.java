package com.sigmundgranaas.forgero.client;

import com.sigmundgranaas.forgero.ForgeroInitializer;
import com.sigmundgranaas.forgero.block.assemblystation.AssemblyStationScreen;
import com.sigmundgranaas.forgero.client.model.ForgeroModelVariantProvider;
import com.sigmundgranaas.forgero.model.ModelRegistry;
import com.sigmundgranaas.forgero.model.PaletteTemplateModel;
import com.sigmundgranaas.forgero.resource.PipelineBuilder;
import com.sigmundgranaas.forgero.resources.FabricPackFinder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

import static com.sigmundgranaas.forgero.block.assemblystation.AssemblyStationScreenHandler.ASSEMBLY_STATION_SCREEN_HANDLER;

@Environment(EnvType.CLIENT)
public class ForgeroClient implements ClientModInitializer {
    public static final Logger LOGGER = LogManager.getLogger(ForgeroInitializer.MOD_NAMESPACE);

    public static Map<String, PaletteTemplateModel> TEXTURES = new HashMap<>();

    @Override
    public void onInitializeClient() {
        initializeItemModels();
        HandledScreens.register(ASSEMBLY_STATION_SCREEN_HANDLER, AssemblyStationScreen::new);
    }

    private void initializeItemModels() {
        var modelRegistry = new ModelRegistry();
        PipelineBuilder
                .builder()
                .register(FabricPackFinder.supplier())
                .data(modelRegistry.paletteListener())
                .data(modelRegistry.modelListener())
                .build()
                .execute();

        registerToolPartTextures(modelRegistry);
        var modelProvider = new ForgeroModelVariantProvider(modelRegistry);
        ModelLoadingRegistry.INSTANCE.registerVariantProvider(variant -> modelProvider);
    }

    private void registerToolPartTextures(ModelRegistry modelRegistry) {
        TEXTURES = modelRegistry.getTextures();
        TEXTURES.values().forEach(texture -> {
            ClientSpriteRegistryCallback.event(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).register((atlasTexture, atlasRegistry) -> atlasRegistry.register(new Identifier(ForgeroInitializer.MOD_NAMESPACE, "item/" + texture.name().replace(".png", ""))));
        });
    }
}
