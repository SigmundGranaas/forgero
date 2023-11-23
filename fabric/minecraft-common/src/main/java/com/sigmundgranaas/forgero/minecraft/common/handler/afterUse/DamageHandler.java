package com.sigmundgranaas.forgero.minecraft.common.handler.afterUse;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.property.v2.feature.HandlerBuilder;
import com.sigmundgranaas.forgero.core.property.v2.feature.JsonBuilder;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

/**
 * Represents a handler that damages the ItemStack after it has been used.
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
 *     "type": "minecraft:stack_damage",
 *     "damage": 50
 *   }
 * }
 * {
 *   "type": "minecraft:on_hit",
 *   "on_hit": {
 *     "type": "minecraft:explosion",
 *     "target": "minecraft:targeted_entity",
 *     "power": 3
 *   }
 *   "after_use": {
 *     "type": "minecraft:stack_damage",
 *     "percentage": 0.1 // This is 10%
 *   }
 * }
 * </pre>
 * </p>
 */
@Getter
@Accessors(fluent = true)
public class DamageHandler implements AfterUseHandler {
	public static final String TYPE = "minecraft:stack_damage";
	public static final JsonBuilder<DamageHandler> BUILDER = HandlerBuilder.fromObject(DamageHandler.class, DamageHandler::fromJson);

	private final int damage;
	private final float percentage;

	public DamageHandler(int damage, float percentage) {
		this.damage = damage;
		this.percentage = percentage;
	}

	/**
	 * Constructs an {@link DamageHandler} from a JSON object.
	 *
	 * @param json The JSON object.
	 * @return A new instance of {@link DamageHandler}.
	 */
	public static DamageHandler fromJson(JsonObject json) {
		int damage = json.has("damage") ? json.get("damage").getAsInt() : 0;
		float percentage = json.has("percentage") ? json.get("percentage").getAsFloat() : 0;
		return new DamageHandler(damage, percentage);
	}

	@Override
	public void handle(Entity source, ItemStack target, Hand hand) {
		int stackDamage = damage == 0 ? (int) (target.getMaxDamage() * percentage) : damage;
		if (source instanceof LivingEntity livingEntity) {
			target.damage(stackDamage, livingEntity, (entity) -> entity.sendToolBreakStatus(hand));
		} else {
			target.setDamage(target.getDamage() + stackDamage);
		}
	}
}
