package com.sigmundgranaas.forgero.minecraft.common.handler.use;

import com.sigmundgranaas.forgero.core.property.v2.feature.ClassKey;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

public interface BaseHandler {
	BaseHandler DEFAULT = new BaseHandler() {
	};
	Item DEFAULT_ITEM_USE_ACTIONS = Items.AIR;

	ClassKey<BaseHandler> KEY = new ClassKey<>("minecraft:base_use_handler", BaseHandler.class);


	default UseAction getUseAction(ItemStack stack) {
		return DEFAULT_ITEM_USE_ACTIONS.getUseAction(stack);
	}

	default int getMaxUseTime(ItemStack stack) {
		return DEFAULT_ITEM_USE_ACTIONS.getMaxUseTime(stack);
	}

	default void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
	}

	default ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
		return DEFAULT_ITEM_USE_ACTIONS.finishUsing(stack, world, user);
	}
	
	default boolean isUsedOnRelease(ItemStack stack) {
		return false;
	}
}
