package com.sigmundgranaas.forgero.item;

import com.sigmundgranaas.forgero.item.implementation.ItemCollectionImpl;
import net.minecraft.item.Item;

import java.util.List;

public interface ItemCollection {
    ItemCollection INSTANCE = ItemCollectionImpl.getInstance();

    List<Item> getTools();

    List<ForgeroToolItem> getToolItems();

    List<Item> getToolParts();

    List<ToolPartItem> getToolPartHeads();

    List<ToolPartItem> getToolPartHandles();

    List<ToolPartItem> getToolPartBindings();
}
