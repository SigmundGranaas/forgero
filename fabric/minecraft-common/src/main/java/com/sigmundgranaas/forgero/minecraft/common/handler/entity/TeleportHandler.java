package com.sigmundgranaas.forgero.minecraft.common.handler.entity;

import java.util.Collections;
import java.util.Random;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.property.v2.feature.HandlerBuilder;
import com.sigmundgranaas.forgero.core.property.v2.feature.JsonBuilder;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.apache.logging.log4j.Logger;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Represents a handler that teleports the entity based on the configured parameters.
 *
 * <p>Example JSON configuration:
 * <pre>
 * {
 *   "type": "minecraft:teleport",
 *   "teleport": {
 *     "random": true,
 *     "onGround": false,
 *     "maxDistance": 10
 *   }
 * }
 * </pre>
 * </p>
 */
@Getter
@Accessors(fluent = true)
public class TeleportHandler implements EntityBasedHandler {
	public static final String TYPE = "minecraft:teleport";
	public static final JsonBuilder<TeleportHandler> BUILDER = HandlerBuilder.fromObject(TeleportHandler.class, TeleportHandler::fromJson);

	private static final int MAX_ATTEMPTS = 10;
	private static final Logger LOGGER = Forgero.LOGGER;
	private final boolean random;
	private final boolean onGround;
	private final int maxDistance;
	private final String target;

	/**
	 * Constructs a new {@link TeleportHandler} with the specified parameters.
	 *
	 * @param random      Whether to teleport in a random direction.
	 * @param onGround    Whether the teleportation can occur in the air.
	 * @param maxDistance Distance to teleport in the look direction.
	 * @param target      Which entity to teleport.
	 */
	public TeleportHandler(boolean random, boolean onGround, int maxDistance, String target) {
		this.random = random;
		this.onGround = onGround;
		this.maxDistance = maxDistance;
		this.target = target;
	}

	/**
	 * Constructs a {@link TeleportHandler} from a JSON object.
	 *
	 * @param json The JSON object.
	 * @return A new instance of {@link TeleportHandler}.
	 */
	public static TeleportHandler fromJson(JsonObject json) {
		boolean randomDirection = json.has("random") && json.get("random").getAsBoolean();
		boolean airTeleport = json.has("onGround") && json.get("onGround").getAsBoolean();
		int lookDirectionDistance = json.has("maxDistance") ? json.get("maxDistance").getAsInt() : 10;
		String target = json.has("target") ? json.get("target").getAsString() : "target";

		return new TeleportHandler(randomDirection, airTeleport, lookDirectionDistance, target);
	}

	/**
	 * This method is triggered upon hitting an entity.
	 * Teleports the entity based on the configured parameters.
	 *
	 * @param source       The source entity.
	 * @param world        The world where the event occurred.
	 * @param targetEntity The targeted entity.
	 */
	@Override
	public void onHit(Entity source, World world, Entity targetEntity) {
		if ("minecraft:targeted_entity".equals(target)) {
			teleportEntity(targetEntity, world);
		} else if ("minecraft:attacker".equals(target) || "minecraft:self".equals(target)) {
			teleportEntity(source, world);
		}
	}

	@Override
	public void handle(Entity entity) {
		teleportEntity(entity, entity.getWorld());
	}

	/**
	 * This method is triggered upon hitting a block.
	 * Teleports the entity or block based on the configured parameters.
	 *
	 * @param source The source entity.
	 * @param world  The world where the event occurred.
	 * @param pos    The position of the hit block.
	 */
	@Override
	public void onHit(Entity source, World world, BlockPos pos) {
		if ("minecraft:self".equals(target)) {
			teleportEntity(source, world);
		} else if ("minecraft:hit_position".equals(target)) {
			teleportToPosition(source, world, pos);
		} else if ("minecraft:targeted_block".equals(target)) {
			teleportBlock(world, pos);
		}
	}

