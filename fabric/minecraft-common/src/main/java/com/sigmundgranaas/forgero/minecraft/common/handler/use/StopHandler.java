package com.sigmundgranaas.forgero.minecraft.common.handler.use;

import com.sigmundgranaas.forgero.core.property.v2.feature.ClassKey;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public interface StopHandler {
	ClassKey<StopHandler> KEY = new ClassKey<>("minecraft:stop_use_handler", StopHandler.class);

	default void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
	}
}
