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
import com.sigmundgranaas.forgero.smithing.block.entity.ModBlockEntities;
import com.sigmundgranaas.forgero.smithing.block.renderer.MoldBlockEntityRenderer;
import com.sigmundgranaas.forgero.smithing.block.renderer.SmithingAnvilBlockEntityRenderer;
import com.sigmundgranaas.forgero.smithing.networking.ModMessages;
import com.sigmundgranaas.forgero.smithing.screen.BloomeryScreen;
import com.sigmundgranaas.forgero.smithing.screen.ModScreenHandlers;

import net.minecraft.client.gui.screen.ingame.HandledScreens;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;

public class ForgeroClientSmithingInitializer implements ClientModInitializer {
    private static com.sigmundgranaas.forgero.core.texture.V2.TextureService textureService;

    public static com.sigmundgranaas.forgero.core.texture.V2.TextureService getTextureService() {
        return textureService;
    }

    @Override
    public void onInitializeClient() {
        // Register block entity renderers
        BlockEntityRendererRegistry.register(ModBlockEntities.SMITHING_ANVIL, SmithingAnvilBlockEntityRenderer::new);
		BlockEntityRendererRegistry.register(ModBlockEntities.MOLD, MoldBlockEntityRenderer::new);
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
