package com.sigmundgranaas.forgero.smithing.mixins;

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.sigmundgranaas.forgero.smithing.temperature.TemperatureColorProvider;
import com.sigmundgranaas.forgero.smithing.temperature.TemperatureUtils;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;



@Mixin(WorldRenderer.class)
public class CauldronItemTemperatureMixin {

	private static final Identifier THERMOMETER = new Identifier("forgero", "textures/gui/thermometer.png");
	private static final Identifier THERMOMETER_DOWN = new Identifier("forgero", "textures/gui/thermometer_down.png");

	@Inject(method = "render", at = @At("TAIL"))
	private void renderItemTemperatures(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo ci) {
		MinecraftClient client = MinecraftClient.getInstance();
		World world = client.world;
		if (world == null || client.player == null) return;

		TextRenderer textRenderer = client.textRenderer;

		List<ItemEntity> items = world.getEntitiesByClass(ItemEntity.class, client.player.getBoundingBox().expand(16), item -> {
			return TemperatureUtils.isItemInFilledWaterCauldron(item, world);
		});

		VertexConsumerProvider.Immediate vertexConsumers = client.getBufferBuilders().getEntityVertexConsumers();
		int light = LightmapTextureManager.pack(15, 15);
		Vec3d camPos = camera.getPos();

		for (int i = 0; i < items.size(); i++) {
			ItemEntity itemEntity = items.get(i);
			ItemStack stack = itemEntity.getStack();
			if (stack.isEmpty()) continue;

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
			if (TemperatureUtils.isAtDefaultTemperature(stack)) {
				icon = THERMOMETER;
			} else if (TemperatureUtils.isCooling(stack)) {
				icon = THERMOMETER_DOWN;
			} else {
				icon = THERMOMETER; // fallback
			}

			matrices.push();

			Vec3d pos = itemEntity.getPos();
			matrices.translate(pos.x - camPos.x, pos.y - camPos.y + 1.5 + (i * 0.15), pos.z - camPos.z);

			float yaw = camera.getYaw();
			float pitch = camera.getPitch();
			matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-yaw));
			matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(pitch));
			matrices.scale(-0.015f, -0.015f, 0.015f);

			Matrix4f localMatrix = matrices.peek().getPositionMatrix();

			RenderSystem.disableDepthTest();

			// --- Draw icon ---
			RenderSystem.setShader(GameRenderer::getPositionTexProgram);
			RenderSystem.setShaderTexture(0, icon);

			int iconSize = 6;
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder buffer = tessellator.getBuffer();
			buffer.begin(VertexFormat.DrawMode.QUADS, net.minecraft.client.render.VertexFormats.POSITION_TEXTURE);

			// Flip the texture vertically by swapping Y texture coordinates
			buffer.vertex(localMatrix, -iconSize / 2f, 0, 0).texture(0f, 0f).next();
			buffer.vertex(localMatrix, -iconSize / 2f, iconSize, 0).texture(0f, 1f).next();
			buffer.vertex(localMatrix, iconSize / 2f, iconSize, 0).texture(1f, 1f).next();
			buffer.vertex(localMatrix, iconSize / 2f, 0, 0).texture(1f, 0f).next();

			tessellator.draw();

			// --- Draw temperature text smaller ---
			matrices.push();
			float textScale = 0.7f; // 70% of original size
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
					light
			);
			matrices.pop();

			vertexConsumers.draw();
			RenderSystem.enableDepthTest();

			matrices.pop();
		}
	}

	// Add missing segmentIndex method
	private int segmentIndex(int value, int[] boundaries) {
		int idx = java.util.Arrays.binarySearch(boundaries, value);
		if (idx >= 0) {
			return Math.min(idx, boundaries.length - 2);
		}
		int insertionPoint = -(idx + 1);
		return Math.max(0, insertionPoint - 1);
	}
}
