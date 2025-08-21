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
import com.sigmundgranaas.forgero.smithing.block.ModBlocks;
import com.sigmundgranaas.forgero.smithing.block.entity.ModBlockEntities;
import com.sigmundgranaas.forgero.smithing.block.renderer.BellowsBlockEntityRenderer;
import com.sigmundgranaas.forgero.smithing.block.renderer.HearthBlockEntityRenderer;
import com.sigmundgranaas.forgero.smithing.block.renderer.MoldBlockEntityRenderer;
import com.sigmundgranaas.forgero.smithing.block.renderer.SmithingAnvilBlockEntityRenderer;
import com.sigmundgranaas.forgero.smithing.networking.C2S.AnvilUseC2SPacket;
import com.sigmundgranaas.forgero.smithing.model.BellowsModel;
import com.sigmundgranaas.forgero.smithing.networking.ModMessages;
import com.sigmundgranaas.forgero.smithing.temperature.TemperatureColorProvider;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;

public class ForgeroClientSmithingInitializer implements ClientModInitializer {
    private static com.sigmundgranaas.forgero.core.texture.V2.TextureService textureService;

    public static com.sigmundgranaas.forgero.core.texture.V2.TextureService getTextureService() {
        return textureService;
    }

    // --- Smithing Anvil highlight overlay tracking ---
    public static net.minecraft.util.math.BlockPos hoveredAnvilPos = null;
    public static double hoveredLocalX = 0;
    public static double hoveredLocalZ = 0;
    // --- End highlight overlay tracking ---

    @Override
    public void onInitializeClient() {
        AnvilUseC2SPacket.register();
		        // Make sure MOLD is registered (null check)
		        if (ModBlockEntities.MOLD == null) {
		            ModBlockEntities.rebuildMoldBlockEntityType();

		            // If still null after rebuilding, log error and skip registration
		            if (ModBlockEntities.MOLD == null) {
		                // Continue with other initializations, skipping the MOLD renderer
		            }
		        }
		BlockEntityRendererFactories.register(ModBlockEntities.BELLOWS, BellowsBlockEntityRenderer::new);
		EntityModelLayerRegistry.registerModelLayer(BellowsModel.LAYER_LOCATION, BellowsModel::getTexturedModelData);

		// Register block entity renderers
		BlockEntityRendererRegistry.register(ModBlockEntities.SMITHING_ANVIL, SmithingAnvilBlockEntityRenderer::new);
		BlockEntityRendererRegistry.register(ModBlockEntities.HEARTH, HearthBlockEntityRenderer::new);
		BlockEntityRendererRegistry.register(ModBlockEntities.MOLD, MoldBlockEntityRenderer::new);

		BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.HEARTH, RenderLayer.getCutout());

		ModMessages.registerS2CPackets();


		// Initialize and store the TextureService and TextureGenerator for smithing
        var textureGenerator = com.sigmundgranaas.forgero.core.texture.V2.TextureGenerator.getInstance(new FileService(), ForgeroClient.PALETTE_REMAP);
        textureService = textureGenerator.getService();
        // Pass textureGenerator to MoldBlockEntityRenderer if needed (e.g., via static setter or context)

        // Ensure grayscale fluid texture is present in generated assets for palette variants
        ensureGrayscaleFluidTexture();

        // Register temperature-based color provider for all tool part head/part items
        TemperatureColorProvider.register();

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
