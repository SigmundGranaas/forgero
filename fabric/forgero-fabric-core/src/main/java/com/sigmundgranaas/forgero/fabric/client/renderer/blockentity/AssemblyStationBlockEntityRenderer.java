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
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3f;

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
		this.renderFlatItem(0, entity.getItems().get(0), direction, matrices, light, overlay, vertexConsumers, posData);
	}

	private void renderFlatItem(int index, ItemStack stack, Direction direction, MatrixStack poseStack, int packedLight, int packedOverlay, VertexConsumerProvider vertexConsumers, int posData) {
		if (stack.isEmpty()) return;

		poseStack.push();
		poseStack.translate(0.0, 1.0375, 0.0);

		boolean mirrored = (direction.getAxis().ordinal() == 1 ? 1 : 0) != index % 2;
		switch (direction.getAxis()) {
			case X -> {
				if (mirrored) {
					poseStack.translate(-0.5f, 0f, 0.5f);
				} else {
					poseStack.translate(0.5f, 0f, 0f);
					poseStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(90f));
				}

			}
			case Z -> {
				if (mirrored) {
					poseStack.translate(0f, 0f, -0.5f);
				} else {
					poseStack.translate(0f, 0f, 0.5f);
				}
			}
		}

		// Rotate the tool so it's flat on the surface of the station
		poseStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(90f));
		poseStack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(90f));

		// Scale the tool down
		poseStack.scale(0.75f, 0.75f, 0.75f);

		this.itemRenderer.renderItem(stack, ModelTransformation.Mode.FIXED, packedLight, packedOverlay, poseStack,
				vertexConsumers, posData + index
		);

		poseStack.pop();
	}
}
