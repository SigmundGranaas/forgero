package com.sigmundgranaas.forgero.effects.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.Set;

/**
 * Spawns entities at either the source or target entity's location.
 * Useful for summoning minions, spawning projectiles, or creating environmental effects.
 *
 * <h3>JSON Configuration Examples:</h3>
 *
 * <p><b>Summon Zombie Minions:</b></p>
 * <pre>
 * {
 *   "type": "forgero:spawn_entity",
 *   "entity": "minecraft:zombie",
 *   "count": 2,
 *   "offset": {"x": 1.0, "y": 0.0, "z": 0.0},
 *   "on_target": false
 * }
 * </pre>
 *
 * <p><b>Spawn Primed TNT:</b></p>
 * <pre>
 * {
 *   "type": "forgero:spawn_entity",
 *   "entity": "minecraft:tnt",
 *   "count": 1,
 *   "offset": {"x": 0.0, "y": 1.0, "z": 0.0},
 *   "on_target": true
 * }
 * </pre>
 *
 * <p><b>Spawn Experience Orbs:</b></p>
 * <pre>
 * {
 *   "type": "forgero:spawn_entity",
 *   "entity": "minecraft:experience_orb",
 *   "count": 5,
 *   "on_target": false
 * }
 * </pre>
 *
 * <h3>Use Cases:</h3>
 * <ul>
 *   <li>Necromancer weapons - summon undead on kill</li>
 *   <li>Explosive weapons - spawn TNT or fireballs</li>
 *   <li>Support items - spawn healing or experience orbs</li>
 *   <li>Arrow/projectile launchers - spawn custom projectiles</li>
 * </ul>
 *
 * <h3>Performance Notes:</h3>
 * Entity spawning is relatively expensive. Recommended to limit count to <= 5 for
 * frequently triggered effects. Invalid entity identifiers fail gracefully with no crash.
 *
 * <h3>Important Notes:</h3>
 * <ul>
 *   <li>Some entities require additional initialization (e.g., setting velocity for projectiles)</li>
 *   <li>Spawned tame entities are automatically owned by the source player</li>
 *   <li>Server spawn limits prevent excessive entity spawning (max 10 per trigger)</li>
 *   <li>Dangerous entities (ender_dragon, wither) are blocked for security</li>
 *   <li>Offset can be relative to entity rotation or world coordinates</li>
 * </ul>
 *
 * @param entity The entity type identifier to spawn (e.g., "minecraft:zombie", "minecraft:arrow")
 * @param count Number of entities to spawn (1-10, clamped for safety)
 * @param offset Position offset from spawn location
 * @param onTarget If true, spawns at target entity; if false, spawns at source entity
 * @param relativeOffset If true, offset is relative to entity rotation; if false, uses world coordinates
 * @param setOwner If true, attempts to set spawner as owner for tameable/projectile entities
 */
