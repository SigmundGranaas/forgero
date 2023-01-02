package com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2;

import com.sigmundgranaas.forgero.core.registry.StateFinder;
import com.sigmundgranaas.forgero.core.state.LeveledState;
import com.sigmundgranaas.forgero.core.state.State;
import net.minecraft.nbt.NbtCompound;

import java.util.Optional;

public class LeveledParser implements CompoundParser<State> {
    private final StateFinder supplier;

    public LeveledParser(StateFinder supplier) {
        this.supplier = supplier;

    }

    @Override
    public Optional<State> parse(NbtCompound compound) {
        if (!compound.getString(NbtConstants.STATE_TYPE_IDENTIFIER).equals(NbtConstants.LEVELED_IDENTIFIER)) {
            return Optional.empty();
        }
        if (compound.contains(NbtConstants.LEVEL_IDENTIFIER)) {
            int level = compound.getInt(NbtConstants.LEVEL_IDENTIFIER);
            var state = supplier.find(compound.getString(NbtConstants.ID_IDENTIFIER));
            if (state.isPresent() && state.get() instanceof LeveledState leveledState) {
                return Optional.of(leveledState.setLevel(level));
            }
        } else {
            return supplier.find(compound.getString(NbtConstants.ID_IDENTIFIER));
        }
        return Optional.empty();
    }

}