	private void teleportBlock(World world, BlockPos originalPos) {
		BlockState blockState = world.getBlockState(originalPos);

		if (blockState.isAir()) {
			return; // Don't teleport air blocks
		}

		for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
			BlockPos newPos = getRandomPosition(originalPos);

			if (isSafeBlockTeleportLocation(world, newPos)) {
				// Remove the block from its original position
				world.setBlockState(originalPos, Blocks.AIR.getDefaultState());

				// Place the block in the new position
				world.setBlockState(newPos, blockState);

				// If the block has a BlockEntity (like a chest), move its data
				BlockEntity blockEntity = world.getBlockEntity(originalPos);
				if (blockEntity != null) {
					NbtCompound nbt = blockEntity.createNbt();
					world.removeBlockEntity(originalPos);
					BlockEntity newBlockEntity = world.getBlockEntity(newPos);
					if (newBlockEntity != null) {
						newBlockEntity.readNbt(nbt);
					}
				}

				return;
			}
		}
		LOGGER.warn("Failed to find a safe teleportation location for block at {} after {} attempts", originalPos, MAX_ATTEMPTS);
	}


	private BlockPos getRandomPosition(BlockPos originalPos) {
		Random random = new Random();
		int deltaX = random.nextInt(maxDistance * 2 + 1) - maxDistance;
		int deltaY = onGround ? 0 : random.nextInt(maxDistance * 2 + 1) - maxDistance;
		int deltaZ = random.nextInt(maxDistance * 2 + 1) - maxDistance;
		return originalPos.add(deltaX, deltaY, deltaZ);
	}

	private boolean isSafeBlockTeleportLocation(World world, BlockPos pos) {
		return world.getBlockState(pos).isAir() &&
				(world.getBlockState(pos.down()).isSolid() || !onGround);
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
			double deltaZ = random.nextDouble() * 2 - 1; // Random value between -1 and 1
			double deltaY = !onGround ? (random.nextDouble() * 2 - 1) : 0; // Random Y if air teleport is allowed

			BlockPos newPos = entity.getBlockPos().add((int) (deltaX * maxDistance),
					(int) (deltaY * maxDistance),
					(int) (deltaZ * maxDistance));

			if (tryTeleport(entity, world, newPos)) {
				return;
			}
		}
		LOGGER.warn("Failed to find a safe teleportation location for entity {} after {} attempts", entity, MAX_ATTEMPTS);
	}

	private void teleportToPosition(Entity entity, World world, BlockPos pos) {
		if (tryTeleport(entity, world, pos)) {
			return;
		}
		if (tryTeleport(entity, world, pos.up())) {
			return;
		}
		if (tryTeleport(entity, world, pos.down())) {
			return;
		}
		LOGGER.warn("Failed to find a safe teleportation location for entity {} after {} attempts", entity, MAX_ATTEMPTS);
	}

	private void teleportInLookDirection(Entity entity, World world) {
		Vec3d lookVec = entity.getCameraPosVec(0);
		for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
			BlockPos newPos = entity.getBlockPos().add((int) (lookVec.x * maxDistance),
					!onGround ? (int) (lookVec.y * maxDistance) : 0,
					(int) (lookVec.z * maxDistance));

			if (tryTeleport(entity, world, newPos)) {
				return;
			}

			// Adjust look vector slightly for next attempt
			lookVec = lookVec.add(new Vec3d(0.1, 0.1, 0.1)).normalize();
		}
		LOGGER.warn("Failed to find a safe teleportation location for entity {} after {} attempts", entity, MAX_ATTEMPTS);
	}

	private boolean isSafeTeleportLocation(World world, BlockPos pos) {
		for (int yOffset = 0; yOffset <= 2; yOffset++) {
			BlockPos checkPos = pos.up(yOffset);
			if (!world.getBlockState(checkPos).isReplaceable()) {
				return false;
			}
		}
		return world.getBlockState(pos.down()).isSolid() || !onGround;
	}

	private boolean tryTeleport(Entity entity, World world, BlockPos newPos) {
		if (isSafeTeleportLocation(world, newPos)) {
			entity.teleport(newPos.getX() + 0.5, newPos.getY(), newPos.getZ() + 0.5);
			if (entity instanceof ServerPlayerEntity serverPlayer) {
				serverPlayer.networkHandler.sendPacket(new PlayerPositionLookS2CPacket(entity.getX(), entity.getY(), entity.getZ(), serverPlayer.getYaw(), serverPlayer.getPitch(), Collections.emptySet(), 0));
			}
			return true;
		}
		return false;
	}
}
