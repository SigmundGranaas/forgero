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
	public AssemblyStationBlockEntityRenderer(BlockEntityRendererFactory.Context ignoredContext) {}

	@Override
	public void render(@Nullable AssemblyStationBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
		if (entity == null) {
			return;
		}

		@Nullable var world = entity.getWorld();
		if (world == null) {
			return;
		}

		BlockState blockState = entity.getCachedState();
		Direction facing = blockState.get(FACING);

		ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();
		ItemStack inventory = entity.getRenderInventory();

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
		matrices.push();
		matrices.translate(0.5f, 1.025f, 0.5f);
		matrices.scale(0.75f, 0.75f, 0.75f);
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));

		itemRenderer.renderItem(
				inventory, ModelTransformationMode.GUI, getLightLevel(world, entity.getPos()), OverlayTexture.DEFAULT_UV, matrices,
				vertexConsumers, world, 1
		);
		matrices.pop();

        // Render the 9 result slots in a 3x3 grid
        float startX = -0.15f;
        float y = 1.025f;
        float startZ = 0.75f;
        float xSpacing = 0.25f;  // Spacing between columns
        float zSpacing = 0.25f;  // Spacing between rows

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int slotIndex = row * 3 + col;
                ItemStack resultItem = getResultSlotItem(entity, slotIndex);

                if (!resultItem.isEmpty()) {
                    matrices.push();
                    // Calculate position based on row and column
                    float x = startX - (col * xSpacing);
                    float z = startZ + (row * zSpacing);

                    matrices.translate(x, y, z);
                    matrices.scale(0.30f, 0.6f, 0.30f);
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

		// End the global rotation that was applied for the facing direction
		matrices.pop();
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
		int bLight = world.getLightLevel(LightType.BLOCK, blockPosition);
		int sLight = world.getLightLevel(LightType.SKY, blockPosition);
		return LightmapTextureManager.pack(bLight, sLight);
	}
}
