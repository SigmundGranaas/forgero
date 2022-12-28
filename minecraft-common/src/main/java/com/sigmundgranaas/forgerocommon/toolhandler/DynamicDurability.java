package com.sigmundgranaas.forgerocommon.toolhandler;

import net.minecraft.item.ItemStack;

public interface DynamicDurability {
    int getDurability(ItemStack stack);
}
