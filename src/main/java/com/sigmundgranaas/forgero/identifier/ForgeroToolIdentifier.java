package com.sigmundgranaas.forgero.identifier;

import com.sigmundgranaas.forgero.item.forgerotool.tool.ForgeroToolTypes;

public interface ForgeroToolIdentifier {
    ForgeroMaterialIdentifier getMaterial();

    ForgeroToolPartIdentifier getHead();

    ForgeroToolPartIdentifier getHandle();
    
    ForgeroToolTypes getToolType();
}
