package com.sigmundgranaas.forgero.smithing.mixins;

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.sigmundgranaas.forgero.smithing.temperature.TemperatureColorProvider;
import com.sigmundgranaas.forgero.smithing.temperature.TemperatureUtils;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.block.entity.CampfireBlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

@Mixin(CampfireBlockEntityRenderer.class)
public class CampfireBlockEntityRendererMixin {
	@Unique
	private static final Identifier THERMOMETER = new Identifier("forgero", "textures/gui/thermometer.png");
	@Unique
	private static final Identifier THERMOMETER_UP = new Identifier("forgero", "textures/gui/thermometer_up.png");

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

				int maxTemp = TemperatureUtils.getMaxTemp(stack);
				int[] boundaries = TemperatureColorProvider.getStageBoundaries(maxTemp);
				// Use MinigameHudOverlay stage colors (copy here for reuse)
				int[] stageColors = new int[] {
					0xFF000099, // Cold: dark blue
					0xFF3399FF, // Warm: dark cyan
					0xFFCCCC00, // Hot: dark yellow
					0xFF00CC00, // Very Hot: dark green
					0xFFCC6600, // Near Melt: dark orange
					0xFFCC0000  // Molten: dark red
				};
				int segIdx = segmentIndex(temperature, boundaries);
				int tempColor = stageColors[segIdx];

				// Choose icon
				Identifier icon;
				if (TemperatureUtils.isAtMaxTemperature(stack)) {
					icon = THERMOMETER;
				} else if (TemperatureUtils.isHeating(stack)) {
					icon = THERMOMETER_UP;
				} else {
					icon = THERMOMETER; // fallback
				}

				matrices.push();
				matrices.translate(0.5, 1.5, 0.5 + i * 0.25);

				float yaw = client.getEntityRenderDispatcher().camera.getYaw();
				float pitch = client.getEntityRenderDispatcher().camera.getPitch();
				matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-yaw));
				matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(pitch));
				matrices.scale(-0.015f, -0.015f, 0.015f);

				Matrix4f matrix4f = matrices.peek().getPositionMatrix();

				RenderSystem.disableDepthTest();

				// --- Draw icon ---
				RenderSystem.setShader(GameRenderer::getPositionTexProgram);
				RenderSystem.setShaderTexture(0, icon);

				int iconSize = 6;
				Tessellator tessellator = Tessellator.getInstance();
				BufferBuilder buffer = tessellator.getBuffer();
				buffer.begin(VertexFormat.DrawMode.QUADS, net.minecraft.client.render.VertexFormats.POSITION_TEXTURE);

				buffer.vertex(matrix4f, -iconSize / 2f, 0, 0).texture(0f, 0f).next();
				buffer.vertex(matrix4f, -iconSize / 2f, iconSize, 0).texture(0f, 1f).next();
				buffer.vertex(matrix4f, iconSize / 2f, iconSize, 0).texture(1f, 1f).next();
				buffer.vertex(matrix4f, iconSize / 2f, 0, 0).texture(1f, 0f).next();

				tessellator.draw();

				// --- Draw temperature text smaller ---
				matrices.push();
				float textScale = 0.7f;
				matrices.scale(textScale, textScale, textScale);

				textRenderer.draw(
					tempText,
					(iconSize / 2f + 2) / textScale,
					0,
					tempColor,
					false,
					matrices.peek().getPositionMatrix(),
					vertexConsumers,
					TextRenderer.TextLayerType.SEE_THROUGH,
					0,
					LightmapTextureManager.pack(15, 15)
				);
				matrices.pop();

				// Fix: Only call draw() if vertexConsumers is Immediate
				if (vertexConsumers instanceof VertexConsumerProvider.Immediate immediate) {
					immediate.draw();
				}
				RenderSystem.enableDepthTest();

				matrices.pop();
			}
		}
	}

	@Unique
	private int segmentIndex(int value, int[] boundaries) {
		int idx = java.util.Arrays.binarySearch(boundaries, value);
		if (idx >= 0) {
			return Math.min(idx, boundaries.length - 2);
		}
		int insertionPoint = -(idx + 1);
		return Math.max(0, insertionPoint - 1);
	}
	@Unique
	private int scaleToMax(int base, int maxTemp) {
		if (maxTemp >= 10000) return base;
		return Math.round(base / 10000f * maxTemp);
	}
}
