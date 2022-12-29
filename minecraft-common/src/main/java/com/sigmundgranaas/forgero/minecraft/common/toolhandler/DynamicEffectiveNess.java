package com.sigmundgranaas.forgero.minecraft.common.toolhandler;

import net.minecraft.block.BlockState;

public interface DynamicEffectiveNess {
    default boolean isEffectiveOn(BlockState state) {
        return false;
    }
}
