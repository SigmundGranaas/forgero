package com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2;

import com.sigmundgranaas.forgero.core.state.Composite;
import com.sigmundgranaas.forgero.core.state.LeveledState;
import com.sigmundgranaas.forgero.core.state.State;
import net.minecraft.nbt.NbtCompound;

import static com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.NbtConstants.STATE_IDENTIFIER;
import static com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.NbtConstants.STATE_TYPE_IDENTIFIER;

public class StateEncoder implements CompoundEncoder<State> {
    private final IdentifiableEncoder identifiableEncoder;

    public StateEncoder() {
        this.identifiableEncoder = new IdentifiableEncoder();
    }

    @Override
    public NbtCompound encode(State element) {
        if (element instanceof Composite) {
            return new CompositeEncoder().encode(element);
        } else if (element instanceof LeveledState) {
            return new LeveledEncoder().encode(element);
        }
        var compound = identifiableEncoder.encode(element);

        compound.putString(STATE_TYPE_IDENTIFIER, STATE_IDENTIFIER);
        return compound;
    }
}
