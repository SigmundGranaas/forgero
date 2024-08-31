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
		ItemStack stack = entity.getRenderStack();
		matrices.push();
		matrices.translate(0.5f, 0.75f, 0.5f);
		matrices.scale(6f, 6f, 6f);
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(270));

		itemRenderer.renderItem(
				stack, ModelTransformationMode.GUI, getLightLevel(world, entity.getPos()), OverlayTexture.DEFAULT_UV, matrices,
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
