package com.sigmundgranaas.forgero.item;

import com.sigmundgranaas.forgero.core.tool.ForgeroTool;
import com.sigmundgranaas.forgero.core.tool.toolpart.ForgeroToolPart;
import com.sigmundgranaas.forgero.item.tool.ForgeroPickaxeItem;
import com.sigmundgranaas.forgero.item.tool.ForgeroShovelItem;
import com.sigmundgranaas.forgero.item.toolpart.ForgeroToolPartItemImpl;
import net.minecraft.item.Item;

;

public class ForgeroItemFactoryImpl implements ForgeroItemFactory {
    public static ForgeroItemFactory INSTANCE;

    public static ForgeroItemFactory getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ForgeroItemFactoryImpl();
        }
        return INSTANCE;
    }

    @Override
    public Item createTool(ForgeroTool tool) {
        return switch (tool.getToolType()) {
            case PICKAXE -> new ForgeroPickaxeItem(tool.getMaterial(), tool.getAttackDamage(), tool.getAttackSpeed(), new Item.Settings().group(ForgeroItemGroups.FORGERO_TOOLS), tool);
            case SHOVEL -> new ForgeroShovelItem(tool.getMaterial(), tool.getAttackDamage(), tool.getAttackSpeed(), new Item.Settings().group(ForgeroItemGroups.FORGERO_TOOLS), tool);
            case SWORD -> null;
        };
    }

    @Override
    public Item createToolPart(ForgeroToolPart toolPart) {
        return new ForgeroToolPartItemImpl(new Item.Settings().group(ForgeroItemGroups.FORGERO_TOOL_PARTS), toolPart.getPrimaryMaterial(), toolPart.getToolPartType(), toolPart);
    }
}
