package com.sigmundgranaas.forgerocommon.item.nbt.v2;

import net.minecraft.nbt.NbtCompound;

@FunctionalInterface
public interface CompoundEncoder<T> {
    StateEncoder ENCODER = new StateEncoder();

    NbtCompound encode(T element);
}
