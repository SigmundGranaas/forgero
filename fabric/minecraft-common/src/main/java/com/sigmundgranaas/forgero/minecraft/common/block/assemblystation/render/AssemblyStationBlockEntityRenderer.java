package com.sigmundgranaas.forgero.minecraft.common.block.assemblystation.render;

import static com.sigmundgranaas.forgero.minecraft.common.block.assemblystation.AssemblyStationBlock.FACING;

import com.sigmundgranaas.forgero.minecraft.common.block.assemblystation.entity.AssemblyStationBlockEntity;
import org.jetbrains.annotations.NotNull;
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
public class AssemblyStationBlockEntityRenderer implements BlockEntityRenderer<AssemblyStationBlockEntity> {
    // Result slot positioning parameters
    private static final float RESULT_START_X = -0.15f;
    private static final float RESULT_Y = 1.025f;
    private static final float RESULT_START_Z = 0.75f;
    private static final float RESULT_X_SPACING = 0.25f;
    private static final float RESULT_Z_SPACING = 0.25f;
    private static final float RESULT_ITEM_SCALE = 0.30f;

    public AssemblyStationBlockEntityRenderer(BlockEntityRendererFactory.Context ignoredContext) {}

    @Override
    public void render(@Nullable AssemblyStationBlockEntity entity, float tickDelta, MatrixStack matrices,
                      VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (entity == null || entity.getWorld() == null) {
            return;
        }

        World world = entity.getWorld();
        BlockState blockState = entity.getCachedState();
        Direction facing = blockState.get(FACING);
        ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();
        ItemStack mainItem = entity.getRenderInventory();

        // Apply rotation based on block facing
        matrices.push();
        matrices.translate(0.5, 0, 0.5);
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
        matrices.translate(-0.5, 0, -0.5);

        // Main item render
        if (!mainItem.isEmpty()) {
            renderMainItem(matrices, vertexConsumers, world, entity, mainItem, itemRenderer);
        }

        // Render the 9 result slots in a 3x3 grid
        renderResultItems(matrices, vertexConsumers, world, entity, itemRenderer);

        // End the global rotation that was applied for the facing direction
        matrices.pop();
    }

    /**
     * Renders the main input item
     */
    private void renderMainItem(MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                               World world, AssemblyStationBlockEntity entity, ItemStack item,
                               ItemRenderer itemRenderer) {
        matrices.push();
        matrices.translate(0.5f, 1.025f, 0.5f);
        matrices.scale(0.75f, 0.75f, 0.75f);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));

        itemRenderer.renderItem(
                item, ModelTransformationMode.GUI,
                getLightLevel(world, entity.getPos()), OverlayTexture.DEFAULT_UV,
                matrices, vertexConsumers, world, 1
        );
        matrices.pop();
    }

    /**
     * Renders all result items in their slots
     */
    private void renderResultItems(MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                                  World world, AssemblyStationBlockEntity entity,
                                  ItemRenderer itemRenderer) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int slotIndex = row * 3 + col;
                ItemStack resultItem = getResultSlotItem(entity, slotIndex);

                if (!resultItem.isEmpty()) {
                    matrices.push();
                    // Calculate position based on row and column
                    float x = RESULT_START_X - (col * RESULT_X_SPACING);
                    float z = RESULT_START_Z + (row * RESULT_Z_SPACING);

                    matrices.translate(x, RESULT_Y, z);
                    matrices.scale(RESULT_ITEM_SCALE, 0.6f, RESULT_ITEM_SCALE);
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(270));
                    matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180));

                    itemRenderer.renderItem(
                            resultItem,
                            ModelTransformationMode.GUI,
                            getLightLevel(world, entity.getPos()),
                            OverlayTexture.DEFAULT_UV,
                            matrices,
                            vertexConsumers,
                            world,
                            1
                    );
                    matrices.pop();
                }
            }
        }
    }

    // Helper method to get the appropriate result slot item by index
    private ItemStack getResultSlotItem(AssemblyStationBlockEntity entity, int index) {
        switch (index) {
            case 0: return entity.getRenderResultSlot1();
            case 1: return entity.getRenderResultSlot2();
            case 2: return entity.getRenderResultSlot3();
            case 3: return entity.getRenderResultSlot4();
            case 4: return entity.getRenderResultSlot5();
            case 5: return entity.getRenderResultSlot6();
            case 6: return entity.getRenderResultSlot7();
            case 7: return entity.getRenderResultSlot8();
            case 8: return entity.getRenderResultSlot9();
            default: return ItemStack.EMPTY;
        }
    }

    private int getLightLevel(@NotNull World world, BlockPos blockPosition) {
        int blockLight = world.getLightLevel(LightType.BLOCK, blockPosition);
        int skyLight = world.getLightLevel(LightType.SKY, blockPosition);
        return LightmapTextureManager.pack(blockLight, skyLight);
    }
}
