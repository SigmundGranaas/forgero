package com.sigmundgranaas.forgero.registry;

import com.sigmundgranaas.forgero.item.ForgeroItemCollection;
import com.sigmundgranaas.forgero.item.tool.ForgeroToolItem;
import com.sigmundgranaas.forgero.item.toolpart.ForgeroToolPartItemImpl;
import net.minecraft.item.Item;
import net.minecraft.util.registry.Registry;

public class ForgeroItemRegistryImpl implements ForgeroItemRegistry {
    private static ForgeroItemRegistry INSTANCE;
    private final ForgeroItemCollection collection;

    public ForgeroItemRegistryImpl(ForgeroItemCollection collection) {
        this.collection = collection;
    }

    public static ForgeroItemRegistry getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ForgeroItemRegistryImpl(ForgeroItemCollection.INSTANCE);
        }
        return INSTANCE;
    }

    @Override
    public void registerToolPart(ForgeroToolPartItemImpl part) {
        Registry.register(Registry.ITEM, part.getIdentifier(), part);
    }

    @Override
    public void registerTool(Item tool) {
        Registry.register(Registry.ITEM, ((ForgeroToolItem) tool).getIdentifier(), tool);
    }


    @Override
    public void registerTools() {
        collection.INSTANCE.getTools().forEach(this::registerTool);
    }

    @Override
    public void registerToolParts() {
        collection.INSTANCE.getToolParts().forEach(this::registerTool);
    }
}
