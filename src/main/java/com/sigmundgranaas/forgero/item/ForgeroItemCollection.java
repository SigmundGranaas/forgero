package com.sigmundgranaas.forgero.item;

import com.sigmundgranaas.forgero.item.tool.ForgeroToolItem;
import com.sigmundgranaas.forgero.item.toolpart.ForgeroToolPartItem;
import net.minecraft.item.Item;

import java.util.List;

public interface ForgeroItemCollection {
    ForgeroItemCollection INSTANCE = ForgeroItemCollectionImpl.getInstance();

    List<Item> getTools();

    List<ForgeroToolItem> getToolItems();

    List<Item> getToolParts();

    List<ForgeroToolPartItem> getToolPartHeads();

    List<ForgeroToolPartItem> getToolPartHandles();

    List<ForgeroToolPartItem> getToolPartBindings();
}
