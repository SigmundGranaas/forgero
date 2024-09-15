package com.sigmundgranaas.forgero.handler.use;

import com.sigmundgranaas.forgero.core.property.v2.feature.ClassKey;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

public interface EntityUseHandler extends BaseHandler {
	ClassKey<EntityUseHandler> KEY = new ClassKey<>("minecraft:entity_use_handler", EntityUseHandler.class);

	ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand);
}
