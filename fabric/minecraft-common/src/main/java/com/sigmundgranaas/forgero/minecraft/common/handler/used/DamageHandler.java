package com.sigmundgranaas.forgero.minecraft.common.handler.used;

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
 * </pre>
 * </p>
 */
@Getter
@Accessors(fluent = true)
public class DamageHandler implements AfterUseHandler {
	public static final String TYPE = "minecraft:stack_damage";
	public static final JsonBuilder<DamageHandler> BUILDER = HandlerBuilder.fromObject(DamageHandler.class, DamageHandler::fromJson);

	private final int damage;

	public DamageHandler(int damage) {
		this.damage = damage;
	}

	/**
	 * Constructs an {@link DamageHandler} from a JSON object.
	 *
	 * @param json The JSON object.
	 * @return A new instance of {@link DamageHandler}.
	 */
	public static DamageHandler fromJson(JsonObject json) {
		int damage = json.get("damage").getAsInt();
		return new DamageHandler(damage);
	}

	@Override
	public void handle(Entity source, ItemStack target, Hand hand) {
		if (source instanceof LivingEntity livingEntity) {
			target.damage(damage, livingEntity, (entity) -> entity.sendToolBreakStatus(hand));
		} else {
			target.setDamage(target.getDamage() + damage);
		}
	}
}
