package com.sigmundgranaas.forgero.smithing.particle;

import com.sigmundgranaas.forgero.smithing.temperature.TemperatureRules;

import net.minecraft.item.ItemStack;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public final class WorkableTemperatureParticleEffects {
	/*
	 * END_ROD is a small white sparkle-like vanilla particle.
	 * This looks much cleaner than a wax/smoke style particle.
	 */
	private static final DefaultParticleType WORKABLE_SPARKLE = ParticleTypes.WAX_OFF;

	private static final int BURST_INTERVAL_TICKS = 8;
	private static final int PARTICLES_PER_BURST = 1;

	/*
	 * Spawn outside the center so the item stays visible.
	 */
	private static final double MIN_RING_RADIUS_FACTOR = 1.15D;
	private static final double MAX_RING_RADIUS_FACTOR = 1.65D;

	private static final double MIN_VERTICAL_OFFSET_FACTOR = 0.25D;
	private static final double MAX_VERTICAL_OFFSET_FACTOR = 0.95D;

	/*
	 * Very low movement so it feels like a shimmer/sparkle,
	 * not smoke or fire.
	 */
	private static final double OUTWARD_SPEED = 0.0015D;
	private static final double DRIFT_SPEED = 0.001D;
	private static final double RISE_SPEED = 0.0035D;

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

		double velocityX = Math.cos(angle) * OUTWARD_SPEED + random.nextGaussian() * DRIFT_SPEED;
		double velocityY = RISE_SPEED + random.nextDouble() * RISE_SPEED;
		double velocityZ = Math.sin(angle) * OUTWARD_SPEED + random.nextGaussian() * DRIFT_SPEED;

		world.addParticle(
				WORKABLE_SPARKLE,
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
