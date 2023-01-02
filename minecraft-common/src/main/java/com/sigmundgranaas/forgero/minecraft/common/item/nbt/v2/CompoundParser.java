package com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2;

import com.sigmundgranaas.forgero.core.ForgeroStateRegistry;
import com.sigmundgranaas.forgero.core.state.State;
import net.minecraft.nbt.NbtCompound;

import java.util.Optional;

@FunctionalInterface
public interface CompoundParser<T> {
    CompoundParser<State> STATE_PARSER = new StateParser(ForgeroStateRegistry.stateFinder());

    Optional<T> parse(NbtCompound compound);
}
