package com.sigmundgranaas.forgero.smithing.block.renderer;

import com.sigmundgranaas.forgero.smithing.block.entity.custom.HearthBlockEntity;
import com.sigmundgranaas.forgero.smithing.item.ModItems;

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

		if (stack.getItem() == ModItems.CRUCIBLE) {
			matrices.translate(0.5, 1.0, 0.5);
			matrices.scale(1.0f, 1.0f, 1.0f);
		} else {
			matrices.translate(0.5, 0.5, 0.5);
			matrices.scale(0.75f, 0.75f, 0.75f);
			matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));
			// ^ Rotate flat items so they "sit" horizontally like on a table
		}

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
