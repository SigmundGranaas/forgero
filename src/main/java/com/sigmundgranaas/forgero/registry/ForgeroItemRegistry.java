package com.sigmundgranaas.forgero.registry;

import com.sigmundgranaas.forgero.item.toolpart.ForgeroToolPartItemImpl;
import net.minecraft.item.Item;

public interface ForgeroItemRegistry {
    ForgeroItemRegistry INSTANCE = ForgeroItemRegistryImpl.getInstance();

    void registerTool(Item tool);

    void registerToolPart(ForgeroToolPartItemImpl part);

    void registerTools();

    void registerToolParts();
}
