package com.sigmundgranaas.forgero.smithing;

import java.util.Map;

import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.texture.V2.Palette;
import com.sigmundgranaas.forgero.core.texture.V2.TextureGenerator;
import com.sigmundgranaas.forgero.fabric.client.ForgeroClient;
import com.sigmundgranaas.forgero.fabric.resources.FileService;
import com.sigmundgranaas.forgero.smithing.block.entity.ModBlockEntities;
import com.sigmundgranaas.forgero.smithing.block.renderer.MoldBlockEntityRenderer;
import com.sigmundgranaas.forgero.smithing.block.renderer.SmithingAnvilBlockEntityRenderer;
import com.sigmundgranaas.forgero.smithing.networking.ModMessages;
import com.sigmundgranaas.forgero.smithing.resource.FluidTextureGenerator;
import com.sigmundgranaas.forgero.smithing.screen.BloomeryScreen;
import com.sigmundgranaas.forgero.smithing.screen.ModScreenHandlers;

import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.resource.ResourceType;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;

public class ForgeroClientSmithingInitializer implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Register block entity renderers
        BlockEntityRendererRegistry.register(ModBlockEntities.SMITHING_ANVIL, SmithingAnvilBlockEntityRenderer::new);
		BlockEntityRendererRegistry.register(ModBlockEntities.MOLD, MoldBlockEntityRenderer::new);
        // Register screens
        HandledScreens.register(ModScreenHandlers.BLOOMERY_SCREEN_HANDLER, BloomeryScreen::new);
        
        // Register network messages
        ModMessages.registerS2CPackets();
        
        // Register fluid texture generator
        registerFluidTextureGenerator();
    }
    
    private void registerFluidTextureGenerator() {
        try {
            // Get the texture generator instance with the same parameters as in ForgeroClient
            var textureGenerator = TextureGenerator.getInstance(new FileService(), ForgeroClient.PALETTE_REMAP);
            
            // Create the fluid texture generator
            FluidTextureGenerator fluidTextureGenerator = new FluidTextureGenerator(textureGenerator);
            
            // Register the fluid texture generator
            ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(fluidTextureGenerator);
            
            // Log palette information
            Map<String, Palette> palettes = textureGenerator.getPaletteMap();
            Forgero.LOGGER.info("Found {} palettes for fluid texture generation", palettes.size());
            
            // Log the palette names for debugging
            if (!palettes.isEmpty()) {
                Forgero.LOGGER.debug("Available palettes: {}", String.join(", ", palettes.keySet()));
            } else {
                Forgero.LOGGER.warn("No palettes found in TextureGenerator. Fluid textures may not be generated correctly.");
            }
            
            Forgero.LOGGER.info("Successfully registered FluidTextureGenerator");
        } catch (Exception e) {
            Forgero.LOGGER.error("Failed to initialize FluidTextureGenerator: {}", e.getMessage(), e);
        }
    }
}
