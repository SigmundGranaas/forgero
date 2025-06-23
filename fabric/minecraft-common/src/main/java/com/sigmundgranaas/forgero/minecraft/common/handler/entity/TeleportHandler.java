package com.sigmundgranaas.forgero.minecraft.common.handler.entity;

import java.util.Optional;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.property.v2.feature.HandlerBuilder;
import com.sigmundgranaas.forgero.core.property.v2.feature.JsonBuilder;
import lombok.Getter;
import lombok.experimental.Accessors;

import net.minecraft.util.math.random.Random;

import org.apache.logging.log4j.Logger;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Represents a handler that teleports the entity based on the configured parameters.
 *
 * <p>Example JSON configuration:
 * <pre>
 * {
 *   "type": "minecraft:teleport",
 *    "target": "minecraft:targeted_entity",
 *    "random": true,
 *    "on_ground": false,
 *    "max_distance": 10
 * }
 * </pre>
 * </p>
 */
@Getter
@Accessors(fluent = true)
public class TeleportHandler implements EntityBasedHandler {
	public static final String TYPE = "minecraft:teleport";
	public static final JsonBuilder<TeleportHandler> BUILDER = HandlerBuilder.fromObject(TeleportHandler.class, TeleportHandler::fromJson);

	private static final int MAX_SEARCH_ATTEMPTS = 16;
	private static final int MAX_SEARCH_RADIUS = 5;
	private static final Logger LOGGER = Forgero.LOGGER;

	private final boolean random;
	private final boolean onGround;
	private final int maxDistance;
	private final String target;

