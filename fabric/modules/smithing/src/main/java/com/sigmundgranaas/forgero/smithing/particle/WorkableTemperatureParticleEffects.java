package com.sigmundgranaas.forgero.smithing.particle;

import com.sigmundgranaas.forgero.smithing.temperature.TemperatureRules;

import net.minecraft.item.ItemStack;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public final class WorkableTemperatureParticleEffects {
	private static final DefaultParticleType WORKABLE_WAX = ModParticles.WORKABLE_WAX;

	/*
	 * Subtle sparkle settings.
	 * Lower frequency + fewer particles keeps the item readable.
	 */
	private static final int BURST_INTERVAL_TICKS = 8;
	private static final int PARTICLES_PER_BURST = 2;

	/*
	 * Ring placement.
	 * Particles spawn around the item, not through the center.
	 */
	private static final double MIN_RING_RADIUS_FACTOR = 0.55D;
	private static final double MAX_RING_RADIUS_FACTOR = 0.95D;

	/*
	 * Small vertical range so the effect hugs the item.
	 */
	private static final double MIN_VERTICAL_OFFSET_FACTOR = 0.15D;
	private static final double MAX_VERTICAL_OFFSET_FACTOR = 0.75D;

	/*
	 * Very gentle motion.
	 * Sparkles should float, not look like smoke/fire.
	 */
	private static final double OUTWARD_SPEED = 0.004D;
	private static final double DRIFT_SPEED = 0.003D;
	private static final double RISE_SPEED = 0.008D;

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
			spawnSparkle(world, random, x, y, z, spread);
		}
	}

	private static void spawnSparkle(
			World world,
			Random random,
			double x,
			double y,
			double z,
			double spread
	) {
		double angle = random.nextDouble() * Math.PI * 2.0D;

		double radius = spread * randomRange(
				random,
				MIN_RING_RADIUS_FACTOR,
				MAX_RING_RADIUS_FACTOR
		);

		double offsetX = Math.cos(angle) * radius;
		double offsetZ = Math.sin(angle) * radius;

		double offsetY = spread * randomRange(
				random,
				MIN_VERTICAL_OFFSET_FACTOR,
				MAX_VERTICAL_OFFSET_FACTOR
		);

		/*
		 * Move slightly outward from the item center.
		 * This makes it feel like small heat sparkles around the metal,
		 * without covering the actual item texture.
		 */
		double velocityX = Math.cos(angle) * OUTWARD_SPEED + random.nextGaussian() * DRIFT_SPEED;
		double velocityY = RISE_SPEED + random.nextDouble() * RISE_SPEED;
		double velocityZ = Math.sin(angle) * OUTWARD_SPEED + random.nextGaussian() * DRIFT_SPEED;

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

	private static double randomRange(Random random, double min, double max) {
		return min + random.nextDouble() * (max - min);
	}
}
