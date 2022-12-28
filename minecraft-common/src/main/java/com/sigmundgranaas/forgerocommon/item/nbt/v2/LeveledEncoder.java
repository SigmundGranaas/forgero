package com.sigmundgranaas.forgerocommon.item.nbt.v2;

import com.sigmundgranaas.forgero.state.Composite;
import com.sigmundgranaas.forgero.state.LeveledState;
import com.sigmundgranaas.forgero.state.State;
import net.minecraft.nbt.NbtCompound;

import static com.sigmundgranaas.forgerocommon.item.nbt.v2.NbtConstants.*;

public class LeveledEncoder implements CompoundEncoder<State> {
    private final IdentifiableEncoder identifiableEncoder;

    public LeveledEncoder() {
        this.identifiableEncoder = new IdentifiableEncoder();
    }

    @Override
    public NbtCompound encode(State element) {
        if (element instanceof Composite composite) {
            return new CompositeEncoder().encode(composite);
        }
        var compound = identifiableEncoder.encode(element);

        if (element instanceof LeveledState state) {
            compound.putInt(LEVEL_IDENTIFIER, state.level());
        }
        compound.putString(STATE_TYPE_IDENTIFIER, LEVELED_IDENTIFIER);
        return compound;
    }
}
