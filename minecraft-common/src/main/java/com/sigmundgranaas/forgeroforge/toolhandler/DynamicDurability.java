package com.sigmundgranaas.forgeroforge.toolhandler;

import net.minecraft.item.ItemStack;

public interface DynamicDurability {
    int getDurability(ItemStack stack);
}
