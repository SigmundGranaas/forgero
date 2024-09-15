package com.sigmundgranaas.forgero.handler.targeted.onHitEntity;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.property.v2.feature.HandlerBuilder;
import com.sigmundgranaas.forgero.core.property.v2.feature.JsonBuilder;
import lombok.Getter;
import lombok.experimental.Accessors;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.world.World;

/**
 * Represents a handler that strikes the targeted entity with lightning upon being hit.
 *
 * <p>Example JSON configuration:
 * <pre>
 * {
 *   "type": "minecraft:on_hit",
 *   "on_hit": {
 *     "type": "minecraft:lightning_strike",
 *     "target": "minecraft:targeted_entity"
 *   }
 * }
 * </pre>
 * </p>
 */
@Getter
@Accessors(fluent = true)
public class LightningStrikeHandler implements EntityTargetHandler {

	public static final String TYPE = "minecraft:lightning_strike";
	public static final JsonBuilder<LightningStrikeHandler> BUILDER = HandlerBuilder.fromObject(LightningStrikeHandler.class, LightningStrikeHandler::fromJson);

	private final String target;

	/**
	 * Constructs a new {@link LightningStrikeHandler} with the specified target.
	 *
	 * @param target The target entity.
	 */
	public LightningStrikeHandler(String target) {
		this.target = target;
	}

	/**
	 * Constructs an {@link LightningStrikeHandler} from a JSON object.
	 *
	 * @param json The JSON object.
	 * @return A new instance of {@link LightningStrikeHandler}.
	 */
	public static LightningStrikeHandler fromJson(JsonObject json) {
		String target = json.get("target").getAsString();
		return new LightningStrikeHandler(target);
	}

	/**
	 * This method is triggered upon hitting an entity.
	 * It checks the target specification and triggers a lightning strike if conditions are met.
	 *
	 * @param source       The source entity.
	 * @param world        The world where the event occurred.
	 * @param targetEntity The targeted entity.
	 */
	@Override
	public void onHit(Entity source, World world, Entity targetEntity) {
		if ("minecraft:targeted_entity".equals(target) && !world.isClient) {
			LightningEntity lightningBolt = EntityType.LIGHTNING_BOLT.create(world);
			lightningBolt.refreshPositionAfterTeleport(targetEntity.getX(), targetEntity.getY(), targetEntity.getZ());
			world.spawnEntity(lightningBolt);
		}
	}
}