public record SpawnEntityHandler(Identifier entity, int count, Vec3d offset, boolean onTarget, boolean relativeOffset, boolean setOwner) implements ContextualEffectHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(SpawnEntityHandler.class);
	public static final String TYPE = "forgero:spawn_entity";

	// Security: Maximum entities per spawn to prevent server lag/griefing
	private static final int MAX_SPAWN_COUNT = 10;

	// Security: Blocklist of dangerous entities that should not be spawnable
	private static final Set<Identifier> BLOCKED_ENTITIES = Set.of(
			new Identifier("minecraft", "ender_dragon"),
			new Identifier("minecraft", "wither"),
			new Identifier("minecraft", "giant")
	);

	public static final Codec<SpawnEntityHandler> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Identifier.CODEC.fieldOf("entity").forGetter(SpawnEntityHandler::entity),
			Codec.intRange(1, MAX_SPAWN_COUNT).optionalFieldOf("count", 1).forGetter(SpawnEntityHandler::count),
			Vec3d.CODEC.optionalFieldOf("offset", Vec3d.ZERO).forGetter(SpawnEntityHandler::offset),
			Codec.BOOL.optionalFieldOf("on_target", true).forGetter(SpawnEntityHandler::onTarget),
			Codec.BOOL.optionalFieldOf("relative_offset", false).forGetter(SpawnEntityHandler::relativeOffset),
			Codec.BOOL.optionalFieldOf("set_owner", true).forGetter(SpawnEntityHandler::setOwner)
	).apply(instance, SpawnEntityHandler::new));

	// Compact constructor for validation
	public SpawnEntityHandler {
		// Clamp count to safe range
		count = MathHelper.clamp(count, 1, MAX_SPAWN_COUNT);
	}

	@Override
	public void apply(Entity source, Entity targetEntity) {
		if (source.getWorld().isClient) {
			return; // Server-side only
		}

		// Security check: Block dangerous entities
		if (BLOCKED_ENTITIES.contains(entity)) {
			LOGGER.warn("Blocked attempt to spawn dangerous entity: {}", entity);
			return;
		}

		// Determine spawn location
		Entity spawnLocation = onTarget ? targetEntity : source;

		// Look up entity type from registry
		Optional<EntityType<?>> entityType = Registries.ENTITY_TYPE.getOrEmpty(entity);

		if (entityType.isEmpty()) {
			// Invalid entity type - fail gracefully with logging
			LOGGER.debug("Invalid entity identifier in spawn effect: {}", entity);
			return;
		}

		// Spawn the specified number of entities
		for (int i = 0; i < count; i++) {
			Entity spawnedEntity = entityType.get().create(spawnLocation.getWorld());

			if (spawnedEntity != null) {
				// Calculate spawn position with offset
				Vec3d spawnOffset = offset;

				// Apply rotation-relative offset if requested
				if (relativeOffset) {
					float yaw = spawnLocation.getYaw();
					float pitch = spawnLocation.getPitch();
					spawnOffset = rotateOffset(offset, yaw, pitch);
				}

				double x = spawnLocation.getX() + spawnOffset.x;
				double y = spawnLocation.getY() + spawnOffset.y;
				double z = spawnLocation.getZ() + spawnOffset.z;

				// Set entity position
				spawnedEntity.refreshPositionAfterTeleport(x, y, z);

				// Set ownership/targeting if requested
				if (setOwner && source instanceof PlayerEntity player) {
					setEntityOwner(spawnedEntity, player);
				}

				// Spawn entity into the world
				spawnLocation.getWorld().spawnEntity(spawnedEntity);
			}
		}
	}

	/**
	 * Rotates an offset vector based on entity yaw and pitch.
	 * Allows spawning entities relative to where the entity is facing.
	 *
	 * @param offset The offset to rotate
	 * @param yaw Entity yaw in degrees
	 * @param pitch Entity pitch in degrees
	 * @return Rotated offset vector
	 */
	private Vec3d rotateOffset(Vec3d offset, float yaw, float pitch) {
		// Convert degrees to radians
		float yawRad = yaw * (float) (Math.PI / 180.0);
		float pitchRad = pitch * (float) (Math.PI / 180.0);

		// Rotate around Y axis (yaw)
		double cosYaw = Math.cos(yawRad);
		double sinYaw = Math.sin(yawRad);
		double rotatedX = offset.x * cosYaw - offset.z * sinYaw;
		double rotatedZ = offset.x * sinYaw + offset.z * cosYaw;

		// Rotate around X axis (pitch) - affects forward/backward
		double cosPitch = Math.cos(-pitchRad);
		double sinPitch = Math.sin(-pitchRad);
		double rotatedY = offset.y * cosPitch - rotatedZ * sinPitch;
		rotatedZ = offset.y * sinPitch + rotatedZ * cosPitch;

		return new Vec3d(rotatedX, rotatedY, rotatedZ);
	}

	/**
	 * Attempts to set the spawning player as the owner of spawned entities.
	 * Works for tameable entities, projectiles, and certain mobs.
	 *
	 * @param entity The spawned entity
	 * @param player The player who spawned it
	 */
	private void setEntityOwner(Entity entity, PlayerEntity player) {
		// Set owner for tameable entities (wolves, cats, parrots, etc.)
		if (entity instanceof TameableEntity tameable) {
			tameable.setOwner(player);
			tameable.setTamed(true);
		}

		// Set owner for projectiles (arrows, snowballs, etc.)
		if (entity instanceof ProjectileEntity projectile) {
			projectile.setOwner(player);
		}

		// Set target for certain mobs (zombies, skeletons will target player's enemies)
		if (entity instanceof MobEntity mob) {
			// Don't make them hostile to the player
			// Future: Could implement ally system here
		}
	}

	@Override
	public String type() {
		return TYPE;
	}
}
