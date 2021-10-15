package com.sigmundgranaas.forgero.item.forgerotool;

import net.minecraft.util.Identifier;

public interface ForgeroItem {
    ForgeroItemTypes getItemType();

    String getToolTip();

    Identifier getIdentifier();
}
