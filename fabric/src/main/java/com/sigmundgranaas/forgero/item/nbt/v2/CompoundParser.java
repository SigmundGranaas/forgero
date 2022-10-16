package com.sigmundgranaas.forgero.item.nbt.v2;

import com.sigmundgranaas.forgero.ForgeroStateRegistry;
import net.minecraft.nbt.NbtCompound;

import java.util.Optional;

@FunctionalInterface
public interface CompoundParser<T> {
    CompositeParser COMPOSITE_PARSER = new CompositeParser(ForgeroStateRegistry.STATES::get, ForgeroStateRegistry.STATES::get);

    Optional<T> parse(NbtCompound compound);
}