	/**
	 * Constructs a new {@link TeleportHandler} with the specified parameters.
	 *
	 * @param random      Whether to teleport in a random direction.
	 * @param onGround    Whether the teleportation must be on ground.
	 * @param maxDistance Maximum distance to teleport.
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
		boolean random = json.has("random") && json.get("random").getAsBoolean();
		boolean onGround = json.has("on_ground") && json.get("on_ground").getAsBoolean();
		int maxDistance = json.has("max_distance") ? json.get("max_distance").getAsInt() : 10;
		String target = json.has("target") ? json.get("target").getAsString() : "minecraft:self";
		return new TeleportHandler(random, onGround, maxDistance, target);
	}

	@Override
	public void onHit(Entity source, World world, Entity targetEntity) {
		if (world.isClient) return;
		Entity entityToTeleport = getEntityToTeleport(source, targetEntity);
		if (entityToTeleport != null) {
			teleportEntity(entityToTeleport, world);
		}
	}

	@Override
	public void handle(Entity entity) {
		if (entity.getWorld().isClient) return;
		if ("minecraft:self".equals(target)) {
			teleportEntity(entity, entity.getWorld());
		}
	}

	@Override
	public void onHit(Entity source, World world, BlockPos pos) {
		if (world.isClient) return;
		switch (target) {
			case "minecraft:self", "minecraft:attacker" -> teleportEntity(source, world);
			case "minecraft:hit_position" -> teleportToPosition(source, world, Vec3d.ofCenter(pos));
			case "minecraft:targeted_block" -> teleportBlock(world, pos);
			default -> LOGGER.warn("Not a valid block target for teleportation: {}", target);
		}
	}

	private void teleportEntity(Entity entity, World world) {
		for (int i = 0; i < MAX_SEARCH_ATTEMPTS; i++) {
			Optional<Vec3d> targetPos = getTargetPos(entity, world);
			if (targetPos.isEmpty()) continue;

			Optional<Vec3d> safePos = findSafeTeleportLocation(entity, world, targetPos.get());
			if (safePos.isPresent()) {
				tryTeleport(entity, safePos.get());
				return;
			}
		}
		LOGGER.warn("Failed to find a safe teleportation location for entity {}", entity.getName().getString());
	}

	private void teleportToPosition(Entity entity, World world, Vec3d pos) {
		findSafeTeleportLocation(entity, world, pos).ifPresent(safePos -> tryTeleport(entity, safePos));
	}


	private Entity getEntityToTeleport(Entity source, Entity targetEntity) {
		return switch (target) {
			case "minecraft:targeted_entity" -> targetEntity;
			case "minecraft:attacker", "minecraft:self" -> source;
			default -> {
				LOGGER.warn("Not a valid entity target for teleportation: {}", target);
				yield null;
			}
		};
	}


	private Optional<Vec3d> getTargetPos(Entity entity, World world) {
		Vec3d currentPos = entity.getPos();
		if (random) {
			Random rand = world.getRandom();
			// Ensure we always teleport at least 1 block away to prevent staying in place.
			double distance = maxDistance > 1 ? 1 + rand.nextDouble() * (maxDistance - 1) : maxDistance;
			if (distance == 0) {
				return Optional.of(currentPos);
			}

			if (onGround) {
				double angle = rand.nextDouble() * 2.0 * Math.PI;
				return Optional.of(new Vec3d(currentPos.x + Math.cos(angle) * distance, currentPos.y, currentPos.z + Math.sin(angle) * distance));
			} else {
				Vec3d randomVec = new Vec3d(rand.nextGaussian(), rand.nextGaussian(), rand.nextGaussian()).normalize();
				return Optional.of(currentPos.add(randomVec.multiply(distance)));
			}
		} else {
			Vec3d lookVec = entity.getRotationVector().multiply(maxDistance);
			return Optional.of(currentPos.add(lookVec));
		}
	}

	private Optional<Vec3d> findSafeTeleportLocation(Entity entity, World world, Vec3d target) {
		BlockPos.Mutable mutable = new BlockPos(MathHelper.floor(target.x), MathHelper.floor(target.y), MathHelper.floor(target.z)).mutableCopy();

		if (onGround) {
			// Search for ground below the target
			for (int i = 0; i < MAX_SEARCH_RADIUS && world.getBlockState(mutable.down()).isAir(); i++) {
				mutable.setY(mutable.getY() - 1);
			}
		}

		if (isSafeForEntity(entity, world, mutable)) {
			return Optional.of(Vec3d.ofBottomCenter(mutable));
		}

		// Spiral search if initial position is not safe
		for (int r = 1; r <= MAX_SEARCH_RADIUS; r++) {
			for (int i = 0; i < r * 8; i++) { // Check all points on the perimeter of a square of radius r
				BlockPos pos = new BlockPos(MathHelper.floor(target.x), MathHelper.floor(target.y), MathHelper.floor(target.z)).add(getSpiralOffset(i, r));
				if (isSafeForEntity(entity, world, pos)) {
					return Optional.of(Vec3d.ofBottomCenter(pos));
				}
			}
		}
		return Optional.empty();
	}

	private BlockPos getSpiralOffset(int i, int r) {
		// Simplified spiral logic
		int sideLength = r * 2;
		int side = i / sideLength;
		int indexOnSide = i % sideLength - r;

		return switch (side) {
			case 0 -> new BlockPos(indexOnSide, 0, r); // Top
			case 1 -> new BlockPos(r, 0, -indexOnSide); // Right
			case 2 -> new BlockPos(-indexOnSide, 0, -r); // Bottom
			default -> new BlockPos(-r, 0, indexOnSide); // Left
		};
	}

	private boolean isSafeForEntity(Entity entity, World world, BlockPos pos) {
		if (onGround && world.getBlockState(pos.down()).isAir()) {
			return false;
		}
		// Check if entity bounding box at new position is clear
		Box newBoundingBox = entity.getBoundingBox().offset(Vec3d.ofBottomCenter(pos).subtract(entity.getPos()));
		return world.isSpaceEmpty(entity, newBoundingBox);
	}

	private void tryTeleport(Entity entity, Vec3d pos) {
		if (entity instanceof ServerPlayerEntity player) {
			player.teleport((ServerWorld) player.getWorld(), pos.x, pos.y, pos.z, player.getYaw(), player.getPitch());
		} else {
			entity.teleport(pos.x, pos.y, pos.z);
		}
	}

	private void teleportBlock(World world, BlockPos originalPos) {
		BlockState blockState = world.getBlockState(originalPos);
		if (blockState.isAir()) {
			return;
		}
		// Complex logic, for now, we leave it simple
		for (int i = 0; i < 10; i++) {
			Random rand = world.getRandom();
			BlockPos newPos = originalPos.add(rand.nextInt(maxDistance * 2 + 1) - maxDistance, rand.nextInt(maxDistance * 2 + 1) - maxDistance, rand.nextInt(maxDistance * 2 + 1) - maxDistance);
			if (world.getBlockState(newPos).isAir() && (!onGround || !world.getBlockState(newPos.down()).isAir())) {
				BlockEntity blockEntity = world.getBlockEntity(originalPos);
				NbtCompound nbt = null;
				if (blockEntity != null) {
					nbt = blockEntity.createNbt();
					world.removeBlockEntity(originalPos);
				}
				world.setBlockState(originalPos, Blocks.AIR.getDefaultState());
				world.setBlockState(newPos, blockState);
				if (nbt != null) {
					BlockEntity newBlockEntity = world.getBlockEntity(newPos);
					if (newBlockEntity != null) {
						newBlockEntity.readNbt(nbt);
					}
				}
				return;
			}
		}
	}
}
