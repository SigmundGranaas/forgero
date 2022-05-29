package com.sigmundgranaas.forgero.registry.impl;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.core.registry.impl.ConcurrentResourceRegistry;
import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.core.toolpart.binding.ToolPartBinding;
import com.sigmundgranaas.forgero.core.toolpart.handle.ToolPartHandle;
import com.sigmundgranaas.forgero.core.toolpart.head.ToolPartHead;
import com.sigmundgranaas.forgero.item.ToolPartItem;
import com.sigmundgranaas.forgero.registry.ToolPartItemRegistry;
import net.minecraft.item.Item;

import java.util.Map;

import static com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes.*;

public class ConcurrentToolPartItemRegistry extends ConcurrentResourceRegistry<ToolPartItem> implements ToolPartItemRegistry {
    ConcurrentToolPartItemRegistry(Map<String, ToolPartItem> resources) {
        super(resources);
    }

    @Override
    public ImmutableList<Item> getItems() {
        return getResourcesAsList().stream().map(ToolPartItem::getItem).collect(ImmutableList.toImmutableList());
    }

    @Override
    public ImmutableList<ToolPartItem> getPickaxeHeads() {
        return filterHeadByToolType(PICKAXE);
    }

    @Override
    public ImmutableList<ToolPartItem> getAxeHeads() {
        return filterHeadByToolType(AXE);
    }

    @Override
    public ImmutableList<ToolPartItem> getHoeHeads() {
        return filterHeadByToolType(HOE);
    }

    @Override
    public ImmutableList<ToolPartItem> getShovelHeads() {
        return filterHeadByToolType(SHOVEL);
    }

    @Override
    public ImmutableList<ToolPartItem> getSwordHeads() {
        return filterHeadByToolType(SWORD);
    }

    private ImmutableList<ToolPartItem> filterHeadByToolType(ForgeroToolTypes type) {
        return getHeads()
                .stream()
                .filter(headItems -> ((ToolPartHead) headItems.getPart())
                        .getToolType() == type)
                .collect(ImmutableList.toImmutableList());
    }

    @Override
    public ImmutableList<ToolPartItem> getHeads() {
        return getResourcesAsList().stream()
                .filter(ToolPartHead.class::isInstance)
                .collect(ImmutableList.toImmutableList());
    }

    @Override
    public ImmutableList<ToolPartItem> getHandles() {
        return getResourcesAsList().stream()
                .filter(ToolPartHandle.class::isInstance)
                .collect(ImmutableList.toImmutableList());
    }

    @Override
    public ImmutableList<ToolPartItem> getBindings() {
        return getResourcesAsList().stream()
                .filter(ToolPartBinding.class::isInstance)
                .collect(ImmutableList.toImmutableList());
    }

}
