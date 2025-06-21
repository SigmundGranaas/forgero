package com.sigmundgranaas.forgero.minecraft.common.block.upgradestation.render;

import static com.sigmundgranaas.forgero.minecraft.common.block.upgradestation.UpgradeStationBlock.FACING;
import static com.sigmundgranaas.forgero.minecraft.common.block.upgradestation.UpgradeStationBlock.PART;

import com.sigmundgranaas.forgero.minecraft.common.block.upgradestation.UpgradeStationBlock.UpgradeStationBlockPart;
import com.sigmundgranaas.forgero.minecraft.common.block.upgradestation.entity.UpgradeStationBlockEntity;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class UpgradeStationBlockEntityRenderer implements BlockEntityRenderer<UpgradeStationBlockEntity> {
    public UpgradeStationBlockEntityRenderer(BlockEntityRendererFactory.Context ignoredContext) {}

    @Override
    public void render(@Nullable UpgradeStationBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (entity == null) {
            return;
        }

        @Nullable var world = entity.getWorld();
        if (world == null) {
            return;
        }

        BlockState blockState = entity.getCachedState();

        // Only render for LEFT part since only LEFT part has a block entity now
        if (blockState.get(PART) != UpgradeStationBlockPart.LEFT) {
            return;
        }

        Direction facing = blockState.get(FACING);

        ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();
        ItemStack inventory = entity.getRenderInventory();

        // Exit early if there's nothing to render
        if (inventory.isEmpty()) {
            return;
        }

        // Apply rotation based on block facing - using the same approach as AssemblyStation
        matrices.push();

        // First translate to center of block
        matrices.translate(0.5, 0, 0.5);

        // Apply rotation based on facing direction
        switch (facing) {
            case NORTH:
                // Default orientation
                break;
            case SOUTH:
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));
                break;
            case EAST:
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(270));
                break;
            case WEST:
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90));
                break;
        }

        // Translate back to maintain rotated coordinate system
        matrices.translate(-0.5, 0, -0.5);

        // From here on, coordinates are relative to the block's facing direction
        // So the same coordinates work for all facing directions

        // Position item in the middle of the upgrade station
        matrices.push();
        matrices.translate(0, 1.01, 0.30);

        // Rotate to lay flat on the table
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));

        // Scale the item with separate x, y, z scale factors
        float scaleX = 1.15f;  // Width (appears as width when flat)
        float scaleY = 1.15f;  // Length (appears as height when flat)
        float scaleZ = 1.15f;  // Thickness (depth when flat on table)
        matrices.scale(scaleX, scaleY, scaleZ);

        // Render in 3D lying flat
        itemRenderer.renderItem(
                inventory,
                ModelTransformationMode.GROUND,
                getLightLevel(world, entity.getPos()),
                OverlayTexture.DEFAULT_UV,
                matrices,
                vertexConsumers,
                world,
                1
        );

        matrices.pop();
        matrices.pop();
    }

    private int getLightLevel(World world, BlockPos pos) {
        int blockLight = world.getLightLevel(LightType.BLOCK, pos);
        int skyLight = world.getLightLevel(LightType.SKY, pos);
        return LightmapTextureManager.pack(blockLight, skyLight);
    }
}
