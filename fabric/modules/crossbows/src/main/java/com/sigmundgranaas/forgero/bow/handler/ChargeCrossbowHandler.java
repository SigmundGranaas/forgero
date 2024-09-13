package com.sigmundgranaas.forgero.bow.handler;


import com.sigmundgranaas.forgero.core.property.v2.feature.ClassKey;
import com.sigmundgranaas.forgero.core.property.v2.feature.HandlerBuilder;
import com.sigmundgranaas.forgero.core.property.v2.feature.JsonBuilder;
import com.sigmundgranaas.forgero.minecraft.common.handler.use.UsageTickHandler;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

public class ChargeCrossbowHandler implements UsageTickHandler {
	public static final String TYPE = "forgero:charge_crossbow_handler";
	public static final ClassKey<ChargeCrossbowHandler> KEY = new ClassKey<>(TYPE, ChargeCrossbowHandler.class);
	public static final JsonBuilder<ChargeCrossbowHandler> BUILDER = HandlerBuilder.fromObject(ChargeCrossbowHandler.class, json -> new ChargeCrossbowHandler());


	private static final String CHARGED_KEY = "Charged";
	private static final String LOADED_KEY = "Loaded";

	@Override
	public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
		if (!world.isClient) {
			int i = EnchantmentHelper.getLevel(Enchantments.QUICK_CHARGE, stack);
			SoundEvent soundEvent = this.getQuickChargeSound(i);

			SoundEvent soundEvent2 = i == 0 ? SoundEvents.ITEM_CROSSBOW_LOADING_MIDDLE : null;
			float f = (float)(stack.getMaxUseTime() - remainingUseTicks) / (float) CrossbowItem.getPullTime(stack);
			if (f < 0.2f) {
				setCharged(stack, false);
			}
			if (f >= 0.2f && !isCharged(stack)) {
				setCharged(stack, true);
				world.playSound(null, user.getX(), user.getY(), user.getZ(), soundEvent, SoundCategory.PLAYERS, 0.5f, 1.0f);
			}
			if (f >= 0.5f && soundEvent2 != null && !isLoaded(stack)) {
				setLoaded(stack, true);
				world.playSound(null, user.getX(), user.getY(), user.getZ(), soundEvent2, SoundCategory.PLAYERS, 0.5f, 1.0f);
			}
		}
	}

	public static void setCharged(ItemStack stack, boolean charged) {
		NbtCompound nbtCompound = stack.getOrCreateNbt();
		nbtCompound.putBoolean(CHARGED_KEY, charged);
	}

	public static boolean isCharged(ItemStack stack) {
		NbtCompound nbtCompound = stack.getOrCreateNbt();
		return nbtCompound.contains(CHARGED_KEY) && nbtCompound.getBoolean(CHARGED_KEY);
	}

	public static void setLoaded(ItemStack stack, boolean loaded) {
		NbtCompound nbtCompound = stack.getOrCreateNbt();
		nbtCompound.putBoolean(LOADED_KEY, loaded);
	}

	public static boolean isLoaded(ItemStack stack) {
		NbtCompound nbtCompound = stack.getOrCreateNbt();
		return nbtCompound.contains(LOADED_KEY) && nbtCompound.getBoolean(LOADED_KEY);
	}

	private SoundEvent getQuickChargeSound(int stage) {
		return switch (stage) {
			case 1 -> SoundEvents.ITEM_CROSSBOW_QUICK_CHARGE_1;
			case 2 -> SoundEvents.ITEM_CROSSBOW_QUICK_CHARGE_2;
			case 3 -> SoundEvents.ITEM_CROSSBOW_QUICK_CHARGE_3;
			default -> SoundEvents.ITEM_CROSSBOW_LOADING_START;
		};
	}
}
