package com.sigmundgranaas.forgero.item.adapter;

import com.sigmundgranaas.forgero.core.tool.ForgeroTool;
import com.sigmundgranaas.forgero.item.ForgeroToolItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

import java.util.Optional;

public interface FabricToForgeroToolAdapter {
    static FabricToForgeroToolAdapter createAdapter() {
        return new FabricToForgeroAdapter();
    }

    boolean isTool(Identifier id);

    boolean isTool(Item item);

    boolean isTool(ItemStack itemStack);

    boolean isTool(NbtCompound compound);

    Optional<ForgeroTool> getTool(Identifier id);

    Optional<ForgeroTool> getTool(Item item);

    Optional<ForgeroTool> getTool(ItemStack itemStack);

    Optional<ForgeroTool> getTool(NbtCompound compound);

    ForgeroTool getTool(ForgeroToolItem toolItem);
}
