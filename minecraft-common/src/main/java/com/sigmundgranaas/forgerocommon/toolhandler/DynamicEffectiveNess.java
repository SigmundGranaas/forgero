package com.sigmundgranaas.forgerocommon.toolhandler;

import net.minecraft.block.BlockState;

public interface DynamicEffectiveNess {
    default boolean isEffectiveOn(BlockState state) {
        return false;
    }
}
