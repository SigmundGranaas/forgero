package com.sigmundgranaas.forgero.item.nbt.v2;

import com.sigmundgranaas.forgero.registry.StateSupplier;
import com.sigmundgranaas.forgero.state.LeveledState;
import com.sigmundgranaas.forgero.state.State;
import net.minecraft.nbt.NbtCompound;

import java.util.Optional;

import static com.sigmundgranaas.forgero.item.nbt.v2.NbtConstants.*;

public class LeveledParser implements CompoundParser<State> {
    private final StateSupplier supplier;

    public LeveledParser(StateSupplier supplier) {
        this.supplier = supplier;

    }

    @Override
    public Optional<State> parse(NbtCompound compound) {
        if (!compound.getString(STATE_TYPE_IDENTIFIER).equals(LEVELED_IDENTIFIER)) {
            return Optional.empty();
        }
        if (compound.contains(LEVEL_IDENTIFIER)) {
            int level = compound.getInt(LEVEL_IDENTIFIER);
            var state = supplier.get(compound.getString(ID_IDENTIFIER));
            if (state.isPresent() && state.get() instanceof LeveledState leveledState) {
                return Optional.of(leveledState.setLevel(level));
            }
        } else {
            return supplier.get(compound.getString(ID_IDENTIFIER));
        }
        return Optional.empty();
    }

}
