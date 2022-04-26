package com.sigmundgranaas.forgero.registry;

import net.minecraft.item.Item;

public interface ItemRegistry {
    ItemRegistry INSTANCE = ItemRegistryImpl.getInstance();

    void registerTool(Item tool);

    void registerToolPart(Item part);

    void registerTools();

    void registerToolParts();

    void registerSchematics();
}
