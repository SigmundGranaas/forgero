package com.sigmundgranaas.forgero.smithing.mixins;

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.sigmundgranaas.forgero.smithing.temperature.TemperatureUtils;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.Blocks;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

@Mixin(WorldRenderer.class)
public class CauldronItemTemperatureMixin {

	@Inject(method = "render", at = @At("TAIL"))
	private void forgero$renderItemTemperatures(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo ci) {
		MinecraftClient client = MinecraftClient.getInstance();
		World world = client.world;
		if (world == null || client.player == null) return;

		TextRenderer textRenderer = client.textRenderer;

		// Find all items in full water cauldrons
		List<ItemEntity> items = world.getEntitiesByClass(ItemEntity.class, client.player.getBoundingBox().expand(16), item -> {
			var pos = item.getBlockPos();
			var state = world.getBlockState(pos);
			return state.isOf(Blocks.WATER_CAULDRON) && state.get(LeveledCauldronBlock.LEVEL) == 3;
		});

		// Use the engine's entity vertex consumers and flush after drawing
		VertexConsumerProvider.Immediate vertexConsumers = client.getBufferBuilders().getEntityVertexConsumers();
		int light = LightmapTextureManager.pack(15, 15);
		Vec3d camPos = camera.getPos();

		for (int i = 0; i < items.size(); i++) {
			ItemEntity itemEntity = items.get(i);
			ItemStack stack = itemEntity.getStack();
			if (!stack.isEmpty()) {
				int temperature = TemperatureUtils.getTemperature(stack); // Always use NBT, which is updated by S2C packet
				// Optionally, request a sync from server if needed (not typical for S2C-only packets)
				String tempText = temperature + "°";

				matrices.push();

				Vec3d pos = itemEntity.getPos();
				// Raise the Y offset so the temperature is well above the cauldron and item
				matrices.translate(pos.x - camPos.x, pos.y - camPos.y + 1.5 + (i * 0.15), pos.z - camPos.z);

				float yaw = camera.getYaw();
				float pitch = camera.getPitch();
				matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-yaw));
				matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(pitch));
				matrices.scale(-0.015f, -0.015f, 0.015f);

				Matrix4f localMatrix = matrices.peek().getPositionMatrix();

				RenderSystem.disableDepthTest();
				textRenderer.draw(
						tempText,
						-textRenderer.getWidth(tempText) / 2f,
						0,
						0xFFFFFF,
						false,
						localMatrix,
						vertexConsumers,
						TextRenderer.TextLayerType.SEE_THROUGH,
						0,
						light
				);
				RenderSystem.enableDepthTest();

				// Flush text
				vertexConsumers.draw();

				matrices.pop();
			}
		}
	}
}
