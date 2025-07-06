package com.sigmundgranaas.forgero.smithing.block.renderer;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.WeakHashMap;
import javax.imageio.ImageIO;

import com.sigmundgranaas.forgero.core.texture.V2.Palette;
import com.sigmundgranaas.forgero.core.texture.V2.TextureService;
import com.sigmundgranaas.forgero.core.texture.V2.TemplateTexture;
import com.sigmundgranaas.forgero.core.texture.V2.recolor.DefaultRecolorStrategy;
import com.sigmundgranaas.forgero.smithing.ForgeroClientSmithingInitializer;
import com.sigmundgranaas.forgero.smithing.block.custom.MoldBlock;
import com.sigmundgranaas.forgero.smithing.block.entity.MoldBlockEntity;

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
    private final WeakHashMap<MoldBlockEntity, Float> displayedProgressMap = new WeakHashMap<>();
    private final WeakHashMap<String, NativeImageBackedTexture> coloredFluidTextureCache = new WeakHashMap<>();

    public MoldBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {}

    private String getTemplateName(MoldBlockEntity entity) {
        BlockState state = entity.getCachedState();
        Identifier id = net.minecraft.registry.Registries.BLOCK.getId(state.getBlock());
        String path = id.getPath();
        if (path.endsWith("_mold")) {
            return path.substring(0, path.length() - "_mold".length());
        }
        return path;
    }

    // Only use the mold template from /assets/forgero/templates/textures/molds/
    private BufferedImage getMoldTemplateImage(String templateName) {
        String resourcePath = "/assets/forgero/templates/textures/molds/" + templateName + ".png";
        try (InputStream is = MoldBlockEntityRenderer.class.getResourceAsStream(resourcePath)) {
            if (is != null) {
                return ImageIO.read(is);
            }
        } catch (Exception ignored) {}
        return null;
    }

    private String getFluidPaletteName(MoldBlockEntity entity) {
        if (entity == null) {
            return "iron";
        }
        Identifier fluidId = entity.getLiquid();
        if (fluidId != null) {
            String path = fluidId.getPath();
            if (path.startsWith("molten_")) {
                String palette = path.substring("molten_".length());
                return palette != null && !palette.isEmpty() ? palette : "iron";
            }
        }
        return "iron";
    }

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
        java.util.Optional<Palette> paletteOpt = textureService.getPalette(actualPaletteName);
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

    @Override
    public void render(MoldBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (!entity.getCachedState().get(MoldBlock.FILLED)) {
            return;
        }

        float targetProgress = entity.getProgress() / 100f;
        float displayedProgress = displayedProgressMap.getOrDefault(entity, targetProgress);
        if (targetProgress >= 0.75f) {
            float lerpSpeed = 0.05f;
            displayedProgress = displayedProgress + (targetProgress - displayedProgress) * lerpSpeed * (1.0f + tickDelta);
        } else {
            displayedProgress = targetProgress;
        }
        displayedProgressMap.put(entity, displayedProgress);
        float progress = displayedProgress;

        float minY = 0.05f;
        float maxY = minY + (1.0f / 16.0f);

        String templateName = getTemplateName(entity);
        BufferedImage moldTemplate = getMoldTemplateImage(templateName);

        // Only render fluid where the mold template is opaque
        boolean[][] moldMask = new boolean[16][16];
        if (moldTemplate != null) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    int pixel = moldTemplate.getRGB(x, z);
                    int alpha = (pixel >> 24) & 0xff;
                    moldMask[x][z] = alpha > 10;
                }
            }
        } else {
            // If no template, render nothing
            return;
        }

        // --- Centering logic (same as MoldGenerator) ---
        int minX = 16, maxX = -1, minZ = 16, maxZ = -1;
        for (int x = 0; x < 16; x++) for (int z = 0; z < 16; z++) {
            if (moldMask[x][z]) {
                if (x < minX) minX = x;
                if (x > maxX) maxX = x;
                if (z < minZ) minZ = z;
                if (z > maxZ) maxZ = z;
            }
        }
        int dx = 0, dz = 0;
        if (maxX >= minX && maxZ >= minZ) {
            int shapeWidth = maxX - minX + 1, shapeDepth = maxZ - minZ + 1;
            int centerX = minX + shapeWidth / 2, centerZ = minZ + shapeDepth / 2, gridCenter = 8;
            dx = gridCenter - centerX;
            dz = gridCenter - centerZ;
        }
        // -----------------------------------------------

        float blendStart = 0.60f;
        float blendEnd = 1.0f;
        float blendFactor = 0.0f;
        if (progress >= blendStart) {
            blendFactor = Math.min(1.0f, (progress - blendStart) / (blendEnd - blendStart));
        }

        float heat = 1.0f - progress;
        float lavaAlpha = (0.8f + (heat * 0.2f)) * (1.0f - blendFactor);

        String paletteName = getFluidPaletteName(entity);
        NativeImageBackedTexture coloredFluidTexture = getColoredFluidTexture(paletteName);

        Sprite lavaSprite;
        Identifier dynamicId = null;
        if (coloredFluidTexture != null) {
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
            minU = 0.0f;
            maxU = 1.0f;
            minV = 0.0f;
            maxV = 1.0f;
            textureId = dynamicId;
        }

        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(textureId));

        matrices.push();
        MatrixStack.Entry entry = matrices.peek();

        float uSpan = maxU - minU;
        float vSpan = maxV - minV;

        // Only render fluid on mold pixels, centered
        if (lavaAlpha > 0.01f) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    int sx = x + dx, sz = z + dz;
                    if (sx < 0 || sx >= 16 || sz < 0 || sz >= 16) continue;
                    if (!moldMask[x][z]) continue;
                    float fx0 = sx / 16.0f;
                    float fx1 = (sx + 1) / 16.0f;
                    float fz0 = sz / 16.0f;
                    float fz1 = (sz + 1) / 16.0f;

                    float u0 = minU + uSpan * x / 16.0f;
                    float u1 = minU + uSpan * (x + 1) / 16.0f;
                    float v0 = minV + vSpan * z / 16.0f;
                    float v1 = minV + vSpan * (z + 1) / 16.0f;

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

        // Render the resulting tool/item as a 2D sprite overlay, fading in, only on mold pixels, centered
        ItemStack result = entity.getResult();
        if (!result.isEmpty() && blendFactor > 0.0f) {
            Sprite itemSprite = MinecraftClient.getInstance()
                .getItemRenderer()
                .getModel(result, entity.getWorld(), null, 0)
                .getParticleSprite();

            float itemAlpha = blendFactor;
            VertexConsumer itemConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(itemSprite.getAtlasId()));

            matrices.push();
            MatrixStack.Entry itemEntry = matrices.peek();

            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    int sx = x + dx, sz = z + dz;
                    if (sx < 0 || sx >= 16 || sz < 0 || sz >= 16) continue;
                    if (!moldMask[x][z]) continue;
                    float fx0 = sx / 16.0f;
                    float fx1 = (sx + 1) / 16.0f;
                    float fz0 = sz / 16.0f;
                    float fz1 = (sz + 1) / 16.0f;

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
