package com.sigmundgranaas.forgero.smithing.block.renderer;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.imageio.ImageIO;

import com.sigmundgranaas.forgero.smithing.block.custom.SmithingAnvil;
import com.sigmundgranaas.forgero.smithing.block.entity.SmithingAnvilBlockEntity;
import com.sigmundgranaas.forgero.smithing.util.TextureCenteringUtil;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class SmithingAnvilBlockEntityRenderer implements BlockEntityRenderer<SmithingAnvilBlockEntity> {
    private final Map<Identifier, int[]> offsetCache = new ConcurrentHashMap<>();

    public SmithingAnvilBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
    }

    @Override
    public void render(SmithingAnvilBlockEntity entity, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light, int overlay) {
        ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();
        ItemStack itemStack = entity.getInventory().getStack(0);

        // Don't render if there's no item
        if (itemStack.isEmpty()) {
            return;
        }

        matrices.push();

        // Position the item on top of the anvil
        matrices.translate(0.23f, 1.025f, 0.5);
        matrices.scale(1.25f, 1.25f, 1.25f);

        // Rotate the item to lay flat on the anvil
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90));

        // Rotate based on anvil facing direction
        switch (entity.getCachedState().get(SmithingAnvil.FACING)) {
            case NORTH -> matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180));
            case EAST -> matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(90));
            case SOUTH -> matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(0));
            case WEST -> matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(270));
        }

        // --- Centering logic start ---
        int[] offset = getItemTextureOffset(itemStack);
        float dx = offset[0] / 16.0f;
        float dz = offset[1] / 16.0f;
        matrices.translate(dx, 0, dz);
        // --- Centering logic end ---

        // Use proper lighting from the block position
        int lightLevel = getLightLevel(entity.getWorld(), entity.getPos());

        itemRenderer.renderItem(itemStack, ModelTransformationMode.GROUND, lightLevel, overlay,
                matrices, vertexConsumers, entity.getWorld(), (int) entity.getPos().asLong());

        matrices.pop();
    }

    private int[] getItemTextureOffset(ItemStack itemStack) {
        Identifier itemId = itemStack.getItem().getRegistryEntry().getKey().get().getValue();
        return offsetCache.computeIfAbsent(itemId, id -> {
            try {
                MinecraftClient client = MinecraftClient.getInstance();
                BakedModel model = client.getItemRenderer().getModel(itemStack, null, null, 0);
                Sprite sprite = model.getParticleSprite();
                Identifier textureId = sprite.getAtlasId();
                // Try to get the actual texture resource for the sprite
                Identifier spriteId = sprite.getContents().getId(); // or sprite.getName() if getContents() is not available
                Identifier resourceId = new Identifier(spriteId.getNamespace(), "textures/" + spriteId.getPath() + ".png");
                BufferedImage image;
                try (InputStream stream = client.getResourceManager().getResource(resourceId).get().getInputStream()) {
                    image = ImageIO.read(stream);
                }
                return TextureCenteringUtil.getTextureCenterOffset(image);
            } catch (Exception e) {
                return new int[] {0, 0};
            }
        });
    }

    private int getLightLevel(World world, BlockPos pos) {
        if (world == null) {
            return 15728880; // Full brightness fallback
        }
        int blockLight = world.getLightLevel(LightType.BLOCK, pos);
        int skyLight = world.getLightLevel(LightType.SKY, pos);
        return LightmapTextureManager.pack(blockLight, skyLight);
    }
}
