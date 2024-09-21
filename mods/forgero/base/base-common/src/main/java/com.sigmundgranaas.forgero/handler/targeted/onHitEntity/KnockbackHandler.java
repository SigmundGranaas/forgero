package com.sigmundgranaas.forgero.handler.targeted.onHitEntity;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.property.v2.feature.HandlerBuilder;
import com.sigmundgranaas.forgero.core.property.v2.feature.JsonBuilder;
import lombok.Getter;
import lombok.experimental.Accessors;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Represents a handler that applies knockback to the targeted entity upon being hit.
 * The direction (push or pull) and the force of the knockback can be configured.
 *
 * <p>Example JSON configuration:
 * <pre>
 * {
 *   "type": "minecraft:on_hit",
 *   "on_hit": {
 *     "type": "minecraft:knockback",
 *     "target": "minecraft:targeted_entity",
 *     "force": 1.5,
 *     "direction": "push"
 *   }
 * }
 * </pre>
 * </p>
 */
@Getter
@Accessors(fluent = true)
public class KnockbackHandler implements EntityTargetHandler {

	public static final String TYPE = "minecraft:knockback";
	public static final JsonBuilder<KnockbackHandler> BUILDER = HandlerBuilder.fromObject(KnockbackHandler.class, KnockbackHandler::fromJson);

	private final String target;
	private final double force;
	private final String direction;

	/**
	 * Constructs a new {@link KnockbackHandler} with the specified target, force, and direction.
	 *
	 * @param target    The target entity.
	 * @param force     The force of the knockback.
	 * @param direction The direction (either "push" or "pull").
	 */
	public KnockbackHandler(String target, double force, String direction) {
		this.target = target;
		this.force = force;
		this.direction = direction;
	}

	/**
	 * Constructs a {@link KnockbackHandler} from a JSON object.
	 *
	 * @param json The JSON object.
	 * @return A new instance of {@link KnockbackHandler}.
	 */
	public static KnockbackHandler fromJson(JsonObject json) {
		String target = json.get("target").getAsString();
		double force = json.get("force").getAsDouble();
		String direction = json.get("direction").getAsString();

		return new KnockbackHandler(target, force, direction);
	}

	/**
	 * This method is triggered upon hitting an entity.
	 * Applies the configured knockback to the targeted entity.
	 *
	 * @param source       The source entity.
	 * @param world        The world where the event occurred.
	 * @param targetEntity The targeted entity.
	 */
	@Override
	public void onHit(Entity source, World world, Entity targetEntity) {
		if ("minecraft:targeted_entity".equals(target)) {
			Vec3d vec = targetEntity.getPos().subtract(source.getPos()).normalize();
			if ("pull".equals(direction)) {
				vec = vec.multiply(-1); // Reverse the direction for pull
			}
			targetEntity.addVelocity(vec.x * force, vec.y * force, vec.z * force);
		}
	}

}
