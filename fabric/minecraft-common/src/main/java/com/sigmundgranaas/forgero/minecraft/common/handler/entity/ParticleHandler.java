package com.sigmundgranaas.forgero.minecraft.common.handler.entity;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.property.v2.feature.HandlerBuilder;
import com.sigmundgranaas.forgero.core.property.v2.feature.JsonBuilder;
import com.sigmundgranaas.forgero.minecraft.common.handler.targeted.onHitBlock.OnHitBlockHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.targeted.onHitEntity.OnHitHandler;
import lombok.Getter;
import lombok.experimental.Accessors;

import net.minecraft.entity.Entity;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Represents a handler that displays specified particle effects when triggered.
 * The handler can be configured using a JSON format.
 *
 * <p><b>Example Configurations:</b></p>
 *
 * <pre>
 * 1. Simple particle display:
 * {
 *   "type": "minecraft:particle",
 *   "particle": "minecraft:happy_villager"
 * }
 * </pre>
 *
 * <pre>
 * 2. Complex particle configuration with outward behavior:
 * {
 *   "type": "minecraft:particle",
 *   "particle": "minecraft:flame",
 *   "count": 10,
 *   "velocity": {"x": 0.1, "y": 0.5, "z": -0.2},
 *   "velocityRandomness": {"x": 0.05, "y": 0.1, "z": 0.05},
 *   "behavior": "OUTWARD",
 *   "offset": {"x": 0.0, "y": 1.0, "z": 0.0}
 * }
 * </pre>
 *
 * <pre>
 * 3. Particle with an inward behavior and velocity randomness:
 * {
 *   "type": "minecraft:particle",
 *   "particle": "minecraft:heart",
 *   "count": 5,
 *   "velocity": {"x": 0, "y": 0.2, "z": 0},
 *   "velocityRandomness": {"x": 0.1, "y": 0.1, "z": 0.1},
 *   "behavior": "INWARD",
 *   "offset": {"x": 0.5, "y": 0.5, "z": 0.5}
 * }
 * </pre>
 *
 * <pre>
 * 4. Particle configuration with no directional behavior and specific offset:
 * {
 *   "type": "minecraft:particle",
 *   "particle": "minecraft:smoke",
 *   "count": 3,
 *   "velocity": {"x": 0, "y": 0.1, "z": 0},
 *   "offset": {"x": -1.0, "y": 0.5, "z": 1.0}
 * }
 * </pre>
 *
 * <p>
 * Note: If certain fields like "count", "velocity", or "offset" are absent,
 * they default to 1, 0, and (0, 0, 0) respectively.
 * </p>
 */
@Getter
@Accessors(fluent = true)
public class ParticleHandler implements EntityHandler, OnHitBlockHandler, OnHitHandler {
	public static final String TYPE = "minecraft:particle";
	public static final JsonBuilder<ParticleHandler> BUILDER = HandlerBuilder.fromObject(ParticleHandler.class, ParticleHandler::fromJson);

	private final Identifier particleId;
	private final int count;
	private final Vec3d velocity;
	private final Vec3d velocityRandomness;
	private final DirectionalBehavior behavior;
	private final Vec3d offset;
	private final double spread;

	/**
	 * Constructs a new {@link ParticleHandler} with the specified properties.
	 *
	 * @param particleId Identifier for the particle to be displayed.
	 * @param count      Number of particles to display.
	 * @param velocity   Velocity of particles.
	 * @param offset     Offset for particle spawn position.
	 */
	public ParticleHandler(Identifier particleId, int count, Vec3d velocity, Vec3d offset, Vec3d velocityRandomness, DirectionalBehavior behavior, double spread) {
		this.particleId = particleId;
		this.count = count;
		this.velocity = velocity;
		this.velocityRandomness = velocityRandomness;
		this.behavior = behavior;
		this.offset = offset;
		this.spread = spread;
	}

	/**
	 * Constructs a {@link ParticleHandler} from a JSON object.
	 *
	 * @param json The JSON object.
	 * @return A new instance of {@link ParticleHandler}.
	 */
	public static ParticleHandler fromJson(JsonObject json) {
		Identifier particleId = new Identifier(json.get("particle").getAsString());
		int count = json.has("count") ? json.get("count").getAsInt() : 1;
		double spread = json.has("spread") ? json.get("spread").getAsDouble() : 1;


		Vec3d offset = getVec3dFromJson(json, "offset");
		Vec3d velocity = getVec3dFromJson(json, "velocity");
		Vec3d velocityRandomness = getVec3dFromJson(json, "velocityRandomness");

		DirectionalBehavior behavior = DirectionalBehavior.NONE;
		if (json.has("behavior")) {
			behavior = DirectionalBehavior.valueOf(json.get("behavior").getAsString().toUpperCase());
		}

		return new ParticleHandler(particleId, count, velocity, offset, velocityRandomness, behavior, spread);
	}

	private static Vec3d getVec3dFromJson(JsonObject jsonObject, String key) {
		if (!jsonObject.has(key)) {
			return Vec3d.ZERO;
		}

		JsonObject vecJson = jsonObject.getAsJsonObject(key);
		double x = vecJson.has("x") ? vecJson.get("x").getAsDouble() : 0;
		double y = vecJson.has("y") ? vecJson.get("y").getAsDouble() : 0;
		double z = vecJson.has("z") ? vecJson.get("z").getAsDouble() : 0;

		return new Vec3d(x, y, z);
	}


	private void spawnParticles(Entity entity, Vec3d pos) {
		ParticleType<?> particle = Registries.PARTICLE_TYPE.get(particleId);

		if (particle instanceof DefaultParticleType defaultParticleType && entity != null) {
			for (int i = 0; i < count; i++) {
				double velX = velocity.x;
				double velY = velocity.y;
				double velZ = velocity.z;

				if (behavior == DirectionalBehavior.INWARD) {
					Vec3d direction = entity.getPos().subtract(pos).normalize();
					velX *= direction.x;
					velY *= direction.y;
					velZ *= direction.z;
				} else if (behavior == DirectionalBehavior.OUTWARD) {
					Vec3d direction = pos.subtract(entity.getPos()).normalize();
					velX *= direction.x;
					velY *= direction.y;
					velZ *= direction.z;
				}

				// Incorporate the randomness AFTER calculating behavior-based direction
				velX += (Math.random() - 0.5) * 2 * velocityRandomness.x;
				velY += (Math.random() - 0.5) * 2 * velocityRandomness.y;
				velZ += (Math.random() - 0.5) * 2 * velocityRandomness.z;

				// Randomized spawn position within maxDistanceFromPos from the original pos
				double spawnX = pos.x + (Math.random() - 0.5) * 2 * spread;
				double spawnY = pos.y + (Math.random() - 0.5) * 2 * spread;
				double spawnZ = pos.z + (Math.random() - 0.5) * 2 * spread;

				entity.getWorld().addParticle(defaultParticleType, spawnX, spawnY, spawnZ, velX, velY, velZ);
			}
		}
	}


	@Override
	public void onHit(Entity root, World world, BlockPos pos) {
		spawnParticles(root, new Vec3d(pos.getX(), pos.getY(), pos.getZ()));
	}

	@Override
	public void onHit(Entity root, World world, Entity target) {
		spawnParticles(root, target.getPos());
	}

	@Override
	public void handle(Entity rootEntity) {
		spawnParticles(rootEntity, rootEntity.getPos());
	}

	// Enumeration for specifying directional behavior of particles
	public enum DirectionalBehavior {
		NONE, INWARD, OUTWARD
	}
}
