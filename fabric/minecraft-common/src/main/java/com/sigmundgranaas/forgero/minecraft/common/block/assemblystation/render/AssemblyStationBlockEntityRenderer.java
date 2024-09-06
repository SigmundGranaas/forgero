package com.sigmundgranaas.forgero.minecraft.common.block.assemblystation.render;

import com.sigmundgranaas.forgero.minecraft.common.block.assemblystation.entity.AssemblyStationBlockEntity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class AssemblyStationBlockEntityRenderer implements BlockEntityRenderer<AssemblyStationBlockEntity> {
	public AssemblyStationBlockEntityRenderer(BlockEntityRendererFactory.Context ignoredContext) {}

	@Override
	public void render(@Nullable AssemblyStationBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
		if (entity == null) {
			return;
		}

		@Nullable var world = entity.getWorld();
		if (world == null) {
			return;
		}

		ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();
		ItemStack inventory = entity.getRenderInventory();
		ItemStack resultSlot1 = entity.getRenderResultSlot1();
		ItemStack resultSlot2 = entity.getRenderResultSlot2();
		ItemStack resultSlot3 = entity.getRenderResultSlot3();
		ItemStack resultSlot4 = entity.getRenderResultSlot4();
		ItemStack resultSlot5 = entity.getRenderResultSlot5();
		ItemStack resultSlot6 = entity.getRenderResultSlot6();
		ItemStack resultSlot7 = entity.getRenderResultSlot7();
		ItemStack resultSlot8 = entity.getRenderResultSlot8();
		ItemStack resultSlot9 = entity.getRenderResultSlot9();

		matrices.push();
		matrices.translate(0.5f, 1.025f, 0.5f);
		matrices.scale(0.75f, 0.75f, 0.75f);
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(270));

		itemRenderer.renderItem(
				inventory, ModelTransformationMode.GUI, getLightLevel(world, entity.getPos()), OverlayTexture.DEFAULT_UV, matrices,
				vertexConsumers, world, 1
		);
		matrices.pop();


		matrices.push();
		matrices.translate(0.70f, 1.025f, 1.15f);
		matrices.scale(0.30f, 0.75f, 0.30f);
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(270));
		matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(270));

		itemRenderer.renderItem(
				resultSlot1, ModelTransformationMode.GUI, getLightLevel(world, entity.getPos()), OverlayTexture.DEFAULT_UV, matrices,
				vertexConsumers, world, 1
		);
		matrices.pop();

		matrices.push();
		matrices.translate(0.70f, 1.025f, 1.15f);
		matrices.scale(0.30f, 0.75f, 0.30f);
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(270));
		matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(270));

		itemRenderer.renderItem(
				resultSlot2, ModelTransformationMode.GUI, getLightLevel(world, entity.getPos()), OverlayTexture.DEFAULT_UV, matrices,
				vertexConsumers, world, 1
		);
		matrices.pop();

		matrices.push();
		matrices.translate(0.70f, 1.025f, 1.65f);
		matrices.scale(0.30f, 0.75f, 0.30f);
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(270));
		matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(270));

		itemRenderer.renderItem(
				resultSlot3, ModelTransformationMode.GUI, getLightLevel(world, entity.getPos()), OverlayTexture.DEFAULT_UV, matrices,
				vertexConsumers, world, 1
		);
		matrices.pop();

		matrices.push();
		matrices.translate(0.45f, 1.025f, 1.15f);
		matrices.scale(0.30f, 0.75f, 0.30f);
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(270));
		matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(270));

		itemRenderer.renderItem(
				resultSlot4, ModelTransformationMode.GUI, getLightLevel(world, entity.getPos()), OverlayTexture.DEFAULT_UV, matrices,
				vertexConsumers, world, 1
		);
		matrices.pop();

		matrices.push();
		matrices.translate(0.45f, 1.025f, 1.15f);
		matrices.scale(0.30f, 0.75f, 0.30f);
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(270));
		matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(270));

		itemRenderer.renderItem(
				resultSlot5, ModelTransformationMode.GUI, getLightLevel(world, entity.getPos()), OverlayTexture.DEFAULT_UV, matrices,
				vertexConsumers, world, 1
		);
		matrices.pop();

		matrices.push();
		matrices.translate(0.45f, 1.025f, 1.65f);
		matrices.scale(0.30f, 0.75f, 0.30f);
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(270));
		matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(270));

		itemRenderer.renderItem(
				resultSlot6, ModelTransformationMode.GUI, getLightLevel(world, entity.getPos()), OverlayTexture.DEFAULT_UV, matrices,
				vertexConsumers, world, 1
		);
		matrices.pop();

		matrices.push();
		matrices.translate(0.20f, 1.025f, 1.15f);
		matrices.scale(0.30f, 0.75f, 0.30f);
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(270));
		matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(270));

		itemRenderer.renderItem(
				resultSlot7, ModelTransformationMode.GUI, getLightLevel(world, entity.getPos()), OverlayTexture.DEFAULT_UV, matrices,
				vertexConsumers, world, 1
		);
		matrices.pop();

		matrices.push();
		matrices.translate(0.20f, 1.025f, 1.15f);
		matrices.scale(0.30f, 0.75f, 0.30f);
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(270));
		matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(270));

		itemRenderer.renderItem(
				resultSlot8, ModelTransformationMode.GUI, getLightLevel(world, entity.getPos()), OverlayTexture.DEFAULT_UV, matrices,
				vertexConsumers, world, 1
		);
		matrices.pop();

		matrices.push();
		matrices.translate(0.20f, 1.025f, 1.65f);
		matrices.scale(0.30f, 0.75f, 0.30f);
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(270));
		matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(270));

		itemRenderer.renderItem(
				resultSlot9, ModelTransformationMode.GUI, getLightLevel(world, entity.getPos()), OverlayTexture.DEFAULT_UV, matrices,
				vertexConsumers, world, 1
		);
		matrices.pop();
	}

	private int getLightLevel(@NotNull World world, BlockPos blockPosition) {
		int bLight = world.getLightLevel(LightType.BLOCK, blockPosition);
		int sLight = world.getLightLevel(LightType.SKY, blockPosition);
		return LightmapTextureManager.pack(bLight, sLight);
	}
}
