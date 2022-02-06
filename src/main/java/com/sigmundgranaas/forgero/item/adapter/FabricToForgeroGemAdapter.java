package com.sigmundgranaas.forgero.item.adapter;

import com.sigmundgranaas.forgero.core.gem.Gem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

import java.util.Optional;

public interface FabricToForgeroGemAdapter {
    static FabricToForgeroGemAdapter createAdapter() {
        return new FabricToForgeroGemAdapterImpl();
    }

    boolean isGem(ItemStack gemStack);

    boolean isGem(Item gemItem);

    boolean isGem(NbtCompound compound);

    Optional<Gem> getGem(ItemStack itemStack);

    Optional<Gem> getGem(NbtCompound compound);
}
