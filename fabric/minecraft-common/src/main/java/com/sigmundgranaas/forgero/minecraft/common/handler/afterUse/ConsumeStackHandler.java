package com.sigmundgranaas.forgero.minecraft.common.handler.afterUse;

import com.sigmundgranaas.forgero.core.property.v2.feature.HandlerBuilder;
import com.sigmundgranaas.forgero.core.property.v2.feature.JsonBuilder;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;


/**
 * Represents a handler that consumes an entire stack after it has been used.
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
 *     "type": "minecraft:consume_stack"
 *   }
 * }
 * </pre>
 * </p>
 */
@Getter
@Accessors(fluent = true)
public class ConsumeStackHandler implements AfterUseHandler {
	public static final String TYPE = "minecraft:consume_stack";
	public static final JsonBuilder<ConsumeStackHandler> BUILDER = HandlerBuilder.fromObject(ConsumeStackHandler.class, (json) -> new ConsumeStackHandler());

	@Override
	public void handle(Entity source, ItemStack target, Hand hand) {
		if (source instanceof LivingEntity livingEntity) {
			livingEntity.setStackInHand(hand, ItemStack.EMPTY);
		} else if (source instanceof ItemEntity item) {
			item.remove(Entity.RemovalReason.DISCARDED);
		} else if (source instanceof ArrowEntity arrow) {
			arrow.remove(Entity.RemovalReason.DISCARDED);
		}
	}
}
