package com.sigmundgranaas.forgero.effects.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Random;

/**
 * Handler that teleports entities randomly or in their look direction.
 *
 * <h3>JSON Example:</h3>
 * <pre>
 * {
 *   "type": "forgero:teleport",
 *   "target": "self",
 *   "random": true,
 *   "onGround": false,
 *   "maxDistance": 10
 * }
 * </pre>
 *
 * <h3>Target Options:</h3>
 * <ul>
 *   <li>"self" - Teleports the attacker</li>
 *   <li>"target" - Teleports the hit entity</li>
 * </ul>
 */
public record TeleportHandler(
		String target,
		boolean random,
		boolean onGround,
		int maxDistance
) implements ContextualEffectHandler {

	public static final String TYPE = "forgero:teleport";
	private static final Logger LOGGER = LoggerFactory.getLogger(TeleportHandler.class);
	private static final int MAX_ATTEMPTS = 10;

	public static final Codec<TeleportHandler> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.optionalFieldOf("target", "target").forGetter(TeleportHandler::target),
			Codec.BOOL.optionalFieldOf("random", false).forGetter(TeleportHandler::random),
			Codec.BOOL.optionalFieldOf("onGround", true).forGetter(TeleportHandler::onGround),
			Codec.INT.optionalFieldOf("maxDistance", 10).forGetter(TeleportHandler::maxDistance)
	).apply(instance, TeleportHandler::new));

	@Override
	public void apply(Entity source, Entity targetEntity) {
		Entity entityToTeleport = switch (target) {
			case "self", "attacker" -> source;
			case "target" -> targetEntity;
			default -> {
				LOGGER.warn("Unknown teleport target: {}. Using 'target'.", target);
				yield targetEntity;
			}
		};

		teleportEntity(entityToTeleport, entityToTeleport.getWorld());
	}

	@Override
	public String type() {
		return TYPE;
	}

	private void teleportEntity(Entity entity, World world) {
		if (random) {
			teleportRandomly(entity, world);
		} else {
			teleportInLookDirection(entity, world);
		}
	}

	private void teleportRandomly(Entity entity, World world) {
		Random random = new Random();
		for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
			double deltaX = random.nextDouble() * 2 - 1; // Random value between -1 and 1
			double deltaZ = random.nextDouble() * 2 - 1;
			double deltaY = !onGround ? (random.nextDouble() * 2 - 1) : 0;

			BlockPos newPos = entity.getBlockPos().add(
					(int) (deltaX * maxDistance),
					(int) (deltaY * maxDistance),
					(int) (deltaZ * maxDistance)
			);

			if (tryTeleport(entity, world, newPos)) {
				return;
			}
		}
		LOGGER.warn("Failed to find a safe teleportation location for entity {} after {} attempts",
				entity, MAX_ATTEMPTS);
	}

	private void teleportInLookDirection(Entity entity, World world) {
		Vec3d lookVec = entity.getRotationVec(1.0f);
		for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
			BlockPos newPos = entity.getBlockPos().add(
					(int) (lookVec.x * maxDistance),
					!onGround ? (int) (lookVec.y * maxDistance) : 0,
					(int) (lookVec.z * maxDistance)
			);

			if (tryTeleport(entity, world, newPos)) {
				return;
			}

			// Adjust look vector slightly for next attempt
			lookVec = lookVec.add(new Vec3d(0.1, 0.1, 0.1)).normalize();
		}
		LOGGER.warn("Failed to find a safe teleportation location for entity {} after {} attempts",
				entity, MAX_ATTEMPTS);
	}

	private boolean tryTeleport(Entity entity, World world, BlockPos newPos) {
		if (isSafeTeleportLocation(world, newPos)) {
			entity.teleport(newPos.getX() + 0.5, newPos.getY(), newPos.getZ() + 0.5);
			if (entity instanceof ServerPlayerEntity serverPlayer) {
				serverPlayer.networkHandler.sendPacket(
						new PlayerPositionLookS2CPacket(
								entity.getX(), entity.getY(), entity.getZ(),
								serverPlayer.getYaw(), serverPlayer.getPitch(),
								Collections.emptySet(), 0
						)
				);
			}
			return true;
		}
		return false;
	}

	private boolean isSafeTeleportLocation(World world, BlockPos pos) {
		// Check that there's space for the entity (3 blocks high)
		for (int yOffset = 0; yOffset <= 2; yOffset++) {
			BlockPos checkPos = pos.up(yOffset);
			BlockState state = world.getBlockState(checkPos);
			if (!state.isReplaceable() && !state.isAir()) {
				return false;
			}
		}
		// Check for solid ground below, or allow air if onGround is false
		return world.getBlockState(pos.down()).isSolid() || !onGround;
	}
}
