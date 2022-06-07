package com.sigmundgranaas.forgero.registry;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.core.data.v1.pojo.ToolPojo;
import com.sigmundgranaas.forgero.item.ForgeroToolItem;
import com.sigmundgranaas.forgero.item.items.tool.*;
import net.minecraft.item.Item;

public interface ToolItemRegistry extends ForgeroItemResourceRegistry<ToolPojo, ForgeroToolItem> {
    ImmutableList<Item> getItems();

    ImmutableList<ForgeroPickaxeItem> getPickaxes();

    ImmutableList<ForgeroAxeItem> getAxes();

    ImmutableList<ForgeroHoeItem> getHoes();

    ImmutableList<ForgeroShovelItem> getShovels();

    ImmutableList<ForgeroSwordItem> getSwords();
}
