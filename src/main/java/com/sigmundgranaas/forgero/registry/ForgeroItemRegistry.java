package com.sigmundgranaas.forgero.registry;

import net.minecraft.item.Item;

public interface ForgeroItemRegistry {
    ForgeroItemRegistry INSTANCE = ForgeroItemRegistryImpl.getInstance();

    void registerTool(Item tool);

    void registerToolPart(Item part);

    void registerTools();

    void registerToolParts();
}
