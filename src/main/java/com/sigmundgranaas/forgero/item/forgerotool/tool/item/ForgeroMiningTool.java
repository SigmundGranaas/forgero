package com.sigmundgranaas.forgero.item.forgerotool.tool.item;

import com.sigmundgranaas.forgero.item.forgerotool.ForgeroItemTypes;
import com.sigmundgranaas.forgero.item.forgerotool.tool.ForgeroToolTypes;

public interface ForgeroMiningTool extends ForgeroTool {
    @Override
    default ForgeroItemTypes getItemType() {
        return ForgeroItemTypes.TOOL;
    }

    ForgeroToolTypes getToolType();

    String getToolTypeLowerCaseString();
}
