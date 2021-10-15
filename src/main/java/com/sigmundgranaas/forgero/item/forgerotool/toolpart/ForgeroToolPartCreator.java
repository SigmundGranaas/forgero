package com.sigmundgranaas.forgero.item.forgerotool.toolpart;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public class ForgeroToolPartCreator {
    public static AbstractToolPart createToolPart(NbtCompound nbtCompound) {
        return null;
    }

    public static AbstractToolPart createToolPart(ItemStack forgeroToolPart) {
        return null;
    }

    public static AbstractToolPart createToolPart(ForgeroToolPartItem forgeroToolPart) {
        switch (forgeroToolPart.getToolPartType()) {
            case HANDLE -> new ForgeroToolHandle(forgeroToolPart.getMaterial());
        }
        return;
    }

    public static AbstractToolPart createToolPart(Item forgeroToolPart) {
        return null;
    }

    private AbstractToolPart createBasicToolPart() {
        return null;
    }
}
