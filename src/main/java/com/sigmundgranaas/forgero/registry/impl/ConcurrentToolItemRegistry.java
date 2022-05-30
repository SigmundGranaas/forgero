package com.sigmundgranaas.forgero.registry.impl;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.item.ForgeroToolItem;
import com.sigmundgranaas.forgero.item.items.tool.*;
import com.sigmundgranaas.forgero.registry.ToolItemRegistry;
import net.minecraft.item.Item;

import java.util.Map;

public class ConcurrentToolItemRegistry extends ConcurrentItemResourceRegistry<ForgeroToolItem> implements ToolItemRegistry {
    ConcurrentToolItemRegistry(Map<String, ForgeroToolItem> resources) {
        super(resources);
    }

    @Override
    public ImmutableList<Item> getItems() {
        return getResourcesAsList().stream().map(ForgeroToolItem::getItem).collect(ImmutableList.toImmutableList());
    }

    @Override
    public ImmutableList<ForgeroPickaxeItem> getPickaxes() {
        return getSubTypeAsList(ForgeroPickaxeItem.class);
    }

    @Override
    public ImmutableList<ForgeroAxeItem> getAxes() {
        return getSubTypeAsList(ForgeroAxeItem.class);
    }

    @Override
    public ImmutableList<ForgeroHoeItem> getHoes() {
        return getSubTypeAsList(ForgeroHoeItem.class);
    }

    @Override
    public ImmutableList<ForgeroShovelItem> getShovels() {
        return getSubTypeAsList(ForgeroShovelItem.class);
    }

    @Override
    public ImmutableList<ForgeroSwordItem> getSwords() {
        return getSubTypeAsList(ForgeroSwordItem.class);
    }
}
