package com.sigmundgranaas.forgero.smithing.block.renderer;

import java.awt.image.BufferedImage;
import java.io.InputStream;

import javax.imageio.ImageIO;

import com.sigmundgranaas.forgero.smithing.block.custom.MoldBlock;
import com.sigmundgranaas.forgero.smithing.block.entity.MoldBlockEntity;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class MoldBlockEntityRenderer implements BlockEntityRenderer<MoldBlockEntity> {
    private static final SpriteIdentifier LAVA_TEXTURE = new SpriteIdentifier(
            SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE,
            new Identifier("block/lava_still")
    );
    private static final SpriteIdentifier LAVA_FLOW_TEXTURE = new SpriteIdentifier(
            SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE,
            new Identifier("block/lava_flow")
    );

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
            "/assets/forgero/templates/textures/main/" + templateName + ".png",
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

    @Override
    public void render(MoldBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (!entity.getCachedState().get(MoldBlock.FILLED)) {
            return;
        }

        BlockState state = entity.getCachedState();
        float progress = entity.getProgress() / 100f;

        // Fixed height for the liquid (5 pixels high)
        float minY = 0.05f;
        float maxY = minY + (5.0f / 16.0f);

        // Get template name and image
        String templateName = getTemplateName(entity);
        BufferedImage template = getTemplateImage(templateName);

        // Prepare colored pixel mask
        boolean[][] coloredPixels = new boolean[16][16];
        int minX = 16, minZ = 16, maxX = -1, maxZ = -1;
        if (template != null) {
            for (int x = 0; x < Math.min(16, template.getWidth()); x++) {
                for (int z = 0; z < Math.min(16, template.getHeight()); z++) {
                    int pixel = template.getRGB(x, z);
                    int alpha = (pixel >> 24) & 0xff;
                    if (alpha > 10) {
                        coloredPixels[x][z] = true;
                        if (x < minX) minX = x;
                        if (x > maxX) maxX = x;
                        if (z < minZ) minZ = z;
                        if (z > maxZ) maxZ = z;
                    }
                }
            }
        }
        // Fallback to a default region if no template or no colored pixels
        if (minX > maxX || minZ > maxZ) {
            minX = 3; maxX = 12; minZ = 3; maxZ = 12;
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    coloredPixels[x][z] = true;
                }
            }
        }

        // Texture and color setup
        float heat = 1.0f - progress;
        float red = 0.9f + (heat * 0.1f);
        float green = 0.3f + (heat * 0.3f);
        float blue = 0.1f;
        float alpha = 0.8f + (heat * 0.2f);

        Sprite lavaSprite = LAVA_FLOW_TEXTURE.getSprite();
        float minU = lavaSprite.getMinU();
        float maxU = lavaSprite.getMaxU();
        float minV = lavaSprite.getMinV();
        float maxV = lavaSprite.getMaxV();

        VertexConsumer vertexConsumer = LAVA_FLOW_TEXTURE.getVertexConsumer(
                vertexConsumers,
                RenderLayer::getEntityTranslucent
        );

        matrices.push();
        MatrixStack.Entry entry = matrices.peek();

        // Map the full texture to the bounds of the colored region
        float uSpan = maxU - minU;
        float vSpan = maxV - minV;

        // For each colored pixel, render a 1x1 "voxel" quad at maxY
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

                // Top face (up)
                vertexConsumer.vertex(entry.getPositionMatrix(), fx0, maxY, fz0)
                        .color(red, green, blue, alpha)
                        .texture(u0, v0)
                        .overlay(overlay)
                        .light(light)
                        .normal(entry.getNormalMatrix(), 0, 1, 0)
                        .next();

                vertexConsumer.vertex(entry.getPositionMatrix(), fx1, maxY, fz0)
                        .color(red, green, blue, alpha)
                        .texture(u1, v0)
                        .overlay(overlay)
                        .light(light)
                        .normal(entry.getNormalMatrix(), 0, 1, 0)
                        .next();

                vertexConsumer.vertex(entry.getPositionMatrix(), fx1, maxY, fz1)
                        .color(red, green, blue, alpha)
                        .texture(u1, v1)
                        .overlay(overlay)
                        .light(light)
                        .normal(entry.getNormalMatrix(), 0, 1, 0)
                        .next();

                vertexConsumer.vertex(entry.getPositionMatrix(), fx0, maxY, fz1)
                        .color(red, green, blue, alpha)
                        .texture(u0, v1)
                        .overlay(overlay)
                        .light(light)
                        .normal(entry.getNormalMatrix(), 0, 1, 0)
                        .next();
            }
        }

        matrices.pop();
    }

    @Override
    public int getRenderDistance() {
        return 64;
    }
}
