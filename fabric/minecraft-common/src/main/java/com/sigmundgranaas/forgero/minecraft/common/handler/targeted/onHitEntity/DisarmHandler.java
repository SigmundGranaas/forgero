package com.sigmundgranaas.forgero.minecraft.common.handler.targeted.onHitEntity;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.property.v2.feature.HandlerBuilder;
import com.sigmundgranaas.forgero.core.property.v2.feature.JsonBuilder;
import lombok.Getter;
import lombok.experimental.Accessors;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

/**
 * Represents a handler that disarms the targeted entity upon being hit.
 *
 * <p>Example JSON configuration:
 * <pre>
 * {
 *   "type": "minecraft:on_hit",
 *   "on_hit": {
 *     "type": "forgero:disarm",
 *     "target": "minecraft:targeted_entity"
 *   }
 * }
 * </pre>
 * </p>
 */
@Getter
@Accessors(fluent = true)
public class DisarmHandler implements OnHitHandler {

	public static final String TYPE = "forgero:disarm";
	public static final JsonBuilder<OnHitHandler> BUILDER = HandlerBuilder.fromObject(OnHitHandler.KEY.clazz(), DisarmHandler::fromJson);

	private final String target;

	/**
	 * Constructs a new {@link DisarmHandler} with the specified target.
	 *
	 * @param target The target entity.
	 */
	public DisarmHandler(String target) {
		this.target = target;
	}

	/**
	 * Constructs an {@link DisarmHandler} from a JSON object.
	 *
	 * @param json The JSON object.
	 * @return A new instance of {@link DisarmHandler}.
	 */
	public static DisarmHandler fromJson(JsonObject json) {
		String target = json.get("target").getAsString();
		return new DisarmHandler(target);
	}

	/**
	 * This method is triggered upon hitting an entity.
	 * It checks the target specification and disarms the targeted entity if conditions are met.
	 *
	 * @param source       The source entity.
	 * @param world        The world where the event occurred.
	 * @param targetEntity The targeted entity.
	 */
	@Override
	public void onHit(Entity source, World world, Entity targetEntity) {
		if ("minecraft:targeted_entity".equals(target) && !world.isClient && targetEntity instanceof LivingEntity livingTarget) {
			// Get the item from the main hand
			ItemStack mainHandStack = livingTarget.getMainHandStack();
			if (!mainHandStack.isEmpty()) {

				// Drop the item in the world
				ItemEntity itemEntity = new ItemEntity(world, livingTarget.getX(), livingTarget.getY(), livingTarget.getZ(), mainHandStack);
				world.spawnEntity(itemEntity);

				// Empty the main hand of the entity
				livingTarget.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
			}
		}
	}
}
