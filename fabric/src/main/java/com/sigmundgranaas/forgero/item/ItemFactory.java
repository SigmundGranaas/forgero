package com.sigmundgranaas.forgero.item;

import com.sigmundgranaas.forgero.gem.Gem;
import com.sigmundgranaas.forgero.schematic.Schematic;
import com.sigmundgranaas.forgero.tool.ForgeroTool;
import com.sigmundgranaas.forgero.toolpart.ForgeroToolPart;
import com.sigmundgranaas.forgero.item.implementation.ItemFactoryImpl;
import com.sigmundgranaas.forgero.item.items.GemItem;
import com.sigmundgranaas.forgero.item.items.SchematicItem;
import net.minecraft.item.Item;

public interface ItemFactory {
    ItemFactory INSTANCE = ItemFactoryImpl.getInstance();

    ForgeroToolItem createTool(ForgeroTool tool);

    Item createToolPart(ForgeroToolPart toolPart);

    SchematicItem createSchematic(Schematic pattern);

    GemItem createGem(Gem gem);
}
