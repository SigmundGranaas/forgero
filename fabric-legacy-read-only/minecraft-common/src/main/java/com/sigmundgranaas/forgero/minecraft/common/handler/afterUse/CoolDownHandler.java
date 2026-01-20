package com.sigmundgranaas.forgero.minecraft.common.handler.afterUse;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.property.v2.feature.HandlerBuilder;
import com.sigmundgranaas.forgero.core.property.v2.feature.JsonBuilder;
import com.sigmundgranaas.forgero.minecraft.common.handler.use.StopHandler;
import lombok.Getter;
import lombok.experimental.Accessors;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.world.World;


/**
 * Represents a handler that sets a cooldown on the item after it has been used.
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
 *   "after_use": {
 *     "type": "minecraft:cooldown",
 *     "duration": 50
 *   }
 * }
 * </pre>
 * </p>
 */
@Getter
@Accessors(fluent = true)
public class CoolDownHandler implements AfterUseHandler, StopHandler {
	public static final String TYPE = "minecraft:cooldown";
	public static final JsonBuilder<CoolDownHandler> BUILDER = HandlerBuilder.fromObject(CoolDownHandler.class, CoolDownHandler::fromJson);
	private final int duration;

	public CoolDownHandler(int duration) {
		this.duration = duration;
	}

	/**
	 * Constructs an {@link CoolDownHandler} from a JSON object.
	 *
	 * @param json The JSON object.
	 * @return A new instance of {@link CoolDownHandler}.
	 */
	public static CoolDownHandler fromJson(JsonObject json) {
		int damage = json.get("duration").getAsInt();
		return new CoolDownHandler(damage);
	}

	@Override
	public void handle(Entity source, ItemStack target, Hand hand) {
		if (source instanceof ServerPlayerEntity player) {
			ItemCooldownManager cooldownManager = player.getItemCooldownManager();
			cooldownManager.set(target.getItem(), duration);
		}
	}


	@Override
	public void stoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
		if (user instanceof ServerPlayerEntity player) {
			ItemCooldownManager cooldownManager = player.getItemCooldownManager();
			cooldownManager.set(stack.getItem(), duration);
		}
	}
}
