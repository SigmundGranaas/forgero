package com.sigmundgranaas.forgero.smithing;

import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.texture.V2.TextureGenerator;
import com.sigmundgranaas.forgero.fabric.client.ForgeroClient;
import com.sigmundgranaas.forgero.fabric.resources.FileService;
import com.sigmundgranaas.forgero.smithing.block.entity.ModBlockEntities;
import com.sigmundgranaas.forgero.smithing.block.renderer.SmithingAnvilBlockEntityRenderer;
import com.sigmundgranaas.forgero.smithing.networking.ModMessages;
import com.sigmundgranaas.forgero.smithing.resource.FluidTextureGenerator;
import com.sigmundgranaas.forgero.smithing.screen.BloomeryScreen;
import com.sigmundgranaas.forgero.smithing.screen.ModScreenHandlers;

import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

public class ForgeroClientSmithingInitializer implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Register block entity renderers
        BlockEntityRendererRegistry.register(ModBlockEntities.SMITHING_ANVIL, SmithingAnvilBlockEntityRenderer::new);
        
        // Register screens
        HandledScreens.register(ModScreenHandlers.BLOOMERY_SCREEN_HANDLER, BloomeryScreen::new);
        
        // Register network messages
        ModMessages.registerS2CPackets();
        
        // Register fluid texture generator
        registerFluidTextureGenerator();
    }
    
    private void registerFluidTextureGenerator() {
        // Get the texture generator instance with the same parameters as in ForgeroClient
        var textureGenerator = TextureGenerator.getInstance(new FileService(), ForgeroClient.PALETTE_REMAP);
        
        // Create and register the fluid texture generator
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(
            new FluidTextureGenerator(textureGenerator)
        );
        
        Forgero.LOGGER.info("Registered FluidTextureGenerator");
    }
}
