package com.sigmundgranaas.forgero.minecraft.common.block.assemblystation.renderer;

import com.sigmundgranaas.forgero.minecraft.common.block.assemblystation.AssemblyStationBlock;
import com.sigmundgranaas.forgero.minecraft.common.block.assemblystation.entity.AssemblyStationBlockEntity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
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


@Environment(EnvType.CLIENT)
public class AssemblyStationBlockEntityRenderer implements BlockEntityRenderer<AssemblyStationBlockEntity> {
	public AssemblyStationBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
	}

	@Override
	public void render(AssemblyStationBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
		ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();
		int posLong = (int) entity.getPos().asLong();

		ItemStack itemStack = entity.getRenderStack();
		matrices.push();
		matrices.translate(0.5f, 1.0150f, 0.5f);
		matrices.scale(6f, 6f, 6f);
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90));

		switch (entity.getCachedState().get(AssemblyStationBlock.FACING)) {
			case NORTH -> matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180));
			case EAST -> matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(270));
			case SOUTH -> matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(0));
			case WEST -> matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(90));
		}

		itemRenderer.renderItem(itemStack, ModelTransformationMode.FIXED, light, overlay,
				matrices, vertexConsumers, entity.getWorld(), posLong);
		matrices.pop();
	}

	private int getLightLevel(World world, BlockPos pos) {
		int bLight = world.getLightLevel(LightType.BLOCK, pos);
		int sLight = world.getLightLevel(LightType.SKY, pos);
		return LightmapTextureManager.pack(bLight, sLight);
	}
}
