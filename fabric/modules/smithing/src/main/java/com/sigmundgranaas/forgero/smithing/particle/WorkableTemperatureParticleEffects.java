package com.sigmundgranaas.forgero.smithing.particle;

import com.sigmundgranaas.forgero.smithing.temperature.TemperatureRules;

import net.minecraft.item.ItemStack;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public final class WorkableTemperatureParticleEffects {
	private static final DefaultParticleType WORKABLE_WAX = ModParticles.WORKABLE_WAX;
	private static final int BURST_INTERVAL_TICKS = 10;
	private static final int PARTICLES_PER_BURST = 3;
	private static final double HORIZONTAL_SPREAD_FACTOR = 1.75D;
	private static final double VERTICAL_SPREAD_FACTOR = 0.85D;
	private static final double DRIFT_SPEED = 0.01D;
	private static final double RISE_SPEED = 0.016D;

	private WorkableTemperatureParticleEffects() {
	}

	public static boolean isWorkable(ItemStack stack) {
		return TemperatureRules.isWorkable(stack);
	}

	public static void spawnIfWorkable(
			World world,
			ItemStack stack,
			double x,
			double y,
			double z,
			double spread
	) {
		if (world == null || !world.isClient || !isWorkable(stack)) {
			return;
		}

		if (world.getTime() % BURST_INTERVAL_TICKS != 0) {
			return;
		}

		spawnBurst(world, x, y, z, spread);
	}

	private static void spawnBurst(World world, double x, double y, double z, double spread) {
		Random random = world.random;

		for (int i = 0; i < PARTICLES_PER_BURST; i++) {
			spawnParticle(world, random, x, y, z, spread);
		}
	}

	private static void spawnParticle(World world, Random random, double x, double y, double z, double spread) {
		double horizontalSpread = spread * HORIZONTAL_SPREAD_FACTOR;
		double offsetX = centered(random) * horizontalSpread;
		double offsetY = random.nextDouble() * spread * VERTICAL_SPREAD_FACTOR;
		double offsetZ = centered(random) * horizontalSpread;
		double velocityX = random.nextGaussian() * DRIFT_SPEED;
		double velocityY = RISE_SPEED + random.nextDouble() * RISE_SPEED;
		double velocityZ = random.nextGaussian() * DRIFT_SPEED;

		world.addParticle(
				WORKABLE_WAX,
				x + offsetX,
				y + offsetY,
				z + offsetZ,
				velocityX,
				velocityY,
				velocityZ
		);
	}

	private static double centered(Random random) {
		return random.nextDouble() - 0.5D;
	}
}
