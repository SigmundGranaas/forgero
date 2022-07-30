package com.sigmundgranaas.forgero.item.adapter;

import com.sigmundgranaas.forgero.core.gem.Gem;
import com.sigmundgranaas.forgero.item.NBTFactory;
import com.sigmundgranaas.forgero.item.items.GemItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

import java.util.Optional;

public class FabricToForgeroGemAdapterImpl implements FabricToForgeroGemAdapter {
    @Override
    public boolean isGem(ItemStack gemStack) {
        return gemStack.getItem() instanceof GemItem;
    }

    @Override
    public boolean isGem(Item gemItem) {
        return gemItem instanceof GemItem;
    }

    @Override
    public boolean isGem(NbtCompound compound) {
        return compound.contains(NBTFactory.GEM_NBT_IDENTIFIER);
    }

    @Override
    public Optional<Gem> getGem(ItemStack itemStack) {
        if (isGem(itemStack)) {
            if (itemStack.getOrCreateNbt().contains(NBTFactory.GEM_NBT_IDENTIFIER)) {
                return NBTFactory.INSTANCE.createGemFromNbt(itemStack.getOrCreateNbt());
            } else {
                return Optional.of(((GemItem) itemStack.getItem()).getGem());
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Gem> getGem(NbtCompound compound) {
        if (isGem(compound)) {
            return NBTFactory.INSTANCE.createGemFromNbt(compound);
        }
        return Optional.empty();
    }
}
