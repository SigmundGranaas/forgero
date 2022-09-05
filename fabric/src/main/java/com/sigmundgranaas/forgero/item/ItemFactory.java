package com.sigmundgranaas.forgero.item;

import com.sigmundgranaas.forgerocore.gem.Gem;
import com.sigmundgranaas.forgerocore.schematic.Schematic;
import com.sigmundgranaas.forgerocore.tool.ForgeroTool;
import com.sigmundgranaas.forgerocore.toolpart.ForgeroToolPart;
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
