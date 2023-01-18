package com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2;

import com.sigmundgranaas.forgero.core.soul.Soul;
import net.minecraft.nbt.NbtCompound;

import static com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.NbtConstants.*;

public class SoulEncoder implements CompoundEncoder<Soul> {

    public static final SoulEncoder ENCODER = new SoulEncoder();

    @Override
    public NbtCompound encode(Soul element) {
        NbtCompound compound = new NbtCompound();
        compound.putInt(LEVEL_IDENTIFIER, element.getLevel());
        compound.putInt(XP_IDENTIFIER, element.getXp());
        compound.putString(NAME_IDENTIFIER, element.name());
        return compound;
    }
}
