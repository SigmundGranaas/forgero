package com.sigmundgranaas.forgero.item.nbt.v2;

import com.sigmundgranaas.forgero.core.state.State;
import net.minecraft.nbt.NbtCompound;

import java.util.Optional;

public interface NbtStateParser {
    Optional<State> parse(NbtCompound compound);
}
