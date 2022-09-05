package com.sigmundgranaas.forgero.item.nbt.v2;

import net.minecraft.nbt.NbtCompound;

import java.util.Optional;

@FunctionalInterface
public interface CompoundParser<T> {
    Optional<T> parse(NbtCompound compound);
}
