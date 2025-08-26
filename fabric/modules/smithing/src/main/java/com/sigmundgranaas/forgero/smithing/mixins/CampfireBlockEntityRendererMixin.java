package com.sigmundgranaas.forgero.smithing.mixins;

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.sigmundgranaas.forgero.smithing.temperature.TemperatureUtils;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.CampfireBlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RotationAxis;

@Mixin(CampfireBlockEntityRenderer.class)
public class CampfireBlockEntityRendererMixin {
	@Inject(
			method = "render(Lnet/minecraft/block/entity/CampfireBlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V",
			at = @At("TAIL")
	)
	private void forgero$renderTemperature(CampfireBlockEntity campfireBlockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, CallbackInfo ci) {
		List<ItemStack> items = campfireBlockEntity.getItemsBeingCooked();
		MinecraftClient client = MinecraftClient.getInstance();
		TextRenderer textRenderer = client.textRenderer;

		for (int i = 0; i < items.size(); i++) {
			ItemStack stack = items.get(i);
			if (!stack.isEmpty()) {
				int temperature = TemperatureUtils.getTemperature(stack);
				String tempText = temperature + "°";

				matrices.push();
				matrices.translate(0.5, 1.5, 0.5 + i * 0.25); // raised above campfire a bit more

				float yaw = client.getEntityRenderDispatcher().camera.getYaw();
				float pitch = client.getEntityRenderDispatcher().camera.getPitch();
				matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-yaw));
				matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(pitch));
				matrices.scale(-0.015f, -0.015f, 0.015f); // smaller text, keep flip

				Matrix4f matrix4f = matrices.peek().getPositionMatrix();

				// Ensure drawn above blocks
				RenderSystem.disableDepthTest();
				textRenderer.draw(
						tempText,
						-textRenderer.getWidth(tempText) / 2f,
						0,
						0xFFFFFF,
						false,
						matrix4f,
						vertexConsumers,
						TextRenderer.TextLayerType.SEE_THROUGH,
						0,
						light
				);
				RenderSystem.enableDepthTest();

				matrices.pop();
			}
		}
	}
}
