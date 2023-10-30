package com.sigmundgranaas.forgero.minecraft.common.handler.targeted;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.property.v2.feature.HandlerBuilder;
import com.sigmundgranaas.forgero.core.property.v2.feature.JsonBuilder;
import com.sigmundgranaas.forgero.minecraft.common.handler.targeted.onHitBlock.OnHitBlockHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.targeted.onHitEntity.OnHitHandler;
import lombok.Getter;
import lombok.experimental.Accessors;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;

/**
 * Represents a handler that triggers an explosion around entities upon hitting a target.
 * The handler can be configured using a JSON format.
 *
 * <p>Example JSON configuration:
 * <pre>
 * {
 *   "type": "minecraft:on_hit",
 *   "on_hit": {
 *     "type": "minecraft:explosion",
 *     "target": "minecraft:targeted_entity",
 *     "power": 3
 *   }
 * }
 * </pre>
 * </p>
 */
@Getter
@Accessors(fluent = true)
public class ExplosionHandler implements OnHitHandler, OnHitBlockHandler {
	public static final String TYPE = "minecraft:explosion";
	public static final JsonBuilder<ExplosionHandler> BUILDER = HandlerBuilder.fromObject(ExplosionHandler.class, ExplosionHandler::fromJson);

	private final float power;
	private final String target;

	/**
	 * Constructs a new {@link ExplosionHandler} with the specified properties.
	 *
	 * @param power  The power of the explosion effect.
	 * @param target The target entity.
	 */
	public ExplosionHandler(float power, String target) {
		this.power = power;
		this.target = target;
	}

	/**
	 * Constructs an {@link ExplosionHandler} from a JSON object.
	 *
	 * @param json The JSON object.
	 * @return A new instance of {@link ExplosionHandler}.
	 */
	public static ExplosionHandler fromJson(JsonObject json) {
		float power = json.get("power").getAsFloat();
		String target = "minecraft:targeted_entity";
		if (json.has("target")) {
			target = json.get("target").getAsString();
		}
		return new ExplosionHandler(power, target);
	}

	/**
	 * This method is triggered upon hitting an entity.
	 * It checks the target specification and triggers an explosion effect if conditions are met.
	 *
	 * @param source       The source entity.
	 * @param world        The world where the event occurred.
	 * @param targetEntity The targeted entity.
	 */
	@Override
	public void onHit(Entity source, World world, Entity targetEntity) {
		if ("minecraft:targeted_entity".equals(target) && !world.isClient) {
			world.createExplosion(targetEntity, targetEntity.getX(), targetEntity.getY(), targetEntity.getZ(), power, Explosion.DestructionType.BREAK);
		}
	}

	@Override
	public void onHit(Entity root, World world, BlockPos pos) {
		if ("minecraft:targeted_entity".equals(target) && !world.isClient) {
			world.createExplosion(root, pos.getX(), pos.getY(), pos.getZ(), power, Explosion.DestructionType.BREAK);
		}
	}
}
