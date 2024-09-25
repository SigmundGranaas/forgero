package com.sigmundgranaas.forgero.minecraft.common.handler.use;

import com.sigmundgranaas.forgero.core.property.v2.feature.ClassKey;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public interface UsageTickHandler {
	ClassKey<UsageTickHandler> KEY = new ClassKey<>("minecraft:usage_tick_handler", UsageTickHandler.class);

	 void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks);
}
