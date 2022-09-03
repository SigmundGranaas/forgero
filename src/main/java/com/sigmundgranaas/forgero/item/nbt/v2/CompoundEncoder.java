package com.sigmundgranaas.forgero.item.nbt.v2;

import net.minecraft.nbt.NbtCompound;

@FunctionalInterface
public interface CompoundEncoder<T> {
    NbtCompound encode(T element);
}
