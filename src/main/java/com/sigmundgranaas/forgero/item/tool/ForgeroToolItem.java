package com.sigmundgranaas.forgero.item.tool;

import com.sigmundgranaas.forgero.core.tool.ForgeroTool;
import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.core.tool.toolpart.ToolPartHandle;
import com.sigmundgranaas.forgero.core.tool.toolpart.ToolPartHead;
import net.minecraft.util.Identifier;

public interface ForgeroToolItem {

    Identifier getIdentifier();

    ForgeroToolTypes getToolType();

    ForgeroTool getTool();

    ToolPartHead getHead();

    ToolPartHandle getHandle();
}
