package com.sigmundgranaas.forgero.smithing.block.renderer;

import com.sigmundgranaas.forgero.smithing.block.entity.custom.HearthBlockEntity;
import com.sigmundgranaas.forgero.smithing.temperature.TemperatureColorProvider;
import com.sigmundgranaas.forgero.smithing.temperature.TemperatureUtils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.World;

public class HearthBlockEntityRenderer implements BlockEntityRenderer<HearthBlockEntity> {
	private static int particleCounter = 0;
	private static final int TICK_INTERVAL = 20;

	public HearthBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {}

	@Override
	public void render(
			HearthBlockEntity entity,
			float tickDelta,
			MatrixStack matrices,
			VertexConsumerProvider vertexConsumers,
			int light,
			int overlay
	) {
		ItemStack stack = entity.getStack(0);
		if (stack.isEmpty()) return;

		ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();
		matrices.push();

		matrices.translate(0.5, 0.519, 0.5);
		matrices.scale(0.6f, 0.6f, 0.6f);
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));

		itemRenderer.renderItem(
				stack,
				ModelTransformationMode.FIXED,
				light,
				overlay,
				matrices,
				vertexConsumers,
				entity.getWorld(),
				0
		);

		matrices.pop();

		// Emit temperature-based particles
		if (TemperatureUtils.hasMaxTemperature(stack)) {
			int temp = TemperatureUtils.getTemperature(stack);
			int maxTemp = TemperatureUtils.getMaxTemp(stack);
			emitTemperatureEffects(entity.getWorld(), entity.getPos(), temp, maxTemp);
		}
	}

	private void emitTemperatureEffects(World world, net.minecraft.util.math.BlockPos pos, int temp, int maxTemp) {
		particleCounter++;
		if (particleCounter % TICK_INTERVAL != 0) {
			return;
		}

		double centerX = pos.getX() + 0.5;
		double centerY = pos.getY() + 0.521;
		double centerZ = pos.getZ() + 0.5;
		long randomSeed = (long) pos.asLong() * 31 + world.getTime();

		if (TemperatureColorProvider.isInCold(temp, maxTemp)) {
			// No effects
		} else if (TemperatureColorProvider.isInWarm(temp, maxTemp)) {
			if ((particleCounter + randomSeed) % 2 == 0) {
				spawnRandomizedParticles(world, centerX, centerY, centerZ, ParticleTypes.SMOKE, 2);
			}
		} else if (TemperatureColorProvider.isInHot(temp, maxTemp)) {
			if ((particleCounter + randomSeed * 2) % 3 == 0) {
				spawnRandomizedParticles(world, centerX, centerY, centerZ, ParticleTypes.SMALL_FLAME, 4);
			}
		} else if (TemperatureColorProvider.isInVeryHot(temp, maxTemp)) {
			if ((particleCounter + randomSeed * 2) % 2 == 0) {
				spawnRandomizedParticles(world, centerX, centerY, centerZ, ParticleTypes.SOUL_FIRE_FLAME, 6);
			}
		} else if (TemperatureColorProvider.isInNearMelt(temp, maxTemp)) {
			if ((particleCounter + randomSeed * 2) % 2 == 0) {
				spawnRandomizedParticles(world, centerX, centerY, centerZ, ParticleTypes.FLAME, 10);
			}
		} else if (TemperatureColorProvider.isInMolten(temp, maxTemp)) {
			if ((particleCounter + randomSeed * 2) % 1 == 0) {
				spawnRandomizedParticles(world, centerX, centerY, centerZ, ParticleTypes.FLAME, 18);
				spawnRandomizedParticles(world, centerX, centerY, centerZ, ParticleTypes.SMALL_FLAME, 12);
			}
			if ((particleCounter + randomSeed * 4) % 2 == 0) {
				spawnRandomizedParticles(world, centerX, centerY, centerZ, ParticleTypes.SMOKE, 4);
			}
		}
	}

	private void spawnRandomizedParticles(World world, double centerX, double centerY, double centerZ, net.minecraft.particle.ParticleEffect particleType, int count) {
		for (int i = 0; i < count; i++) {
			double randomOffsetX = (Math.random() - 0.5) * 0.3;
			double randomOffsetY = (Math.random() - 0.5) * 0.3;
			double randomOffsetZ = (Math.random() - 0.5) * 0.3;
			world.addParticle(particleType, centerX + randomOffsetX, centerY + randomOffsetY, centerZ + randomOffsetZ, 0, 0.01, 0);
		}
	}
}
