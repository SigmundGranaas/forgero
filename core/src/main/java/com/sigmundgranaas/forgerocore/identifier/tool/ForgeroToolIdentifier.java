package com.sigmundgranaas.forgerocore.identifier.tool;

import com.sigmundgranaas.forgerocore.tool.ForgeroToolTypes;

public interface ForgeroToolIdentifier {
    ForgeroMaterialIdentifier getMaterial();

    ForgeroToolPartIdentifier getHead();

    ForgeroToolPartIdentifier getHandle();

    ForgeroToolTypes getToolType();
}
