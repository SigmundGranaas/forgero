package com.sigmundgranaas.forgero.smithing.block.renderer;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.WeakHashMap;

import javax.imageio.ImageIO;

import com.sigmundgranaas.forgero.core.texture.V2.Palette;
import com.sigmundgranaas.forgero.core.texture.V2.TextureService;
import com.sigmundgranaas.forgero.core.texture.V2.TemplateTexture;
import com.sigmundgranaas.forgero.core.texture.V2.recolor.DefaultRecolorStrategy;
import com.sigmundgranaas.forgero.core.texture.utils.RgbColour;
import com.sigmundgranaas.forgero.smithing.ForgeroClientSmithingInitializer;
import com.sigmundgranaas.forgero.smithing.block.custom.MoldBlock;
import com.sigmundgranaas.forgero.smithing.block.entity.MoldBlockEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class MoldBlockEntityRenderer implements BlockEntityRenderer<MoldBlockEntity> {


    private static final SpriteIdentifier FLUID_TEXTURE = new SpriteIdentifier(
            SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE,
            new Identifier("forgero", "block/fluid")
    );

    // Track displayed progress for smooth cooling interpolation
    private final WeakHashMap<MoldBlockEntity, Float> displayedProgressMap = new WeakHashMap<>();

    // Cache for colored textures to avoid regenerating every frame
    private final WeakHashMap<String, NativeImageBackedTexture> coloredFluidTextureCache = new WeakHashMap<>();

    private static final Logger LOGGER = LogManager.getLogger("MoldBlockEntityRenderer");

    public MoldBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
    }

    // Helper to get the template name from the block registry name (e.g., axe_head_mold -> axe_head)
    private String getTemplateName(MoldBlockEntity entity) {
        BlockState state = entity.getCachedState();
        Identifier id = net.minecraft.registry.Registries.BLOCK.getId(state.getBlock());
        String path = id.getPath();
        if (path.endsWith("_mold")) {
            return path.substring(0, path.length() - "_mold".length());
        }
        return path;
    }

    // Helper to load the template PNG as a BufferedImage (from resources)
    private BufferedImage getTemplateImage(String templateName) {
        String[] resourcePaths = {
            "/assets/forgero/templates/textures/smithing/" + templateName + ".png",
            "/assets/forgero/textures/templates/main/" + templateName + ".png"
        };
        BufferedImage img = null;
        for (String path : resourcePaths) {
            try (InputStream is = MoldBlockEntityRenderer.class.getResourceAsStream(path)) {
                if (is != null) {
                    img = ImageIO.read(is);
                    break;
                }
            } catch (Exception ignored) {}
        }
        return img;
    }

    // Helper to extract the palette name from the fluid in the mold
    private String getFluidPaletteName(MoldBlockEntity entity) {
        if (entity == null) {
            return "iron";
        }

        Identifier fluidId = entity.getLiquid();
        if (fluidId != null) {
            String path = fluidId.getPath(); // e.g., "molten_iron"
            if (path.startsWith("molten_")) {
                String palette = path.substring("molten_".length());
                return palette != null && !palette.isEmpty() ? palette : "iron"; // e.g., "iron"
            }
        }
        // fallback to "iron"
        return "iron";
    }

    // Helper to colorize the grayscale fluid image with the palette
    private NativeImageBackedTexture getColoredFluidTexture(String paletteName) {
        if (paletteName == null) {
            paletteName = "iron";
        }

        String actualPaletteName = paletteName.endsWith(".png") ? paletteName : paletteName + ".png";
        TextureService textureService = ForgeroClientSmithingInitializer.getTextureService();

        if (textureService == null) {
            return null;
        }

        if (coloredFluidTextureCache.containsKey(actualPaletteName)) {
            return coloredFluidTextureCache.get(actualPaletteName);
        }

        Optional<Palette> paletteOpt = textureService.getPalette(actualPaletteName);
        if (paletteOpt.isEmpty()) {
            return null;
        }
        BufferedImage grayscale = null;
        try {
            String generatedPath = System.getProperty("user.dir") + "/generated/assets/forgero/textures/block/fluid.png";
            java.io.File generatedFile = new java.io.File(generatedPath);
            if (generatedFile.exists()) {
                grayscale = ImageIO.read(generatedFile);
            } else {
                try (InputStream is = MoldBlockEntityRenderer.class.getResourceAsStream("/assets/forgero/textures/block/fluid.png")) {
                    if (is != null) {
                        grayscale = ImageIO.read(is);
                    }
                }
            }
        } catch (Exception e) {
            return null;
        }
        if (grayscale == null) {
            return null;
        }
        // --- Refactored: Use DefaultRecolorStrategy and TemplateTexture ---
        DefaultRecolorStrategy recolorStrategy = new DefaultRecolorStrategy();
        TemplateTexture templateTexture = new TemplateTexture(grayscale, recolorStrategy);
        BufferedImage coloredImage = recolorStrategy.recolor(templateTexture, paletteOpt.get());
        NativeImage colored;
        try (java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream()) {
            javax.imageio.ImageIO.write(coloredImage, "PNG", baos);
            baos.flush();
            colored = NativeImage.read(baos.toByteArray());
        } catch (Exception e) {
            return null;
        }
        NativeImageBackedTexture tex = new NativeImageBackedTexture(colored);
        coloredFluidTextureCache.put(actualPaletteName, tex);
        return tex;
    }

    // Minimal implementation for DefaultRecolorStrategy
    private static class FluidGreyscaleTemplate {
        private final List<RgbColour> greyScaleValues;
        public FluidGreyscaleTemplate(List<RgbColour> greyScaleValues) {
            this.greyScaleValues = greyScaleValues;
        }
        public int getNumberOfGreyScales() {
            return 1;
        }
        public List<RgbColour> getGreyScaleValues(int frameIndex) {
            return greyScaleValues;
        }
    }

    @Override
    public void render(MoldBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (!entity.getCachedState().get(MoldBlock.FILLED)) {
            return;
        }

        // --- Smooth interpolation for cooling when progress >= 0.75 ---
        float targetProgress = entity.getProgress() / 100f;
        float displayedProgress = displayedProgressMap.getOrDefault(entity, targetProgress);

        // If progress >= 0.75, interpolate displayedProgress toward targetProgress (1.0)
        if (targetProgress >= 0.75f) {
            float lerpSpeed = 0.05f; // Lower = slower
            displayedProgress = displayedProgress + (targetProgress - displayedProgress) * lerpSpeed * (1.0f + tickDelta);
        } else {
            // Snap to target if not cooling, or update instantly
            displayedProgress = targetProgress;
        }
        displayedProgressMap.put(entity, displayedProgress);

        // Use displayedProgress instead of progress for rendering
        float progress = displayedProgress;

        float minY = 0.05f;
        float maxY = minY + (1.0f / 16.0f);

        // Get template name and image
        String templateName = getTemplateName(entity);
        BufferedImage template = getTemplateImage(templateName);

        // Prepare colored pixel mask
        boolean[][] coloredPixels = new boolean[16][16];
        int minX = 0, minZ = 0, maxX = 15, maxZ = 15; // Always use full 16x16 template
        if (template != null) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    int pixel = template.getRGB(x, z);
                    int alpha = (pixel >> 24) & 0xff;
                    coloredPixels[x][z] = alpha > 10;
                }
            }
        } else {
            // If no template, fallback to all true (render all)
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    coloredPixels[x][z] = true;
                }
            }
        }

        // --- Blending logic for lava/result transition ---
        float blendStart = 0.60f; // Start blending at 70% (was 0.85)
        float blendEnd = 1.0f;    // End at 100%
        float blendFactor = 0.0f;
        if (progress >= blendStart) {
            blendFactor = Math.min(1.0f, (progress - blendStart) / (blendEnd - blendStart));
        }

        // Texture and color setup for lava
        float heat = 1.0f - progress;
        float red = 0.9f + (heat * 0.1f);
        float green = 0.3f + (heat * 0.3f);
        float blue = 0.1f;
        float lavaAlpha = (0.8f + (heat * 0.2f)) * (1.0f - blendFactor);

        // Get palette name for current fluid/material
        String paletteName = getFluidPaletteName(entity);
        NativeImageBackedTexture coloredFluidTexture = getColoredFluidTexture(paletteName);

        Sprite lavaSprite;
        Identifier dynamicId = null;
        if (coloredFluidTexture != null) {
            // Register the colored texture as a dynamic texture and use its Identifier for rendering
            dynamicId = MinecraftClient.getInstance().getTextureManager().registerDynamicTexture(
                "forgero_fluid_colored_" + paletteName, coloredFluidTexture
            );
            lavaSprite = null;
        } else {
            lavaSprite = FLUID_TEXTURE.getSprite();
        }

        float minU, maxU, minV, maxV;
        Identifier textureId;
        if (lavaSprite != null) {
            minU = lavaSprite.getMinU();
            maxU = lavaSprite.getMaxU();
            minV = lavaSprite.getMinV();
            maxV = lavaSprite.getMaxV();
            textureId = lavaSprite.getAtlasId();
        } else {
            // Full texture UVs for dynamic texture
            minU = 0.0f;
            maxU = 1.0f;
            minV = 0.0f;
            maxV = 1.0f;
            // Use the dynamicId returned by registerDynamicTexture
            textureId = dynamicId;
        }

        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(textureId));

        matrices.push();
        MatrixStack.Entry entry = matrices.peek();

        // Map the full texture to the bounds of the colored region
        float uSpan = maxU - minU;
        float vSpan = maxV - minV;

        // For each colored pixel, render a 1x1 "voxel" quad at maxY (lava, fading out)
        if (lavaAlpha > 0.01f) {
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (!coloredPixels[x][z]) continue;
                    float fx0 = x / 16.0f;
                    float fx1 = (x + 1) / 16.0f;
                    float fz0 = z / 16.0f;
                    float fz1 = (z + 1) / 16.0f;

                    // Calculate UVs so the full texture is mapped over the whole colored region
                    float u0 = minU + uSpan * (x - minX) / (float)(maxX - minX + 1);
                    float u1 = minU + uSpan * (x - minX + 1) / (float)(maxX - minX + 1);
                    float v0 = minV + vSpan * (z - minZ) / (float)(maxZ - minZ + 1);
                    float v1 = minV + vSpan * (z - minZ + 1) / (float)(maxZ - minZ + 1);

                    // Remove tint: use white color and full alpha
                    vertexConsumer.vertex(entry.getPositionMatrix(), fx0, maxY, fz0)
                            .color(1f, 1f, 1f, 1f)
                            .texture(u0, v0)
                            .overlay(overlay)
                            .light(light)
                            .normal(entry.getNormalMatrix(), 0, 1, 0)
                            .next();

                    vertexConsumer.vertex(entry.getPositionMatrix(), fx1, maxY, fz0)
                            .color(1f, 1f, 1f, 1f)
                            .texture(u1, v0)
                            .overlay(overlay)
                            .light(light)
                            .normal(entry.getNormalMatrix(), 0, 1, 0)
                            .next();

                    vertexConsumer.vertex(entry.getPositionMatrix(), fx1, maxY, fz1)
                            .color(1f, 1f, 1f, 1f)
                            .texture(u1, v1)
                            .overlay(overlay)
                            .light(light)
                            .normal(entry.getNormalMatrix(), 0, 1, 0)
                            .next();

                    vertexConsumer.vertex(entry.getPositionMatrix(), fx0, maxY, fz1)
                            .color(1f, 1f, 1f, 1f)
                            .texture(u0, v1)
                            .overlay(overlay)
                            .light(light)
                            .normal(entry.getNormalMatrix(), 0, 1, 0)
                            .next();
                }
            }
        }

        matrices.pop();

        // --- Render the resulting tool/item as a 2D sprite overlay, fading in ---
        ItemStack result = entity.getResult();



        if (!result.isEmpty() && blendFactor > 0.0f) {
            // Get the item sprite
            Sprite itemSprite = MinecraftClient.getInstance()
                .getItemRenderer()
                .getModel(result, entity.getWorld(), null, 0)
                .getParticleSprite();

            float itemAlpha = blendFactor;

            // Use the correct VertexConsumer for the item sprite
            VertexConsumer itemConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(itemSprite.getAtlasId()));

            matrices.push();
            MatrixStack.Entry itemEntry = matrices.peek();

            // Render the item sprite only where the template mask is set, using the original sprite pixel mapping
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (!coloredPixels[x][z]) continue;
                    float fx0 = x / 16.0f;
                    float fx1 = (x + 1) / 16.0f;
                    float fz0 = z / 16.0f;
                    float fz1 = (z + 1) / 16.0f;

                    // Map each mold pixel directly to the corresponding item sprite pixel (1:1 mapping)
                    float u0 = itemSprite.getMinU() + (itemSprite.getMaxU() - itemSprite.getMinU()) * x / 16.0f;
                    float u1 = itemSprite.getMinU() + (itemSprite.getMaxU() - itemSprite.getMinU()) * (x + 1) / 16.0f;
                    float v0 = itemSprite.getMinV() + (itemSprite.getMaxV() - itemSprite.getMinV()) * z / 16.0f;
                    float v1 = itemSprite.getMinV() + (itemSprite.getMaxV() - itemSprite.getMinV()) * (z + 1) / 16.0f;

                    itemConsumer.vertex(itemEntry.getPositionMatrix(), fx0, maxY, fz0)
                            .color(1f, 1f, 1f, itemAlpha)
                            .texture(u0, v0)
                            .overlay(overlay)
                            .light(light)
                            .normal(itemEntry.getNormalMatrix(), 0, 1, 0)
                            .next();

                    itemConsumer.vertex(itemEntry.getPositionMatrix(), fx1, maxY, fz0)
                            .color(1f, 1f, 1f, itemAlpha)
                            .texture(u1, v0)
                            .overlay(overlay)
                            .light(light)
                            .normal(itemEntry.getNormalMatrix(), 0, 1, 0)
                            .next();

                    itemConsumer.vertex(itemEntry.getPositionMatrix(), fx1, maxY, fz1)
                            .color(1f, 1f, 1f, itemAlpha)
                            .texture(u1, v1)
                            .overlay(overlay)
                            .light(light)
                            .normal(itemEntry.getNormalMatrix(), 0, 1, 0)
                            .next();

                    itemConsumer.vertex(itemEntry.getPositionMatrix(), fx0, maxY, fz1)
                            .color(1f, 1f, 1f, itemAlpha)
                            .texture(u0, v1)
                            .overlay(overlay)
                            .light(light)
                            .normal(itemEntry.getNormalMatrix(), 0, 1, 0)
                            .next();
                }
            }
            matrices.pop();
        }
    }

    @Override
    public int getRenderDistance() {
        return 64;
    }
}
