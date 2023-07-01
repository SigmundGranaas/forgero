package com.sigmundgranaas.forgero.fabric.client.renderer.blockentity;

import com.sigmundgranaas.forgero.fabric.block.assemblystation.AssemblyStationBlock;
import com.sigmundgranaas.forgero.fabric.blockentity.assemblystation.AssemblyStationBlockEntity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Quaternion;

@Environment(EnvType.CLIENT)
public class AssemblyStationBlockEntityRenderer implements BlockEntityRenderer<AssemblyStationBlockEntity> {
	private final ItemRenderer itemRenderer;

	public AssemblyStationBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
		itemRenderer = ctx.getItemRenderer();
	}

	@Override
	public void render(AssemblyStationBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
		//		if (!EasyAnvils.CONFIG.get(ClientConfig.class).renderAnvilContents) return;
		Direction direction = entity.getCachedState().get(AssemblyStationBlock.FACING);
		int posData = (int) entity.getPos().asLong();
		this.renderFlatItem(
				0, ((Inventory) entity).getStack(0), direction, matrices, light, overlay, vertexConsumers, posData);
		this.renderFlatItem(
				1, ((Inventory) entity).getStack(1), direction, matrices, light, overlay, vertexConsumers, posData);
	}

	private void renderFlatItem(int index, ItemStack stack, Direction direction, MatrixStack poseStack, int packedLight, int packedOverlay, VertexConsumerProvider vertexConsumers, int posData) {
		if (stack.isEmpty()) return;

		poseStack.push();
		poseStack.translate(0.0, 1.0375, 0.0);
//		poseStack.multiply(Direction.Axis.XN.rotationDegrees(90.0F));
		poseStack.multiply(Quaternion.fromEulerXyz(90f, 0f, 0f));
		boolean mirrored = (direction.getAxis().ordinal() == 1 ? 1 : 0) != index % 2;

		switch (direction.getAxis()) {
			case X -> {
				if (mirrored) {
					poseStack.translate(0.25, -0.5, 0.0);
				} else {
//					poseStack.multiply(Direction.Axis.ZP.rotationDegrees(180.0F));
					poseStack.multiply(Quaternion.fromEulerXyz(0f, 0f, -180f));
					poseStack.translate(-0.75, 0.5, 0.0);
				}
			}
			case Z -> {
				if (mirrored) {
//					poseStack.multiply(Direction.Axis.ZN.rotationDegrees(90.0F));
					poseStack.multiply(Quaternion.fromEulerXyz(0, 0f, 90f));
					poseStack.translate(0.25, 0.5, 0.0);
				} else {
//					poseStack.multiply(Axis.ZP.rotationDegrees(90.0F));
					poseStack.multiply(Quaternion.fromEulerXyz(0, 0f, -90f));
					poseStack.translate(-0.75, -0.5, 0.0);
				}
			}
		}
		poseStack.scale(0.375F, 0.375F, 0.375F);

		this.itemRenderer.renderItem(stack, ModelTransformation.Mode.FIXED, packedLight, packedOverlay, poseStack,
				vertexConsumers, posData + index
		);

		poseStack.pop();
	}
}
