package com.sigmundgranaas.forgero.smithing;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.fabric.client.ForgeroClient;
import com.sigmundgranaas.forgero.fabric.resources.FileService;
import com.sigmundgranaas.forgero.minecraft.common.item.StateItem;
import com.sigmundgranaas.forgero.smithing.block.entity.ModBlockEntities;
import com.sigmundgranaas.forgero.smithing.block.renderer.MoldBlockEntityRenderer;
import com.sigmundgranaas.forgero.smithing.block.renderer.SmithingAnvilBlockEntityRenderer;
import com.sigmundgranaas.forgero.smithing.networking.ModMessages;
import com.sigmundgranaas.forgero.smithing.screen.BloomeryScreen;
import com.sigmundgranaas.forgero.smithing.screen.ModScreenHandlers;
import com.sigmundgranaas.forgero.smithing.temperature.TemperatureUtils;
import com.sigmundgranaas.forgero.smithing.util.ToolPartTypeUtils;

import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.registry.Registries;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;

public class ForgeroClientSmithingInitializer implements ClientModInitializer {
    private static com.sigmundgranaas.forgero.core.texture.V2.TextureService textureService;

    public static com.sigmundgranaas.forgero.core.texture.V2.TextureService getTextureService() {
        return textureService;
    }

    @Override
    public void onInitializeClient() {
		        // Make sure MOLD is registered (null check)
		        if (ModBlockEntities.MOLD == null) {
		            Forgero.LOGGER.warn("MOLD BlockEntityType is null, rebuilding it...");
		            ModBlockEntities.rebuildMoldBlockEntityType();

		            // If still null after rebuilding, log error and skip registration
		            if (ModBlockEntities.MOLD == null) {
		                Forgero.LOGGER.error("Failed to rebuild MOLD BlockEntityType, renderer will not be registered");
		                // Continue with other initializations, skipping the MOLD renderer
		            }
		        }

		        // Register block entity renderers
		        BlockEntityRendererRegistry.register(ModBlockEntities.SMITHING_ANVIL, SmithingAnvilBlockEntityRenderer::new);
		// Only register MOLD renderer if the BlockEntityType is not null
		if (ModBlockEntities.MOLD != null) {
		    BlockEntityRendererRegistry.register(ModBlockEntities.MOLD, MoldBlockEntityRenderer::new);
		}
        // Register screens
        HandledScreens.register(ModScreenHandlers.BLOOMERY_SCREEN_HANDLER, BloomeryScreen::new);
        
        // Register network messages
        ModMessages.registerS2CPackets();
        
        // Initialize and store the TextureService and TextureGenerator for smithing
        var textureGenerator = com.sigmundgranaas.forgero.core.texture.V2.TextureGenerator.getInstance(new FileService(), ForgeroClient.PALETTE_REMAP);
        textureService = textureGenerator.getService();
        // Pass textureGenerator to MoldBlockEntityRenderer if needed (e.g., via static setter or context)

        // Ensure grayscale fluid texture is present in generated assets for palette variants
        ensureGrayscaleFluidTexture();

        // Register temperature-based color provider for all tool part head/part items
        Registries.ITEM.forEach(item -> {
            if (item instanceof StateItem stateItem) {
                var type = stateItem.defaultState().type();
                if (ToolPartTypeUtils.isToolPartHeadOrToolPart(type)) {
                    ColorProviderRegistry.ITEM.register((stack, tintIndex) -> {
                        int temp = TemperatureUtils.getTemperature(stack);
                        float normalized = (float)(temp - TemperatureUtils.MIN_TEMPERATURE) / (TemperatureUtils.MAX_TEMPERATURE - TemperatureUtils.MIN_TEMPERATURE);
                        return getHeatColor(normalized);
                    }, item);
                }
            }
        });
    }

    // Utility to interpolate between gray, orange, and red based on normalized temperature
    private static int getHeatColor(float normalized) {
        normalized = Math.max(0, Math.min(1, normalized));
        int cold = 0xCCCCCC;
        int hot = 0xFF6600;
        int veryHot = 0xFF0000;
        if (normalized < 0.7f) {
            return lerpColor(cold, hot, normalized / 0.7f);
        } else {
            return lerpColor(hot, veryHot, (normalized - 0.7f) / 0.3f);
        }
    }

    // Linear interpolation between two RGB colors
    private static int lerpColor(int colorA, int colorB, float t) {
        int aR = (colorA >> 16) & 0xFF;
        int aG = (colorA >> 8) & 0xFF;
        int aB = colorA & 0xFF;
        int bR = (colorB >> 16) & 0xFF;
        int bG = (colorB >> 8) & 0xFF;
        int bB = colorB & 0xFF;
        int r = (int)(aR + (bR - aR) * t);
        int g = (int)(aG + (bG - aG) * t);
        int b = (int)(aB + (bB - aB) * t);
        return (r << 16) | (g << 8) | b;
    }
    
    private void ensureGrayscaleFluidTexture() {
        try {
            String resourcePath = "/assets/forgero/textures/block/fluid.png";
            Path outputPath = Paths.get(System.getProperty("user.dir"), "generated", "assets", "forgero", "textures", "block", "fluid.png");
            if (!Files.exists(outputPath)) {
                InputStream stream = getClass().getResourceAsStream(resourcePath);
                if (stream != null) {
                    BufferedImage img = ImageIO.read(stream);
                    Files.createDirectories(outputPath.getParent());
                    ImageIO.write(img, "PNG", outputPath.toFile());
                    Forgero.LOGGER.info("Copied grayscale fluid texture to: {}", outputPath.toAbsolutePath());
                } else {
                    Forgero.LOGGER.warn("Could not find grayscale fluid texture at: {}", resourcePath);
                }
            }
        } catch (Exception e) {
            Forgero.LOGGER.error("Failed to ensure grayscale fluid texture: {}", e.getMessage(), e);
        }
    }
}
