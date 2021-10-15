package com.sigmundgranaas.forgero.item.forgerotool.tool;

import com.sigmundgranaas.forgero.item.forgerotool.ForgeroItem;
import com.sigmundgranaas.forgero.item.forgerotool.toolpart.ForgeroToolPartItem;

public interface ForgeroTool extends ForgeroItem {
    ForgeroToolPartItem getToolHead();

    ForgeroToolPartItem getToolHandle();

}
