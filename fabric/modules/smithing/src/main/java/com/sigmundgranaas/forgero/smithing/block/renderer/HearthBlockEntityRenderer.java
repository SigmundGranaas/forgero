package com.sigmundgranaas.forgero.smithing.block.renderer;

import com.sigmundgranaas.forgero.smithing.block.entity.custom.HearthBlockEntity;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RotationAxis;

public class HearthBlockEntityRenderer implements BlockEntityRenderer<HearthBlockEntity> {
	private static final int ITEM_SLOT = 0;
	private static final float ITEM_CENTER_X = 0.5f;
	private static final float ITEM_CENTER_Y = 0.519f;
	private static final float ITEM_CENTER_Z = 0.5f;
	private static final float ITEM_SCALE = 0.6f;
	// Rotate X after Y rotation so the item lays flat facing the correct direction
	private static final float ITEM_ROTATION_X = 90.0f;

	@SuppressWarnings("unused")
	public HearthBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
	}

	private void setupItemTransforms(MatrixStack matrices, HearthBlockEntity entity) {
		matrices.translate(ITEM_CENTER_X, ITEM_CENTER_Y, ITEM_CENTER_Z);

		var blockState = entity.getCachedState();
		if (blockState.contains(net.minecraft.state.property.Properties.HORIZONTAL_FACING)) {
			float yRotation = switch (blockState.get(net.minecraft.state.property.Properties.HORIZONTAL_FACING)) {
				case NORTH -> 180.0f;
				case SOUTH -> 0f;
				case EAST -> 90.0f;
				case WEST -> 270.0f;
				default -> 180.0f;
			};
			matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yRotation));
		}

		// Rotate around X to lay the item flat with the correct face up/down
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(ITEM_ROTATION_X));

		matrices.scale(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE);
	}

	@Override
	public void render(
			HearthBlockEntity entity,
			float tickDelta,
			MatrixStack matrices,
			VertexConsumerProvider vertexConsumers,
			int light,
			int overlay
	) {
		ItemStack stack = entity.getStack(ITEM_SLOT);
		if (stack.isEmpty()) {
			return;
		}

		matrices.push();
		setupItemTransforms(matrices, entity);
		renderItem(matrices, vertexConsumers, entity, stack, light, overlay);
		matrices.pop();
	}

	private void renderItem(MatrixStack matrices, VertexConsumerProvider vertexConsumers,
			HearthBlockEntity entity, ItemStack stack, int light, int overlay) {
		ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();

		itemRenderer.renderItem(
				stack,
				ModelTransformationMode.FIXED,
				light,
				overlay,
				matrices,
				vertexConsumers,
				entity.getWorld(),
				0
		);
	}
}
