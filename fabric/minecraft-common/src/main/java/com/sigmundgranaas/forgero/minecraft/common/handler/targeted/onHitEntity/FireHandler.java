package com.sigmundgranaas.forgero.minecraft.common.handler.targeted.onHitEntity;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.property.v2.feature.HandlerBuilder;
import com.sigmundgranaas.forgero.core.property.v2.feature.JsonBuilder;
import com.sigmundgranaas.forgero.minecraft.common.handler.targeted.onHitBlock.BlockTargetHandler;
import lombok.Getter;
import lombok.experimental.Accessors;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Represents a handler that sets the targeted entity on fire for a specified duration upon being hit.
 *
 * <p>Example JSON configuration:
 * <pre>
 * {
 *   "type": "minecraft:on_hit",
 *   "on_hit": {
 *     "type": "minecraft:fire",
 *     "target": "minecraft:targeted_entity",
 *     "duration": 5
 *   }
 * }
 * </pre>
 * </p>
 */
@Getter
@Accessors(fluent = true)
public class FireHandler implements EntityTargetHandler, BlockTargetHandler {
	public static final String TYPE = "minecraft:fire";
	public static final JsonBuilder<FireHandler> BUILDER = HandlerBuilder.fromObject(FireHandler.class, FireHandler::fromJson);

	private final String target;
	private final int duration;

	/**
	 * Constructs a new {@link FireHandler} with the specified target and duration.
	 *
	 * @param target   The target entity.
	 * @param duration The duration for which the entity will be on fire (in seconds).
	 */
	public FireHandler(String target, int duration) {
		this.target = target;
		this.duration = duration;
	}

	/**
	 * Constructs an {@link FireHandler} from a JSON object.
	 *
	 * @param json The JSON object.
	 * @return A new instance of {@link FireHandler}.
	 */
	public static FireHandler fromJson(JsonObject json) {
		String target = json.get("target").getAsString();
		int duration = json.get("duration").getAsInt();
		return new FireHandler(target, duration);
	}

	/**
	 * This method is triggered upon hitting an entity.
	 * It checks the target specification and sets the entity on fire if conditions are met.
	 *
	 * @param source       The source entity.
	 * @param world        The world where the event occurred.
	 * @param targetEntity The targeted entity.
	 */
	@Override
	public void onHit(Entity source, World world, Entity targetEntity) {
		if ("minecraft:targeted_entity".equals(target)) {
			targetEntity.setOnFireFor(duration);
		}
	}

	/**
	 * This method is triggered upon hitting a block.
	 * It checks the target specification and sets the entity on fire if conditions are met.
	 *
	 * @param source The source entity.
	 * @param world  The world where the event occurred.
	 * @param pos    The targeted entity.
	 */
	@Override
	public void onHit(Entity source, World world, BlockPos pos) {
		if ("minecraft:targeted_block".equals(target)) {
			// Get the block state at the targeted position
			BlockState blockState = world.getBlockState(pos);

			if (blockState.getMaterial().isBurnable()) {
				world.setBlockState(pos, Blocks.FIRE.getDefaultState(), 3);
			}
		}
	}
}
