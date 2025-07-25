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
		// Calculate fill percentage (max 64 coal)
		float fillPercentage = Math.min(coalLevel / 64.0f, 1.0f);

		// If there's no coal, don't render anything
		if (coalLevel == 0) {
			return;
		}

		matrices.push();

		// Center on the block first
		matrices.translate(0.5, 0, 0.5);

		// Rotate based on facing direction
		switch (facing) {
			case NORTH -> matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(0));
			case SOUTH -> matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));
			case WEST -> matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90));
			case EAST -> matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-90));
		}

		// Position the bar on the front face of the block (after rotation)
		// This will always place it on the "front" face regardless of actual facing
		matrices.translate(0.376, 0.1876, -0.3124);

		// Bar dimensions
		float barWidth = 0.0624f; // Exactly 1 pixel (1/16 = 0.0625)
		float maxBarHeight = 0.4373f; // Maximum 7 pixels high (7/16 = 0.4375)
		float barDepth = 0.124f;
		float filledHeight = maxBarHeight * fillPercentage;

		// Use translucent layer to prevent z-fighting and flickering
		VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getSolid());

		// Render filled portion if there's any coal
		if (coalLevel > 0) {
			// Make it completely black
			float red = 0.0f, green = 0.0f, blue = 0.0f;

			renderFilledPortion(matrices, vertexConsumer, barWidth, filledHeight, barDepth, red, green, blue, light, overlay);
		}

		matrices.pop();
	}

	private void renderFilledPortion(MatrixStack matrices, VertexConsumer vertexConsumer, float width, float height, float depth,
									 float red, float green, float blue, int light, int overlay) {
		float a = 1.0f;

		// Front face (negative Z)
		addQuad(matrices, vertexConsumer,
			0, 0, 0,
			width, 0, 0,
			width, height, 0,
			0, height, 0,
			red, green, blue, a, light, overlay);

		// Back face (positive Z)
		addQuad(matrices, vertexConsumer,
			width, 0, depth,
			0, 0, depth,
			0, height, depth,
			width, height, depth,
			red, green, blue, a, light, overlay);

		// Left face (negative X)
		addQuad(matrices, vertexConsumer,
			0, 0, depth,
			0, 0, 0,
			0, height, 0,
			0, height, depth,
			red, green, blue, a, light, overlay);

		// Right face (positive X)
		addQuad(matrices, vertexConsumer,
			width, 0, 0,
			width, 0, depth,
			width, height, depth,
			width, height, 0,
			red, green, blue, a, light, overlay);

		// Top face (positive Y)
		addQuad(matrices, vertexConsumer,
			0, height, 0,
			width, height, 0,
			width, height, depth,
			0, height, depth,
			red, green, blue, a, light, overlay);

		// Bottom face (negative Y)
		addQuad(matrices, vertexConsumer,
			0, 0, depth,
			width, 0, depth,
			width, 0, 0,
			0, 0, 0,
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

		// Calculate proper normal vector using cross product
		float dx1 = x2 - x1;
		float dy1 = y2 - y1;
		float dz1 = z2 - z1;

		float dx2 = x4 - x1;
		float dy2 = y4 - y1;
		float dz2 = z4 - z1;

		// Cross product to get normal
		float nx = dy1 * dz2 - dz1 * dy2;
		float ny = dz1 * dx2 - dx1 * dz2;
		float nz = dx1 * dy2 - dy1 * dx2;

		// Normalize the normal vector
		float length = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
		if (length > 0) {
			nx /= length;
			ny /= length;
			nz /= length;
		}

		// Add vertices in counter-clockwise order for proper front-face rendering
		vertexConsumer.vertex(matrix, x1, y1, z1).color(red, green, blue, alpha).texture(0, 0).overlay(overlay).light(light).normal(normalMatrix, nx, ny, nz).next();
		vertexConsumer.vertex(matrix, x2, y2, z2).color(red, green, blue, alpha).texture(1, 0).overlay(overlay).light(light).normal(normalMatrix, nx, ny, nz).next();
		vertexConsumer.vertex(matrix, x3, y3, z3).color(red, green, blue, alpha).texture(1, 1).overlay(overlay).light(light).normal(normalMatrix, nx, ny, nz).next();
		vertexConsumer.vertex(matrix, x4, y4, z4).color(red, green, blue, alpha).texture(0, 1).overlay(overlay).light(light).normal(normalMatrix, nx, ny, nz).next();
	}
}
