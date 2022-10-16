package com.sigmundgranaas.forgero.item.nbt.v2;

import net.minecraft.nbt.NbtCompound;

@FunctionalInterface
public interface CompoundEncoder<T> {
    CompositeEncoder COMPOSITE_ENCODER = new CompositeEncoder();
    
    NbtCompound encode(T element);
}
