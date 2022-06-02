package com.sigmundgranaas.forgero.registry;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.core.data.v1.pojo.ToolPartPojo;
import com.sigmundgranaas.forgero.item.ToolPartItem;
import net.minecraft.item.Item;

public interface ToolPartItemRegistry extends ForgeroItemResourceRegistry<ToolPartPojo, ToolPartItem> {
    ImmutableList<Item> getItems();

    ImmutableList<ToolPartItem> getPickaxeHeads();

    ImmutableList<ToolPartItem> getAxeHeads();

    ImmutableList<ToolPartItem> getHoeHeads();

    ImmutableList<ToolPartItem> getShovelHeads();

    ImmutableList<ToolPartItem> getSwordHeads();

    ImmutableList<ToolPartItem> getHeads();

    ImmutableList<ToolPartItem> getHandles();

    ImmutableList<ToolPartItem> getBindings();
}
