package com.sigmundgranaas.forgero.minecraft.common.handler.afterUse;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.property.v2.feature.HandlerBuilder;
import com.sigmundgranaas.forgero.core.property.v2.feature.JsonBuilder;
import com.sigmundgranaas.forgero.minecraft.common.handler.use.StopHandler;
import lombok.Getter;
import lombok.experimental.Accessors;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.world.World;


/**
 * Represents a handler that consumes an entire stack after it has been used.
 *
 * <p>Example JSON configuration for consuming an entire stack:
 * <pre>
 * {
 *   "type": "minecraft:on_hit",
 *   "on_hit": {
 *     "type": "minecraft:explosion",
 *     "target": "minecraft:targeted_entity",
 *     "power": 3
 *   }
 *   "after_use": {
 *     "type": "minecraft:consume_stack"
 *   }
 * }
 * </pre>
 * </p>
 *
 * <p>Example JSON configuration for consuming a specific amount of items from a stack:
 * <pre>
 * {
 *    "type": "minecraft:on_hit",
 *    "on_hit": {
 *      "type": "minecraft:explosion",
 *        "target": "minecraft:targeted_entity",
 *        "power": 3
 *    }
 *    "after_use": {
 *      "type": "minecraft:consume_stack",
 *      "count": 1
 *    }
 * }
 */
@Getter
@Accessors(fluent = true)
public class ConsumeStackHandler implements AfterUseHandler, StopHandler {
	public static final String TYPE = "minecraft:consume_stack";
	public static final JsonBuilder<ConsumeStackHandler> BUILDER = HandlerBuilder.fromObject(ConsumeStackHandler.class, ConsumeStackHandler::fromJson);
	private final int count;

	public ConsumeStackHandler(int count) {
		this.count = count;
	}

	public static ConsumeStackHandler fromJson(JsonObject json) {
		int count = json.has("count") ? json.get("count").getAsInt() : -1;
		return new ConsumeStackHandler(count);
	}

	@Override
	public void handle(Entity source, ItemStack target, Hand hand) {
		if (count > 0) {
			target.decrement(count);
		} else {
			if (source instanceof LivingEntity livingEntity) {
				livingEntity.setStackInHand(hand, ItemStack.EMPTY);
			} else if (source instanceof ItemEntity item) {
				item.remove(Entity.RemovalReason.DISCARDED);
			} else if (source instanceof ArrowEntity arrow) {
				arrow.remove(Entity.RemovalReason.DISCARDED);
			}
		}
	}

	@Override
	public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
		if (count > 0) {
			stack.decrement(count);
		} else {
			user.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
		}
	}
}
