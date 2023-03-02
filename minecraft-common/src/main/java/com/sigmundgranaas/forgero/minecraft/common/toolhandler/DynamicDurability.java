package com.sigmundgranaas.forgero.minecraft.common.toolhandler;

import net.minecraft.item.ItemStack;

public interface DynamicDurability {
	int getDurability(ItemStack stack);
}
