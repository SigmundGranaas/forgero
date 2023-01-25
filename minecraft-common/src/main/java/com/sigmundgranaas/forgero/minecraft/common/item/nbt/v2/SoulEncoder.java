package com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2;

import com.sigmundgranaas.forgero.core.soul.IdentifiableIntTracker;
import com.sigmundgranaas.forgero.core.soul.Soul;
import net.minecraft.nbt.NbtCompound;

import static com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.NbtConstants.*;

public class SoulEncoder implements CompoundEncoder<Soul> {

    public static final SoulEncoder ENCODER = new SoulEncoder();

    @Override
    public NbtCompound encode(Soul element) {
        NbtCompound compound = new NbtCompound();
        compound.putInt(LEVEL_IDENTIFIER, element.getLevel());
        compound.putString(ID_IDENTIFIER, element.identifier());
        compound.putInt(XP_IDENTIFIER, element.getXp());
        compound.putString(NAME_IDENTIFIER, element.name());
        compound.put(TRACKER_IDENTIFIER, new NbtCompound());
        compound.getCompound(TRACKER_IDENTIFIER).put(MOB_TRACKER_IDENTIFIER, encodeTracker(element.tracker().mobs()));
        compound.getCompound(TRACKER_IDENTIFIER).put(BLOCK_TRACKER_IDENTIFIER, encodeTracker(element.tracker().blocks()));
        return compound;
    }

    private NbtCompound encodeTracker(IdentifiableIntTracker tracker) {
        NbtCompound compound = new NbtCompound();
        tracker.toMap().forEach(compound::putInt);
        return compound;
    }
}
