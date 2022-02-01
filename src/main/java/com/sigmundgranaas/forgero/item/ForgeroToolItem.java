package com.sigmundgranaas.forgero.item;

import com.sigmundgranaas.forgero.core.tool.ForgeroTool;
import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.core.tool.toolpart.handle.ToolPartHandle;
import com.sigmundgranaas.forgero.core.tool.toolpart.head.ToolPartHead;
import com.sigmundgranaas.forgero.item.adapter.FabricToForgeroToolAdapter;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public interface ForgeroToolItem {

    Identifier getIdentifier();

    ForgeroToolTypes getToolType();

    ForgeroTool getTool();

    ToolPartHead getHead();

    ToolPartHandle getHandle();

    default int getDurability(ItemStack stack) {
        ForgeroTool forgeroTool = FabricToForgeroToolAdapter.createAdapter().getTool(stack).orElse(getTool());
        return forgeroTool.getDurability();
    }

    default int getCustomItemBarStep(ItemStack stack) {
        ForgeroTool forgeroTool = FabricToForgeroToolAdapter.createAdapter().getTool(stack).orElse(getTool());
        return Math.round(13.0f - (float) stack.getDamage() * 13.0f / (float) forgeroTool.getDurability());
    }
}
