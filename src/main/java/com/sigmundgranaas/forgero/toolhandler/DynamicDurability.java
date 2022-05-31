package com.sigmundgranaas.forgero.toolhandler;

import net.minecraft.item.ItemStack;

public interface DynamicDurability {
    int getDurability(ItemStack stack);
}
