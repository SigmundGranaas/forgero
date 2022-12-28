package com.sigmundgranaas.forgeroforge.toolhandler;

import net.minecraft.block.BlockState;

public interface DynamicEffectiveNess {
    default boolean isEffectiveOn(BlockState state) {
        return false;
    }
}
