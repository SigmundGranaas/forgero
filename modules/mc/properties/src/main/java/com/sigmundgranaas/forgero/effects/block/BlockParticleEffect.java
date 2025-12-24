package com.sigmundgranaas.forgero.effects.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Spawns particles when a block is hit.
 *
 * <h3>JSON Configuration Example:</h3>
 * <pre>
 * {
 *   "type": "forgero:block_particle",
 *   "particle": "minecraft:flame",
 *   "count": 10,
 *   "spread": 0.5
 * }
 * </pre>
 */
public record BlockParticleEffect(
		String particle,
		int count,
		double spread
) implements OnHitBlockEffect {
	public static final String TYPE = "forgero:block_particle";

	public static final Codec<BlockParticleEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("particle").forGetter(BlockParticleEffect::particle),
			Codec.INT.optionalFieldOf("count", 10).forGetter(BlockParticleEffect::count),
			Codec.DOUBLE.optionalFieldOf("spread", 0.5).forGetter(BlockParticleEffect::spread)
	).apply(instance, BlockParticleEffect::new));

	@Override
	public void apply(World world, Entity source, BlockPos pos) {
		if (!(world instanceof ServerWorld serverWorld)) {
			return;
		}

		Identifier particleId = new Identifier(particle);
		ParticleType<?> particleType = Registries.PARTICLE_TYPE.get(particleId);

		if (particleType != null && particleType instanceof ParticleEffect particleEffect) {
			serverWorld.spawnParticles(
					particleEffect,
					pos.getX() + 0.5,
					pos.getY() + 0.5,
					pos.getZ() + 0.5,
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
