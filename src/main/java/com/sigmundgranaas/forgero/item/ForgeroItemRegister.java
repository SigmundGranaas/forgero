package com.sigmundgranaas.forgero.item;

import com.sigmundgranaas.forgero.item.forgerotool.tool.item.ForgeroTool;
import com.sigmundgranaas.forgero.item.forgerotool.toolpart.ForgeroToolPartItem;
import net.minecraft.item.Item;
import net.minecraft.util.registry.Registry;

import java.util.List;

public class ForgeroItemRegister {
    public static void registerToolPart(ForgeroToolPartItem part) {
        Registry.register(Registry.ITEM, part.getIdentifier(), part);
    }

    private static void registerTool(Item tool) {
        Registry.register(Registry.ITEM, ((ForgeroTool) tool).getIdentifier(), tool);
    }

    private static void registerForgeroItem(Item item) {
        if (item instanceof ForgeroToolPartItem) {
            registerToolPart((ForgeroToolPartItem) item);
        } else if (item instanceof ForgeroTool) {
            registerTool(item);
        }
    }

    public static void RegisterForgeroItem(List<? extends Item> items) {
        for (Item item : items) {
            registerForgeroItem(item);
        }
    }
}
