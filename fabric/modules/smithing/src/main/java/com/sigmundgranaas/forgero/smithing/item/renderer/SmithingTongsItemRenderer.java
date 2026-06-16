package com.sigmundgranaas.forgero.smithing.item.renderer;

import com.sigmundgranaas.forgero.smithing.item.custom.SmithingTongsItem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RotationAxis;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;

@Environment(EnvType.CLIENT)
public class SmithingTongsItemRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {
	private static final float STORED_OFFSET_X = 0.25f;
	private static final float STORED_OFFSET_Y = 0.25f;
	private static final float STORED_OFFSET_Z = 0f;

	private static final float STORED_SCALE = 0.5f;

	// These are DEGREES, not radians.
	private static final float STORED_ROTATION_X = 45f;
	private static final float STORED_ROTATION_Y = 90f;
	private static final float STORED_ROTATION_Z = 0f;


	// 10 45 -20
	@Override
	public void render(
			ItemStack stack,
			ModelTransformationMode mode,
			MatrixStack matrices,
			VertexConsumerProvider vertexConsumers,
			int light,
			int overlay
	) {
		matrices.push();

		// Render base first, then render the stored item on top.
		renderTongsBase(stack, mode, matrices, vertexConsumers, light, overlay);
		renderStoredStack(stack, mode, matrices, vertexConsumers, light, overlay);

		matrices.pop();
	}

	private void renderStoredStack(
			ItemStack tongsStack,
			ModelTransformationMode mode,
			MatrixStack matrices,
			VertexConsumerProvider vertexConsumers,
			int light,
			int overlay
	) {
		ItemStack stored = SmithingTongsItem.getStoredStack(tongsStack);

		if (stored.isEmpty()) {
			return;
		}

		matrices.push();

		matrices.translate(
				0.5f + STORED_OFFSET_X,
				0.5f + STORED_OFFSET_Y,
				0.5f + STORED_OFFSET_Z
		);

		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(STORED_ROTATION_Y));
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(STORED_ROTATION_X));
		matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(STORED_ROTATION_Z));

		matrices.scale(STORED_SCALE, STORED_SCALE, STORED_SCALE);

		ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();

		itemRenderer.renderItem(
				stored,
				ModelTransformationMode.NONE,
				light,
				overlay,
				matrices,
				vertexConsumers,
				MinecraftClient.getInstance().world,
				0
		);

		matrices.pop();
	}

	private void renderTongsBase(
			ItemStack tongsStack,
			ModelTransformationMode mode,
			MatrixStack matrices,
			VertexConsumerProvider vertexConsumers,
			int light,
			int overlay
	) {
		ItemStack baseTongs = tongsStack.copy();
		SmithingTongsItem.clearStoredStack(baseTongs);

		ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();

		matrices.push();

		matrices.translate(0.5f, 0.5f, 0.5f);

		itemRenderer.renderItem(
				baseTongs,
				ModelTransformationMode.NONE,
				light,
				overlay,
				matrices,
				vertexConsumers,
				MinecraftClient.getInstance().world,
				0
		);

		matrices.pop();
	}
}
