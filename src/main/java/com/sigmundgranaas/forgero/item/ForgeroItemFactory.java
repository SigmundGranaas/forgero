package com.sigmundgranaas.forgero.item;

import com.sigmundgranaas.forgero.core.tool.ForgeroTool;
import com.sigmundgranaas.forgero.core.tool.toolpart.ForgeroToolPart;
import net.minecraft.item.Item;

public interface ForgeroItemFactory {
    ForgeroItemFactory INSTANCE = ForgeroItemFactoryImpl.getInstance();

    Item createTool(ForgeroTool tool);

    Item createToolPart(ForgeroToolPart toolPart);
}
