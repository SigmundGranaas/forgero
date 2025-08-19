package com.sigmundgranaas.forgero.smithing.block.renderer;

import com.sigmundgranaas.forgero.smithing.block.entity.BellowsBlockEntity;
import com.sigmundgranaas.forgero.smithing.model.BellowsModel;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;

public class BellowsBlockEntityRenderer implements BlockEntityRenderer<BellowsBlockEntity> {
	private final BellowsModel model;

	public BellowsBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
		this.model = new BellowsModel(ctx.getLayerModelPart(BellowsModel.LAYER_LOCATION));
	}

	@Override
	public void render(BellowsBlockEntity be, float tickDelta, MatrixStack matrices,
					   VertexConsumerProvider vertexConsumers, int light, int overlay) {
		matrices.push();

		// Rotate bellows around center if you have a rotation property on the blockstate
		matrices.translate(0.5, 0, 0.5);
		// If you add a rotation property, apply it here:
		// matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(<your rotation degrees>));
		matrices.translate(-0.5, 0, -0.5);

		float progress = be.getAnimationProgress();
		float yScale = 1f - (0.5f * progress); // squashes down to 50% height

		// Render accordion with scaling
		matrices.push();
		matrices.scale(1f, yScale, 1f);
		model.renderAccordion(matrices, vertexConsumers.getBuffer(RenderLayer.getEntitySolid(BellowsModel.TEXTURE)), light, overlay, 1f, 1f, 1f, 1f);
		matrices.pop();

		// Render base without scaling
		model.renderBase(matrices, vertexConsumers.getBuffer(RenderLayer.getEntitySolid(BellowsModel.TEXTURE)), light, overlay, 1f, 1f, 1f, 1f);

		matrices.pop();
	}
}
