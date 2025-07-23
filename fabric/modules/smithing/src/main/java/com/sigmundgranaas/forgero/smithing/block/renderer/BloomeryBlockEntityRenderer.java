package com.sigmundgranaas.forgero.smithing.block.renderer;

import com.sigmundgranaas.forgero.smithing.block.custom.BloomeryBlock;
import com.sigmundgranaas.forgero.smithing.block.entity.BloomeryBlockEntity;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;

public class BloomeryBlockEntityRenderer implements BlockEntityRenderer<BloomeryBlockEntity> {

	public BloomeryBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
	}

	@Override
	public void render(BloomeryBlockEntity entity, float tickDelta, MatrixStack matrices,
					   VertexConsumerProvider vertexConsumers, int light, int overlay) {
		// Add isRemoved() check to prevent rendering a removed entity
		if (entity == null || entity.getWorld() == null || entity.getCachedState() == null || entity.isRemoved()) {
			return;
		}

		// Get the facing direction
		Direction facing = entity.getCachedState().get(BloomeryBlock.FACING);

		// Get coal level for the fuel bar
		ItemStack fuelSlot = entity.getFuelSlot();
		int coalLevel = fuelSlot.getCount();

		// Render the fuel bar
		renderFuelBar(entity, coalLevel, facing, matrices, vertexConsumers, light, overlay);
	}

	private void renderFuelBar(BloomeryBlockEntity entity, int coalLevel, Direction facing, MatrixStack matrices,
							   VertexConsumerProvider vertexConsumers, int light, int overlay) {
		// Debug: Log the coal level to see what we're getting
		if (coalLevel > 0) {
			System.out.println("Rendering fuel bar with coal level: " + coalLevel + " from fuel slot: " + entity.getFuelSlot());
		}

		// Calculate fill percentage (max 64 coal)
		float fillPercentage = Math.min(coalLevel / 64.0f, 1.0f);

		// If there's no coal, don't render anything
		if (coalLevel == 0) {
			return;
		}

		matrices.push();

		// Position the bar based on facing direction
		matrices.translate(2, 2, 2);

		// Rotate based on facing direction to position bar on the front
		switch (facing) {
			case NORTH -> matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(0));
			case SOUTH -> matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));
			case WEST -> matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90));
			case EAST -> matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-90));
		}

		// Move to the front face of the block
		matrices.translate(0, 0, -0.48);

		// Bar dimensions
		float barWidth = 0.08f;
		float barHeight = 0.6f;
		float barDepth = 0.02f;
		float filledHeight = barHeight * fillPercentage;

		// Position bar slightly to the side
		matrices.translate(-0.35, 0.15, 0);

		VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getSolid());

		// Render bar outline (dark frame)
		renderBarFrame(matrices, vertexConsumer, barWidth, barHeight, barDepth, light, overlay);

		// Render filled portion if there's any coal
		if (coalLevel > 0) {
			// Choose color based on fill level
			float red, green, blue;
			if (fillPercentage < 0.25f) {
				// Low - dark gray/black
				red = 0.2f; green = 0.2f; blue = 0.2f;
			} else if (fillPercentage < 0.5f) {
				// Medium - brown/coal color
				red = 0.3f; green = 0.2f; blue = 0.1f;
			} else if (fillPercentage < 0.75f) {
				// High - orange glow
				red = 0.8f; green = 0.4f; blue = 0.1f;
			} else {
				// Full - bright orange/red
				red = 1.0f; green = 0.3f; blue = 0.0f;
			}

			renderFilledPortion(matrices, vertexConsumer, barWidth, filledHeight, barDepth, red, green, blue, light, overlay);
		}

		matrices.pop();
	}

	private void renderBarFrame(MatrixStack matrices, VertexConsumer vertexConsumer, float width, float height, float depth, int light, int overlay) {
		// Frame color - dark gray
		float r = 0.1f, g = 0.1f, b = 0.1f, a = 1.0f;

		// Front face of frame
		addQuad(matrices, vertexConsumer,
			0, 0, 0,
			width, 0, 0,
			width, height, 0,
			0, height, 0,
			r, g, b, a, light, overlay);

		// Back face of frame
		addQuad(matrices, vertexConsumer,
			width, 0, depth,
			0, 0, depth,
			0, height, depth,
			width, height, depth,
			r, g, b, a, light, overlay);

		// Sides of frame
		addQuad(matrices, vertexConsumer,
			0, 0, depth,
			0, 0, 0,
			0, height, 0,
			0, height, depth,
			r, g, b, a, light, overlay);

		addQuad(matrices, vertexConsumer,
			width, 0, 0,
			width, 0, depth,
			width, height, depth,
			width, height, 0,
			r, g, b, a, light, overlay);
	}

	private void renderFilledPortion(MatrixStack matrices, VertexConsumer vertexConsumer, float width, float height, float depth,
									 float red, float green, float blue, int light, int overlay) {
		float a = 1.0f;
		float inset = 0.01f; // Small inset from frame

		// Front face of filled area
		addQuad(matrices, vertexConsumer,
			inset, inset, -inset,
			width - inset, inset, -inset,
			width - inset, height, -inset,
			inset, height, -inset,
			red, green, blue, a, light, overlay);

		// Back face of filled area
		addQuad(matrices, vertexConsumer,
			width - inset, inset, depth + inset,
			inset, inset, depth + inset,
			inset, height, depth + inset,
			width - inset, height, depth + inset,
			red, green, blue, a, light, overlay);

		// Sides of filled area
		addQuad(matrices, vertexConsumer,
			inset, inset, depth + inset,
			inset, inset, -inset,
			inset, height, -inset,
			inset, height, depth + inset,
			red, green, blue, a, light, overlay);

		addQuad(matrices, vertexConsumer,
			width - inset, inset, -inset,
			width - inset, inset, depth + inset,
			width - inset, height, depth + inset,
			width - inset, height, -inset,
			red, green, blue, a, light, overlay);
	}

	private void addQuad(MatrixStack matrices, VertexConsumer vertexConsumer,
						 float x1, float y1, float z1,
						 float x2, float y2, float z2,
						 float x3, float y3, float z3,
						 float x4, float y4, float z4,
						 float red, float green, float blue, float alpha,
						 int light, int overlay) {
		var matrix = matrices.peek().getPositionMatrix();
		var normalMatrix = matrices.peek().getNormalMatrix();

		// Calculate normal (assuming quad is facing forward)
		float nx = 0, ny = 0, nz = 1;

		vertexConsumer.vertex(matrix, x1, y1, z1).color(red, green, blue, alpha).texture(0, 0).overlay(overlay).light(light).normal(normalMatrix, nx, ny, nz).next();
		vertexConsumer.vertex(matrix, x2, y2, z2).color(red, green, blue, alpha).texture(1, 0).overlay(overlay).light(light).normal(normalMatrix, nx, ny, nz).next();
		vertexConsumer.vertex(matrix, x3, y3, z3).color(red, green, blue, alpha).texture(1, 1).overlay(overlay).light(light).normal(normalMatrix, nx, ny, nz).next();
		vertexConsumer.vertex(matrix, x4, y4, z4).color(red, green, blue, alpha).texture(0, 1).overlay(overlay).light(light).normal(normalMatrix, nx, ny, nz).next();
	}
}
