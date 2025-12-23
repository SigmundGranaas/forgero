package com.sigmundgranaas.forgero.effects.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

/**
 * Spawns particle effects at either the source or target entity's location.
 * Provides visual feedback for weapon effects and abilities.
 *
 * <h3>JSON Configuration Examples:</h3>
 *
 * <p><b>Flame Impact:</b></p>
 * <pre>
 * {
 *   "type": "forgero:particle",
 *   "particle": "minecraft:flame",
 *   "count": 20,
 *   "speed": 0.05,
 *   "spread": 1.0,
 *   "target": "target"
 * }
 * </pre>
 *
 * <p><b>Soul Aura:</b></p>
 * <pre>
 * {
 *   "type": "forgero:particle",
 *   "particle": "minecraft:soul",
 *   "count": 50,
 *   "speed": 0.02,
 *   "spread": 0.5,
 *   "target": "source"
 * }
 * </pre>
 *
 * <h3>Common Particles:</h3>
 * <ul>
 *   <li>minecraft:flame - Fire particles</li>
 *   <li>minecraft:smoke - Smoke particles</li>
 *   <li>minecraft:crit - Critical hit stars</li>
 *   <li>minecraft:enchant - Enchantment symbols</li>
 *   <li>minecraft:dragon_breath - Purple dragon particles</li>
 *   <li>minecraft:soul - Blue soul particles</li>
 * </ul>
 *
 * <h3>Use Cases:</h3>
 * <ul>
 *   <li>Impact effects - spawn particles on hit</li>
 *   <li>Ability visualization - show particle clouds for active effects</li>
 *   <li>Trail effects - continuous particle spawning via OnTick</li>
 * </ul>
 *
 * <h3>Performance Notes:</h3>
 * Particle spawning is server-side only. Large particle counts may impact performance.
 * Recommended: count <= 50 for frequent effects, <= 100 for rare effects.
 *
 * @param particle The identifier of the particle type (e.g., "minecraft:flame", "minecraft:soul")
 * @param count Number of particles to spawn (1-100)
 * @param speed Particle velocity multiplier (0.0-2.0)
 * @param spread Particle spread radius (0.0-5.0)
 * @param target Whether to spawn particles at SOURCE or TARGET location
 */
public record ParticleHandler(Identifier particle, int count, double speed, double spread, ParticleTarget target) implements ContextualEffectHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(ParticleHandler.class);
	public static final String TYPE = "forgero:particle";
	public static final Codec<ParticleHandler> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Identifier.CODEC.fieldOf("particle").forGetter(ParticleHandler::particle),
			Codec.intRange(1, 100).optionalFieldOf("count", 10).forGetter(ParticleHandler::count),
			Codec.doubleRange(0.0, 2.0).optionalFieldOf("speed", 0.1).forGetter(ParticleHandler::speed),
			Codec.doubleRange(0.0, 5.0).optionalFieldOf("spread", 0.5).forGetter(ParticleHandler::spread),
			ParticleTarget.CODEC.optionalFieldOf("target", ParticleTarget.TARGET).forGetter(ParticleHandler::target)
	).apply(instance, ParticleHandler::new));

	// Compact constructor for validation
	public ParticleHandler {
		if (count > 50) {
			LOGGER.warn("Large particle count ({}). May impact performance. Recommended: <= 50", count);
		}
	}

	@Override
	public void apply(Entity source, Entity targetEntity) {
		Entity particleLocation = (target == ParticleTarget.SOURCE) ? source : targetEntity;

		if (particleLocation.getWorld() instanceof ServerWorld serverWorld) {
			ParticleType<?> particleType = Registries.PARTICLE_TYPE.get(particle);

			if (particleType == null) {
				LOGGER.debug("Invalid particle identifier in effect: {}", particle);
				return;
			}

			// Handle different particle types correctly
			if (particleType instanceof DefaultParticleType defaultType) {
				// Simple particles like flame, smoke, crit
				serverWorld.spawnParticles(
						defaultType,
						particleLocation.getX(),
						particleLocation.getEyeY(), // Eye height for better visibility
						particleLocation.getZ(),
						count,
						spread, spread, spread, // x, y, z spread
						speed
				);
			} else if (particleType instanceof ParticleEffect particleEffect) {
				// Complex particles (rare case, but supported)
				serverWorld.spawnParticles(
						particleEffect,
						particleLocation.getX(),
						particleLocation.getEyeY(),
						particleLocation.getZ(),
						count,
						spread, spread, spread,
						speed
				);
			} else {
				// Unsupported particle type
				LOGGER.debug("Unsupported particle type: {} ({})", particle, particleType.getClass().getSimpleName());
			}
		}
	}

	@Override
	public String type() {
		return TYPE;
	}

	/**
	 * Determines where particles should be spawned.
	 */
	public enum ParticleTarget implements StringIdentifiable {
		/** Spawn particles at the source entity (attacker) */
		SOURCE,
		/** Spawn particles at the target entity (victim) */
		TARGET;

		public static final Codec<ParticleTarget> CODEC = StringIdentifiable.createCodec(
				ParticleTarget::values,
				(value) -> String.valueOf(ParticleTarget.valueOf(value.toUpperCase(Locale.ROOT)))
		);

		@Override
		public String asString() {
			return this.name().toLowerCase(Locale.ROOT);
		}
	}
}
