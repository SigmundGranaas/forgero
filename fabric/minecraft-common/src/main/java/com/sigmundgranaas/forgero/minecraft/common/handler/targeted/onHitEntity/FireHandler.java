package com.sigmundgranaas.forgero.minecraft.common.handler.targeted.onHitEntity;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.property.v2.feature.HandlerBuilder;
import com.sigmundgranaas.forgero.core.property.v2.feature.JsonBuilder;
import lombok.Getter;
import lombok.experimental.Accessors;

import net.minecraft.entity.Entity;
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
public class FireHandler implements OnHitHandler {
	public static final String TYPE = "minecraft:fire";
	public static final JsonBuilder<OnHitHandler> BUILDER = HandlerBuilder.fromObject(OnHitHandler.KEY.clazz(), FireHandler::fromJson);

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
}
