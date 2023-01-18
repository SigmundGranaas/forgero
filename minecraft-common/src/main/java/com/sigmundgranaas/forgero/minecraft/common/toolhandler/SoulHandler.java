package com.sigmundgranaas.forgero.minecraft.common.toolhandler;

import com.sigmundgranaas.forgero.core.soul.Soul;
import com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.SoulEncoder;
import com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.SoulParser;
import net.minecraft.item.ItemStack;

import java.util.Optional;

import static com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.NbtConstants.FORGERO_IDENTIFIER;
import static com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.NbtConstants.SOUL_IDENTIFIER;

public class SoulHandler {
    private final ItemStack stack;

    private final Soul soul;

    public SoulHandler(ItemStack stack, Soul soul) {
        this.stack = stack;
        this.soul = soul;
    }

    public static Optional<SoulHandler> of(ItemStack stack) {
        return SoulParser.of(stack).map(value -> new SoulHandler(stack, value));
    }

    public void processBlockBreak() {
        soul.addXp(5);
        stack.getOrCreateNbt().getCompound(FORGERO_IDENTIFIER).put(SOUL_IDENTIFIER, SoulEncoder.ENCODER.encode(soul));
    }

    public void processMobKill() {
        soul.addXp(5);
        stack.getOrCreateNbt().getCompound(FORGERO_IDENTIFIER).put(SOUL_IDENTIFIER, SoulEncoder.ENCODER.encode(soul));
    }
}
