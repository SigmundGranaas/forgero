package com.sigmundgranaas.forgero.minecraft.common.toolhandler;

import net.minecraft.item.ItemStack;

public interface DynamicMiningLevel {
    int getMiningLevel(ItemStack stack);

    int getMiningLevel();
}
