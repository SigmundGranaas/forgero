package com.sigmundgranaas.forgero.registry;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.core.ForgeroResourceRegistry;
import com.sigmundgranaas.forgero.item.ForgeroToolItem;
import com.sigmundgranaas.forgero.item.items.tool.*;
import net.minecraft.item.Item;

public interface ToolItemRegistry extends ForgeroResourceRegistry<ForgeroToolItem> {
    ImmutableList<Item> getItems();

    ImmutableList<ForgeroPickaxeItem> getPickaxes();

    ImmutableList<ForgeroAxeItem> getAxes();

    ImmutableList<ForgeroHoeItem> getHoes();

    ImmutableList<ForgeroShovelItem> getShovels();

    ImmutableList<ForgeroSwordItem> getSwords();
}
