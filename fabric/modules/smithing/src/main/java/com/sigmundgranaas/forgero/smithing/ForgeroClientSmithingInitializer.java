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
import com.sigmundgranaas.forgero.smithing.block.renderer.BellowsBlockEntityRenderer;
import com.sigmundgranaas.forgero.smithing.block.renderer.BloomeryBlockEntityRenderer;
import com.sigmundgranaas.forgero.smithing.block.renderer.BloomeryExtensionBlockEntityRenderer;
import com.sigmundgranaas.forgero.smithing.block.renderer.MoldBlockEntityRenderer;
import com.sigmundgranaas.forgero.smithing.block.renderer.SmithingAnvilBlockEntityRenderer;
import com.sigmundgranaas.forgero.smithing.model.BellowsModel;
import com.sigmundgranaas.forgero.smithing.networking.ModMessages;
import com.sigmundgranaas.forgero.smithing.temperature.TemperatureColorProvider;

import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;

import net.fabricmc.api.ClientModInitializer;
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
		BlockEntityRendererFactories.register(ModBlockEntities.BELLOWS, BellowsBlockEntityRenderer::new);
		EntityModelLayerRegistry.registerModelLayer(BellowsModel.LAYER_LOCATION, BellowsModel::getTexturedModelData);

		        // Register block entity renderers
		        BlockEntityRendererRegistry.register(ModBlockEntities.SMITHING_ANVIL, SmithingAnvilBlockEntityRenderer::new);
		        BlockEntityRendererRegistry.register(ModBlockEntities.BLOOMERY, BloomeryBlockEntityRenderer::new);
		        BlockEntityRendererRegistry.register(ModBlockEntities.BLOOMERY_EXTENSION, BloomeryExtensionBlockEntityRenderer::new);
		// Only register MOLD renderer if the BlockEntityType is not null
		if (ModBlockEntities.MOLD != null) {
		    BlockEntityRendererRegistry.register(ModBlockEntities.MOLD, MoldBlockEntityRenderer::new);
		}

        // Register network messages
        ModMessages.registerS2CPackets();
        
        // Initialize and store the TextureService and TextureGenerator for smithing
        var textureGenerator = com.sigmundgranaas.forgero.core.texture.V2.TextureGenerator.getInstance(new FileService(), ForgeroClient.PALETTE_REMAP);
        textureService = textureGenerator.getService();
        // Pass textureGenerator to MoldBlockEntityRenderer if needed (e.g., via static setter or context)

        // Ensure grayscale fluid texture is present in generated assets for palette variants
        ensureGrayscaleFluidTexture();

        // Register temperature-based color provider for all tool part head/part items
        TemperatureColorProvider.register();

        // Register highlight overlay tracking for smithing anvil
        net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents.BLOCK_OUTLINE.register((context, blockOutline) -> {
            net.minecraft.client.MinecraftClient client = net.minecraft.client.MinecraftClient.getInstance();
            hoveredAnvilPos = null;
            if (client.crosshairTarget instanceof net.minecraft.util.hit.BlockHitResult hit) {
                net.minecraft.util.math.BlockPos pos = hit.getBlockPos();
                if (context.world().getBlockState(pos).getBlock() instanceof com.sigmundgranaas.forgero.smithing.block.custom.SmithingAnvil) {
                    net.minecraft.item.ItemStack mainHand = client.player.getStackInHand(net.minecraft.util.Hand.MAIN_HAND);
                    if (mainHand.getItem().getTranslationKey().contains("smithing_hammer")) {
                        net.minecraft.util.math.Vec3d hitPos = hit.getPos();
                        hoveredAnvilPos = pos;
                        hoveredLocalX = hitPos.x - pos.getX();
                        hoveredLocalZ = hitPos.z - pos.getZ();
                    }
                }
            }
            return true;
        });
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
