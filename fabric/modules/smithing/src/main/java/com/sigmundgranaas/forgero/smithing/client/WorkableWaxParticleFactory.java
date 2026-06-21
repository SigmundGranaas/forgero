package com.sigmundgranaas.forgero.smithing.client;

import com.sigmundgranaas.forgero.smithing.particle.ModParticles;

import net.minecraft.client.particle.GlowParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.particle.DefaultParticleType;

import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;

public final class WorkableWaxParticleFactory {
	private static final float WORKABLE_RED = 1.0F;
	private static final float WORKABLE_GREEN = 0.74F;
	private static final float WORKABLE_BLUE = 0.22F;

	private WorkableWaxParticleFactory() {
	}

	public static void register() {
		ParticleFactoryRegistry.getInstance().register(ModParticles.WORKABLE_WAX, provider -> {
			ParticleFactory<DefaultParticleType> waxOnFactory = new GlowParticle.WaxOnFactory(provider);

			return (parameters, world, x, y, z, velocityX, velocityY, velocityZ) -> {
				Particle particle = waxOnFactory.createParticle(parameters, world, x, y, z, velocityX, velocityY, velocityZ);

				if (particle != null) {
					particle.setColor(WORKABLE_RED, WORKABLE_GREEN, WORKABLE_BLUE);
					particle.setVelocity(velocityX, velocityY, velocityZ);
				}

				return particle;
			};
		});
	}
}
