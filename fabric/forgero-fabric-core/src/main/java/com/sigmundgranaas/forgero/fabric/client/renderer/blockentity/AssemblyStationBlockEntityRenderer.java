package com.sigmundgranaas.forgero.fabric.client.renderer.blockentity;

import com.sigmundgranaas.forgero.fabric.block.assemblystation.AssemblyStationBlock;
import com.sigmundgranaas.forgero.fabric.blockentity.assemblystation.AssemblyStationBlockEntity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;

@Environment(EnvType.CLIENT)
public class AssemblyStationBlockEntityRenderer implements BlockEntityRenderer<AssemblyStationBlockEntity> {
	private final ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();

	public AssemblyStationBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {}

	@Override
	public void render(AssemblyStationBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
		//		if (!EasyAnvils.CONFIG.get(ClientConfig.class).renderAnvilContents) return;
		Direction direction = entity.getCachedState().get(AssemblyStationBlock.FACING);
		int posData = (int) entity.getPos().asLong();
		var items = entity.getItems();

//		for (var item : items) {
//			Forgero.LOGGER.info(item);
//		}

		for (int i = 0; i < items.size(); i++) {
			this.renderItem(i, items.get(i), direction, matrices, light, overlay, vertexConsumers, posData);
		}
	}

	private void renderItem(int index, ItemStack stack, Direction direction, MatrixStack poseStack, int packedLight, int packedOverlay, VertexConsumerProvider vertexConsumers, int posData) {
		if (stack.isEmpty()) return;

		poseStack.push();

		// Move the item up slightly above the station's surface (to avoid Z-fighting)
		poseStack.translate(0.0, 1.0375, 0.0);

		// Move the item to the center of the station
//		boolean isDirectionPositive = direction.getDirection() == Direction.AxisDirection.POSITIVE;
		float directionMultiplier = direction.getDirection() == Direction.AxisDirection.POSITIVE ? 1f : -1f;
		switch (direction.getAxis()) {
			case X -> poseStack.translate(0f, 0f, 0.5f * directionMultiplier);
			case Z -> poseStack.translate(0.5f * directionMultiplier, 0f, 0f);
		}
//
//		if (index == 0) {
//			switch (direction.getAxis()) {
//				case X -> {
//					if (isDirectionPositive) {
//						poseStack.translate(0.5f, 0f, 0f);
//					} else {
//						poseStack.translate(0.5f, 0f, 0f);
//					}
////					poseStack.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(90f));
//				}
//				case Z -> {
//					if (isDirectionPositive) {
//						poseStack.translate(-0.5f, 0f, 0f);
//					} else {
//						poseStack.translate(0.5f, 0f, 0f);
//					}
//				}
//			}
//
//			// Rotate the item so it's flat on the surface of the station
//			poseStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(90f));
//			poseStack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(90f));
//		}
//
//		if (index >= 1) {
//			switch (direction.getAxis()) {
//				case X -> {
//					if (isDirectionPositive) {
//						poseStack.translate(0.5f, 0f, 0.5f);
//					} else {
//						poseStack.translate(-0.5f, 0f, 0f);
//						poseStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(90f));
//					}
//
//					poseStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(25f));
//				}
//				case Z -> {
//					if (isDirectionPositive) {
//						poseStack.translate(0f, 0f, 0.5f);
//					} else {
//						poseStack.translate(0f, 0f, -0.5f);
//					}
//
//					poseStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(25f));
//				}
//			}
//
//			// Rotate the part so it's flat on the surface of the station
//			poseStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(90f));
//			poseStack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(90f));
//		}

		// Scale the item down
		poseStack.scale(0.75f, 0.75f, 0.75f);

		this.itemRenderer.renderItem(stack, ModelTransformation.Mode.FIXED, packedLight, packedOverlay, poseStack,
				vertexConsumers, posData + index
		);

		poseStack.pop();
	}
}
