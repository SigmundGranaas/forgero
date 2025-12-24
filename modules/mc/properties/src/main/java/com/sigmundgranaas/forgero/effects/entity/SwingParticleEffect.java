package com.sigmundgranaas.forgero.effects.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;

/**
 * Spawns particles when hand is swung.
 *
 * <h3>JSON Configuration Example:</h3>
 * <pre>
 * {
 *   "type": "forgero:swing_particle",
 *   "particle": "minecraft:sweep_attack",
 *   "count": 5,
 *   "spread": 0.3
 * }
 * </pre>
 */
public record SwingParticleEffect(
		String particle,
		int count,
		double spread
) implements SwingEffect {
	public static final String TYPE = "forgero:swing_particle";

	public static final Codec<SwingParticleEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("particle").forGetter(SwingParticleEffect::particle),
			Codec.INT.optionalFieldOf("count", 5).forGetter(SwingParticleEffect::count),
			Codec.DOUBLE.optionalFieldOf("spread", 0.3).forGetter(SwingParticleEffect::spread)
	).apply(instance, SwingParticleEffect::new));

	@Override
	public void apply(Entity source, Hand hand) {
		if (!(source.getWorld() instanceof ServerWorld serverWorld)) {
			return;
		}

		Identifier particleId = new Identifier(particle);
		ParticleType<?> particleType = Registries.PARTICLE_TYPE.get(particleId);

		if (particleType != null && particleType instanceof ParticleEffect particleEffect) {
			serverWorld.spawnParticles(
					particleEffect,
					source.getX(),
					source.getY() + source.getStandingEyeHeight() * 0.5,
					source.getZ(),
					count,
					spread,
					spread,
					spread,
					0.0
			);
		}
	}

	@Override
	public String type() {
		return TYPE;
	}
}
