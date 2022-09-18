package com.sigmundgranaas.forgero.item.nbt.v2;

import com.sigmundgranaas.forgero.state.Composite;
import com.sigmundgranaas.forgero.state.State;
import net.minecraft.nbt.NbtCompound;

import static com.sigmundgranaas.forgero.item.nbt.v2.NbtConstants.STATE_TYPE_IDENTIFIER;
import static com.sigmundgranaas.forgero.item.nbt.v2.NbtConstants.UPGRADES_IDENTIFIER;

public class UpgradeEncoder implements CompoundEncoder<State> {
    private final IdentifiableEncoder identifiableEncoder;

    public UpgradeEncoder() {
        this.identifiableEncoder = new IdentifiableEncoder();
    }

    @Override
    public NbtCompound encode(State element) {
        if (element instanceof Composite composite) {
            return new CompositeEncoder().encode(composite);
        }
        var compound = identifiableEncoder.encode(element);

        compound.putString(STATE_TYPE_IDENTIFIER, UPGRADES_IDENTIFIER);
        return compound;
    }
}
