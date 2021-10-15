package com.sigmundgranaas.forgero.item.forgerotool.tool;

import com.sigmundgranaas.forgero.item.forgerotool.toolpart.ForgeroToolBinding;
import com.sigmundgranaas.forgero.item.forgerotool.toolpart.ForgeroToolHandle;
import com.sigmundgranaas.forgero.item.forgerotool.toolpart.ForgeroToolHead;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;

public class ForgeroToolInstance {
    ItemStack ForgeroTool;
    NbtCompound rootNbt;
    ForgeroToolHead head;
    ForgeroToolHandle handle;
    ForgeroToolBinding binding;

    public ForgeroToolInstance(ItemStack forgeroTool, ForgeroToolHead head, ForgeroToolHandle handle, @Nullable ForgeroToolBinding binding) {
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
