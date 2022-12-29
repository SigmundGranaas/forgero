package com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2;

import com.sigmundgranaas.forgero.state.Identifiable;
import net.minecraft.nbt.NbtCompound;

import static com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.NbtConstants.*;

public class IdentifiableEncoder implements CompoundEncoder<Identifiable> {
    @Override
    public NbtCompound encode(Identifiable element) {
        var compound = new NbtCompound();
        compound.putString(NAME_IDENTIFIER, element.name());
        compound.putString(NAMESPACE_IDENTIFIER, element.nameSpace());
        compound.putString(ID_IDENTIFIER, element.identifier());
        return compound;
    }
}
