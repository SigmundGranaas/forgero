package com.sigmundgranaas.forgero.smithing.block.renderer;

import com.sigmundgranaas.forgero.smithing.block.custom.BloomeryExtensionBlock;
import com.sigmundgranaas.forgero.smithing.block.entity.BloomeryExtensionBlockEntity;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;

public class BloomeryExtensionBlockEntityRenderer implements BlockEntityRenderer<BloomeryExtensionBlockEntity> {
	private final ItemRenderer itemRenderer;

	public BloomeryExtensionBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
		this.itemRenderer = MinecraftClient.getInstance().getItemRenderer();
	}

	@Override
	public void render(BloomeryExtensionBlockEntity entity, float tickDelta, MatrixStack matrices,
					   VertexConsumerProvider vertexConsumers, int light, int overlay) {
		// Add isRemoved() check to prevent rendering a removed entity
		if (entity == null || entity.getWorld() == null || entity.getCachedState() == null || entity.isRemoved()) {
			return;
		}

		Direction facing = entity.getCachedState().get(BloomeryExtensionBlock.FACING);

		ItemStack toolStack = entity.getStack(BloomeryExtensionBlockEntity.TOOL_SLOT);
		if (!toolStack.isEmpty()) {
			renderItem(toolStack, BloomeryExtensionBlockEntity.TOOL_SLOT, facing, matrices, vertexConsumers, light, overlay, tickDelta, entity);
		}

		ItemStack crucibleStack = entity.getStack(BloomeryExtensionBlockEntity.CRUCIBLE_SLOT);
		if (!crucibleStack.isEmpty()) {
			renderItem(crucibleStack, BloomeryExtensionBlockEntity.CRUCIBLE_SLOT, facing, matrices, vertexConsumers, light, overlay, tickDelta, entity);
		}

		ItemStack oreStack = entity.getStack(BloomeryExtensionBlockEntity.ORE_SLOT);
		if (!oreStack.isEmpty()) {
			renderItem(oreStack, BloomeryExtensionBlockEntity.ORE_SLOT, facing, matrices, vertexConsumers, light, overlay, tickDelta, entity);
		}
	}

	private void renderItem(ItemStack stack, int slot, Direction facing, MatrixStack matrices,
							VertexConsumerProvider vertexConsumers, int light, int overlay, float tickDelta, BloomeryExtensionBlockEntity entity) {

		matrices.push();

		matrices.translate(0.5, 0.5, 0.5);

		float rotation = switch (facing) {
			case SOUTH -> 180f;
			case WEST -> 270f;
			case EAST -> 90f;
			default -> 0f;
		};
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotation));

		ModelTransformationMode mode = ModelTransformationMode.GUI; // Default

		switch (slot) {
			case BloomeryExtensionBlockEntity.TOOL_SLOT -> {
				matrices.translate(0.0, 0.6, -0.2);
				matrices.scale(0.75f, 0.75f, 0.75f);
				matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));
				mode = ModelTransformationMode.FIXED;
			}
			case BloomeryExtensionBlockEntity.CRUCIBLE_SLOT -> {
				matrices.translate(0.5, 0.75, 0.5);
				matrices.scale(0.6f, 0.6f, 0.6f);
				mode = ModelTransformationMode.FIXED;
			}
			case BloomeryExtensionBlockEntity.ORE_SLOT -> {
				matrices.translate(0.25, 0.9, 0.0);
				matrices.scale(0.4f, 0.4f, 0.4f);
				mode = ModelTransformationMode.FIXED;
			}
		}

		itemRenderer.renderItem(stack, mode, light, overlay, matrices, vertexConsumers, entity.getWorld(), 0);

		matrices.pop();
	}
}
