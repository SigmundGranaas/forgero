package com.sigmundgranaas.forgero.item.forgerotool.tool;

import com.sigmundgranaas.forgero.item.forgerotool.toolpart.ForgeroToolPart;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ForgeroToolInstance {
    ItemStack ForgeroTool;
    NbtCompound rootNbt;
    ForgeroToolPart head;
    ForgeroToolPart handle;
    ForgeroToolPart binding;

    public ForgeroToolInstance(@NotNull ItemStack forgeroTool, @NotNull ForgeroToolPart head, @NotNull ForgeroToolPart handle, @NotNull ForgeroToolPart binding) {
        if (!(forgeroTool.getItem() instanceof ForgeroTool)) {
            throw new IllegalArgumentException("The provided Itemstack is not a ForgeroTool");
        }
        this.ForgeroTool = forgeroTool;
        rootNbt = forgeroTool.getOrCreateNbt();
        this.head = head;
        this.handle = handle;
        this.binding = binding;
    }

    public NbtCompound getNbt() {
        return this.rootNbt;
    }
}
