package com.sigmundgranaas.forgero.item.forgerotool.tool;

import com.sigmundgranaas.forgero.item.forgerotool.toolpart.ForgeroToolHead;
import com.sigmundgranaas.forgero.item.forgerotool.toolpart.ForgeroToolPartCreator;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public class ForgeroToolCreator {
    public static ForgeroToolInstance createForgeroToolInstance(NbtCompound nbt) {

        return null;
    }

    public static ForgeroToolInstance createForgeroToolInstance(ItemStack forgeroTool) {
        return null;
    }

    public static ForgeroToolInstance createForgeroToolInstance(ItemStack head, ItemStack handle) {
        return null;
    }

    public static ForgeroToolInstance createForgeroToolInstance(ItemStack head, ItemStack handle, ItemStack binding) {
        return null;
    }

    private ForgeroToolInstance createBasicForgeroToolInstance(ItemStack forgeroTool) {
        ForgeroToolHead head = ForgeroToolPartCreator.
        return new ForgeroToolInstance(forgeroTool);
    }

}
