package com.sigmundgranaas.forgero.item.forgerotool.tool;

import com.sigmundgranaas.forgero.item.forgerotool.ForgeroItemTypes;

public interface ForgeroMiningTool extends ForgeroTool {
    @Override
    default ForgeroItemTypes getItemType() {
        return ForgeroItemTypes.TOOL;
    }

    ForgeroToolTypes getToolType();

    String getToolTypeLowerCaseString();
}
