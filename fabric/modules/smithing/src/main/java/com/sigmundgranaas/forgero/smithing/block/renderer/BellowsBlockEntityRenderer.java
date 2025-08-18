package com.sigmundgranaas.forgero.smithing.block.renderer;

import com.sigmundgranaas.forgero.smithing.block.entity.BellowsBlockEntity;
import com.sigmundgranaas.forgero.smithing.model.BellowsModel;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;

public class BellowsBlockEntityRenderer implements BlockEntityRenderer<BellowsBlockEntity> {
	private final BellowsModel model;

	public BellowsBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
		this.model = new BellowsModel(ctx.getLayerModelPart(BellowsModel.LAYER_LOCATION));
	}

	@Override
	public void render(BellowsBlockEntity be, float tickDelta, MatrixStack matrices,
					   VertexConsumerProvider vertexConsumers, int light, int overlay) {
		matrices.push();

		// Center pivot
		matrices.translate(0.5, 0, 0.5);

		// Apply rotation
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(be.getRotation()));

		// Move back
		matrices.translate(-0.5, 0, -0.5);

		// Render your Blockbench model
		model.render(matrices, vertexConsumers.getBuffer(RenderLayer.getSolid()), light, overlay, 1f, 1f, 1f, 1f);

		matrices.pop();
	}
}
