package com.sigmundgranaas.forgero.item;

import com.sigmundgranaas.forgero.core.tool.ForgeroTool;
import com.sigmundgranaas.forgero.core.tool.toolpart.ForgeroToolPart;
import com.sigmundgranaas.forgero.item.implementation.ItemFactoryImpl;
import net.minecraft.item.Item;

public interface ItemFactory {
    ItemFactory INSTANCE = ItemFactoryImpl.getInstance();

    Item createTool(ForgeroTool tool);

    Item createToolPart(ForgeroToolPart toolPart);
}
