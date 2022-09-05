package com.sigmundgranaas.forgero.item.nbt.v2;

import com.sigmundgranaas.forgerocore.state.Identifiable;
import net.minecraft.nbt.NbtCompound;

import static com.sigmundgranaas.forgero.item.nbt.v2.NbtConstants.ID_IDENTIFIER;
import static com.sigmundgranaas.forgero.item.nbt.v2.NbtConstants.NAME_IDENTIFIER;

public class IdentifiableEncoder implements CompoundEncoder<Identifiable> {
    @Override
    public NbtCompound encode(Identifiable element) {
        var compound = new NbtCompound();
        compound.putString(NAME_IDENTIFIER, element.name());
        compound.putString(ID_IDENTIFIER, element.identifier());
        return compound;
    }
}
