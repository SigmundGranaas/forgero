package com.sigmundgranaas.forgero.minecraft.common.handler.entity;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.property.v2.feature.HandlerBuilder;
import com.sigmundgranaas.forgero.core.property.v2.feature.JsonBuilder;
import lombok.Getter;
import lombok.experimental.Accessors;

import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.Random;

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
		if(this.target.equals("target")){
			teleportEntity(targetEntity, world);
		}else{
			teleportEntity(source, world);
		}
	}

	@Override
	public void handle(Entity entity) {
		teleportEntity(entity, entity.getWorld());
	}

	/**
	 * This method is triggered upon hitting a block.
	 * Teleports the entity based on the configured parameters.
	 *
	 * @param source The source entity.
	 * @param world  The world where the event occurred.
	 * @param pos    The targeted entity.
	 */
	@Override
	public void onHit(Entity source, World world, BlockPos pos) {
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
		double deltaX = random.nextDouble() * 2 - 1; // Random value between -1 and 1
		double deltaZ = random.nextDouble() * 2 - 1; // Random value between -1 and 1
		double deltaY = !onGround ? (random.nextDouble() * 2 - 1) : 0; // Random Y if air teleport is allowed

		BlockPos newPos = entity.getBlockPos().add((int) (deltaX * maxDistance),
				(int) (deltaY * maxDistance),
				(int) (deltaZ * maxDistance));
		teleportToPosition(entity, world, newPos);
	}

	private void teleportInLookDirection(Entity entity, World world) {
		Vec3d lookVec = entity.getCameraPosVec(0);
		BlockPos newPos = entity.getBlockPos().add((int) (lookVec.x * maxDistance),
				!onGround ? (int) (lookVec.y * maxDistance) : 0,
				(int) (lookVec.z * maxDistance));
		teleportToPosition(entity, world, newPos);
	}

	private boolean isSafeTeleportLocation(World world, BlockPos pos) {
		for (int yOffset = 0; yOffset <= 2; yOffset++) {
			BlockPos checkPos = pos.up(yOffset);
			if (!world.getBlockState(checkPos).isReplaceable()) {
				return false;
			}
		}
		return true;
	}

	private void teleportToPosition(Entity entity, World world, BlockPos newPos) {
		if (isSafeTeleportLocation(world, newPos)) {
			entity.teleport(newPos.getX() + 0.5, newPos.getY(), newPos.getZ() + 0.5);
			if (entity instanceof ServerPlayerEntity serverPlayer) {
				((ServerPlayerEntity)entity).networkHandler.sendPacket(new PlayerPositionLookS2CPacket(entity.getX(), entity.getY(), entity.getZ(), serverPlayer.getYaw(), serverPlayer.getPitch(), Collections.emptySet(), 0));
			}
		}
	}
}
