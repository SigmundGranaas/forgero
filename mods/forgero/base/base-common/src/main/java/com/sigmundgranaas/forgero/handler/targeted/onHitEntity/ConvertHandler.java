package com.sigmundgranaas.forgero.handler.targeted.onHitEntity;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.property.v2.feature.HandlerBuilder;
import com.sigmundgranaas.forgero.core.property.v2.feature.JsonBuilder;
import lombok.Getter;
import lombok.experimental.Accessors;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.world.World;

/**
 * Represents a handler that converts the targeted entity to a specified entity type upon being hit.
 * The resulting entity type can be configured.
 *
 * <p>Example JSON configuration:
 * <pre>
 * {
 *   "type": "minecraft:on_hit",
 *   "on_hit": {
 *     "type": "minecraft:convert",
 *     "convert_to": "minecraft:zombie",
 *     "target": "minecraft:targeted_entity"
 *   }
 * }
 * </pre>
 * </p>
 */
@Getter
@Accessors(fluent = true)
public class ConvertHandler implements EntityTargetHandler {

	public static final String TYPE = "minecraft:convert";
	public static final JsonBuilder<ConvertHandler> BUILDER = HandlerBuilder.fromObject(ConvertHandler.class, ConvertHandler::fromJson);

	private final String target;
	private final String convertTo;

	/**
	 * Constructs a new {@link ConvertHandler} with the specified target and convertTo type.
	 *
	 * @param target    The target entity.
	 * @param convertTo The entity type to convert to.
	 */
	public ConvertHandler(String target, String convertTo) {
		this.target = target;
		this.convertTo = convertTo;
	}

	/**
	 * Constructs a {@link ConvertHandler} from a JSON object.
	 *
	 * @param json The JSON object.
	 * @return A new instance of {@link ConvertHandler}.
	 */
	public static ConvertHandler fromJson(JsonObject json) {
		String target = json.get("target").getAsString();
		String convertTo = json.get("convert_to").getAsString();

		return new ConvertHandler(target, convertTo);
	}

	/**
	 * This method is triggered upon hitting an entity.
	 * Converts the targeted entity to the specified type.
	 *
	 * @param source       The source entity.
	 * @param world        The world where the event occurred.
	 * @param targetEntity The targeted entity.
	 */
	@Override
	public void onHit(Entity source, World world, Entity targetEntity) {
		if ("minecraft:targeted_entity".equals(target) && !world.isClient) {
			Entity newEntity = EntityType.get(convertTo).get().create(world);
			newEntity.copyPositionAndRotation(targetEntity);
			world.spawnEntity(newEntity);
			targetEntity.remove(Entity.RemovalReason.DISCARDED);
		}
	}
}
