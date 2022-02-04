package com.sigmundgranaas.forgero.item.implementation;

import com.sigmundgranaas.forgero.core.tool.ForgeroTool;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPart;
import com.sigmundgranaas.forgero.item.ItemFactory;
import com.sigmundgranaas.forgero.item.ItemGroups;
import com.sigmundgranaas.forgero.item.adapter.ToolMaterialAdapter;
import com.sigmundgranaas.forgero.item.tool.ForgeroPickaxeItem;
import com.sigmundgranaas.forgero.item.tool.ShovelItem;
import net.minecraft.item.Item;

public class ItemFactoryImpl implements ItemFactory {
    public static ItemFactory INSTANCE;

    public static ItemFactory getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ItemFactoryImpl();
        }
        return INSTANCE;
    }

    @Override
    public Item createTool(ForgeroTool tool) {
        return switch (tool.getToolType()) {
            case PICKAXE -> new ForgeroPickaxeItem(new ToolMaterialAdapter(tool.getMaterial()), 1, tool.getAttackSpeed(), new Item.Settings().group(ItemGroups.FORGERO_TOOLS), tool);
            case SHOVEL -> new ShovelItem(new ToolMaterialAdapter(tool.getMaterial()), 1.5f, tool.getAttackSpeed(), new Item.Settings().group(ItemGroups.FORGERO_TOOLS), tool);
            case SWORD -> null;
        };
    }

    @Override
    public Item createToolPart(ForgeroToolPart toolPart) {
        return new ToolPartItemImpl(new Item.Settings().group(ItemGroups.FORGERO_TOOL_PARTS), toolPart.getPrimaryMaterial(), toolPart.getToolPartType(), toolPart);
    }
}
