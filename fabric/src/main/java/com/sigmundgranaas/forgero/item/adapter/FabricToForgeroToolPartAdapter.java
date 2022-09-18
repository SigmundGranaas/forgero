package com.sigmundgranaas.forgero.item.adapter;

import com.sigmundgranaas.forgero.toolpart.ForgeroToolPart;
import com.sigmundgranaas.forgero.item.ToolPartItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

import java.util.Optional;

@SuppressWarnings("unused")
public interface FabricToForgeroToolPartAdapter {
    static FabricToForgeroToolPartAdapter createAdapter() {
        return new FabricToForgeroAdapter();
    }


    boolean isToolPart(Identifier id);

    boolean isToolPart(Item item);

    boolean isToolPart(ItemStack itemStack);

    boolean isToolPart(NbtCompound compound);

    Optional<ForgeroToolPart> getToolPart(Identifier id);

    Optional<ForgeroToolPart> getToolPart(Item item);

    Optional<ForgeroToolPart> getToolPart(ItemStack itemStack);

    Optional<ForgeroToolPart> getToolPart(NbtCompound compound);

    ForgeroToolPart getToolPart(ToolPartItem toolItem);
}
