package com.sigmundgranaas.forgero.smithing.block.renderer;

import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.sigmundgranaas.forgero.smithing.block.custom.SmithingAnvil;
import com.sigmundgranaas.forgero.smithing.block.entity.SmithingAnvilBlockEntity;
import com.sigmundgranaas.forgero.smithing.util.BoundingBoxUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
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
    private static final Logger LOGGER = LogManager.getLogger("ForgeroSmithingAnvilRenderer");
    private final Map<Identifier, int[]> offsetCache = new ConcurrentHashMap<>();
    private final BoundingBoxUtil boundingBoxUtil = new BoundingBoxUtil();

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

        // First, translate to the center of the block (0.5, 1.025, 0.5)
        matrices.translate(0.5f, 1.025f, 0.5f);

        // Then, rotate based on anvil facing direction (Y axis)
        switch (entity.getCachedState().get(SmithingAnvil.FACING)) {
            case NORTH -> matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(0));
            case EAST -> matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-90));
            case SOUTH -> matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));
            case WEST -> matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90));
        }

        // Rotate the tool 180 degrees around Y to turn it around
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));

        // Now, translate forward to the "anvil top" (relative to facing)

        matrices.scale(1f, 1f, 1f);

        // --- Centering logic start ---
        int[] offset = getItemTextureOffset(itemStack);
        float dx = offset[0] / 16.0f;
        float dz = offset[1] / 16.0f;
        // Cap the offsets to a maximum of 3/16 in either direction
        dx = Math.max(-1f/16f, Math.min(1f/16f, dx));
        dz = Math.max(-1f/16f, Math.min(1f/16f, dz));
        matrices.translate(dx, 0, dz);
        // --- Centering logic end ---

        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90));

		matrices.translate(0, -0.1f, 0f);

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
                BufferedImage image = com.sigmundgranaas.forgero.smithing.util.RuntimeModelUtil.getFirstQuadTextureImage(itemStack, client);
                int[] offset = BoundingBoxUtil.getItemTextureOffsetFromImage(image);
                LOGGER.info("[Renderer] Calculated offset for {}: ({}, {})", itemId, offset[0], offset[1]);
                return offset;
            } catch (Exception e) {
                LOGGER.error("[Renderer] Error calculating texture offset: ", e);
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
