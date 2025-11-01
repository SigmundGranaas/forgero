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
	private static final float ITEM_ROTATION_X = 90.0f;

	public HearthBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
		// Context parameter required by interface
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
		setupItemTransforms(matrices);
		renderItem(matrices, vertexConsumers, entity, stack, light, overlay);
		matrices.pop();
	}

	private void setupItemTransforms(MatrixStack matrices) {
		matrices.translate(ITEM_CENTER_X, ITEM_CENTER_Y, ITEM_CENTER_Z);
		matrices.scale(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE);
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(ITEM_ROTATION_X));
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
