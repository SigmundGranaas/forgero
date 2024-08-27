package com.sigmundgranaas.forgero.minecraft.common.handler.targeted;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.property.v2.feature.HandlerBuilder;
import com.sigmundgranaas.forgero.core.property.v2.feature.JsonBuilder;
import com.sigmundgranaas.forgero.minecraft.common.handler.targeted.onHitBlock.BlockTargetHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.targeted.onHitEntity.EntityTargetHandler;
import lombok.Getter;
import lombok.experimental.Accessors;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

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
 *     "power": 10
 *   }
 * }
 * </pre>
 * </p>
 */
@Getter
@Accessors(fluent = true)
public class ExplosionHandler implements EntityTargetHandler, BlockTargetHandler {
	public static final String TYPE = "minecraft:explosion";
	public static final JsonBuilder<ExplosionHandler> BUILDER = HandlerBuilder.fromObject(ExplosionHandler.class, ExplosionHandler::fromJson);

	private final float power;
	private final String target;

	public static String simpleExplosionComment = """
			Simple explosion configuration targeting the hit entity.
			This will create an explosion with power 5 at the location of the entity that was hit.
			""";
	public static String SIMPLE_EXPLOSION_EXAMPLE = """
			{
			    "type": "minecraft:explosion",
			    "target": "minecraft:targeted_entity",
			    "power": 5
			}
			""";

	public static String powerfulExplosionComment = """
			A more powerful explosion configuration targeting the hit entity.
			This will create a large explosion with power 10 at the location of the entity that was hit.
			""";
	public static String POWERFUL_EXPLOSION_EXAMPLE = """
			{
			    "type": "minecraft:explosion",
			    "target": "minecraft:targeted_entity",
			    "power": 10
			}
			""";

	public static String attackerExplosionComment = """
			Explosion configuration targeting the attacker.
			This will create an explosion with power 3 at the location of the entity that initiated the attack.
			""";
	public static String ATTACKER_EXPLOSION_EXAMPLE = """
			 {
			   "type": "minecraft:explosion",
			   "target": "minecraft:attacker",
			   "power": 3
			 }
			""";

	// Complete examples as features
	public static String blockExplosionComment = """
			Explosion configuration for hitting a block.
			This will create an explosion with power 2 at the location of the hit block.
			Note that the 'target' property is not needed for block explosions. But you can also use the "minecraft:attacker" for targeting the one hitting the block.
			""";
	public static String FEATURE_BLOCK_EXPLOSION = """
			{
			  "type": "minecraft:on_hit_block",
			  "on_hit": {
			    "type": "minecraft:explosion",
			    "power": 2
			  }
			}
			""";
	public static String entityExplosionComment = """
			Explosion configuration for hitting a block.
			This will create an explosion with power 2 at the location of the entity that is hit.
			""";
	public static String FEATURE_ENTITY_EXPLOSION = """
			{
			  "type": "minecraft:on_hit_block",
			  "on_hit": {
			    "type": "minecraft:explosion",
			    "power": 2
			  }
			}
			""";


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
			world.createExplosion(null, targetEntity.getX(), targetEntity.getY(), targetEntity.getZ(), power, World.ExplosionSourceType.TNT);
		} else if ("minecraft:attacker".equals(target) && !world.isClient) {
			world.createExplosion(null, source.getX(), source.getY(), source.getZ(), power, World.ExplosionSourceType.TNT);
		}
	}

	@Override
	public void onHit(Entity source, World world, BlockPos pos) {
		if ("minecraft:attacker".equals(target) && !world.isClient) {
			world.createExplosion(null, source.getX(), source.getY(), source.getZ(), power, World.ExplosionSourceType.TNT);
		} else if (!world.isClient) {
			world.createExplosion(null, pos.getX(), pos.getY(), pos.getZ(), power, World.ExplosionSourceType.TNT);
		}
	}
}
