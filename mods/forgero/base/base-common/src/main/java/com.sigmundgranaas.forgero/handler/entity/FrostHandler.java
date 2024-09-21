package com.sigmundgranaas.forgero.handler.entity;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.property.v2.feature.HandlerBuilder;
import com.sigmundgranaas.forgero.core.property.v2.feature.JsonBuilder;
import com.sigmundgranaas.forgero.handler.targeted.onHitBlock.BlockTargetHandler;
import com.sigmundgranaas.forgero.handler.targeted.onHitEntity.EntityTargetHandler;
import lombok.Getter;
import lombok.experimental.Accessors;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;


/**
 * Represents a handler for applying the freezing effect to entities. This implementation directly uses the freezetick property on the entity class.
 *
 * <p>Example usage:</p>
 * <pre>
 *     {
 *         "type": "minecraft:on_hit",
 *         "on_hit": [
 *           {
 *             "type": "minecraft:particle",
 *             "particle": "minecraft:snowflake",
 *             "count": 20,
 *             "spread": 1.5
 *           },
 *           {
 *             "type": "minecraft:frost",
 *             "ticks": 100,
 *             "mode": "add"
 *           }
 *         ]
 *       }
 * </pre>
 */
@Getter
@Accessors(fluent = true)
public class FrostHandler implements EntityBasedHandler, BlockTargetHandler, EntityTargetHandler {
	public static final String TYPE = "minecraft:frost";
	public static final JsonBuilder<FrostHandler> BUILDER = HandlerBuilder.fromObject(FrostHandler.class, FrostHandler::fromJson);

	private final int frostTicks;
	private final String mode;


	public FrostHandler(int frostTicks, String mode) {
		this.frostTicks = frostTicks;
		this.mode = mode;
	}

	public static FrostHandler fromJson(JsonObject json) {
		int frostTicks = json.has("ticks") ? json.get("ticks").getAsInt() : 10;
		String mode = json.has("mode") ? json.get("mode").getAsString() : "add";

		return new FrostHandler(frostTicks, mode);
	}

	private void applyFrostToEntity(Entity entity) {
		if (this.mode().equals("add")) {
			entity.setFrozenTicks(entity.getFrozenTicks() + this.frostTicks());
		} else {
			entity.setFrozenTicks(frostTicks);
		}
	}

	@Override
	public void onHit(Entity root, World world, Entity target) {
		applyFrostToEntity(target);
	}

	@Override
	public void handle(Entity rootEntity) {
		applyFrostToEntity(rootEntity);
	}
}
