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
	public HearthBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {}

	@Override
	public void render(
			HearthBlockEntity entity,
			float tickDelta,
			MatrixStack matrices,
			VertexConsumerProvider vertexConsumers,
			int light,
			int overlay
	) {
		ItemStack stack = entity.getStack(0);
		if (stack.isEmpty()) return;

		ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();
		matrices.push();

		matrices.translate(0.5, 0.519, 0.5);
		matrices.scale(0.6f, 0.6f, 0.6f);
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));

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

		matrices.pop();
	}
}
